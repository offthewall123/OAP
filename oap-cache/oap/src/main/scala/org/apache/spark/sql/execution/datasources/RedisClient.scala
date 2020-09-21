/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import org.apache.spark.SparkEnv
import org.apache.spark.internal.Logging
import org.apache.spark.sql.internal.oap.OapConf

class RedisClient extends ExternalDBClient with Logging {

  private var redisClientPool: JedisPool = null

  private implicit val formats = DefaultFormats

  override def init(sparkEnv: SparkEnv): Unit = {
    logInfo("Initing RedisClientPool, server address is : " +
      sparkEnv.conf.get(OapConf.OAP_EXTERNAL_CACHE_METADB_ADDRESS))

    val jedisPoolConfig = new JedisPoolConfig
    jedisPoolConfig.setMaxTotal(10)
    redisClientPool = new JedisPool(
      jedisPoolConfig,
      sparkEnv.conf.get(OapConf.OAP_EXTERNAL_CACHE_METADB_ADDRESS))
  }

  override def get(fileName: String, start: Long,
                   length: Long): ArrayBuffer[CacheMetaInfoValue] = {
    var jedisClientInstance: Jedis = null
    val cacheMetaInfoArrayBuffer: ArrayBuffer[CacheMetaInfoValue] =
      new ArrayBuffer[CacheMetaInfoValue](0)
    try {
      jedisClientInstance = redisClientPool.getResource
      // jedisClientInstance.zrange() returns a java.util.Set
      // if not define it or use .asInstanceOf[Set[String]]
      // would throw exception "cannot be cast to scala.collection.immutable.Set"
      // zrange()'s return will be cast to scala.collection.immutable.Set automatically
      // start - 1 because zrange is (start, length]
      val cacheMetaInfoValueJavaSet: java.util.Set[String] =
        jedisClientInstance.zrange(fileName, start - 1, length)
      val cacheMetaInfoValueSet: scala.collection.mutable.Set[String] =
        cacheMetaInfoValueJavaSet.asScala

      for (x <- cacheMetaInfoValueSet) {
        cacheMetaInfoArrayBuffer.+=(parse(x.asInstanceOf[String]).extract[CacheMetaInfoValue])
      }
    } finally {
      if (null != jedisClientInstance) {
        jedisClientInstance.close()
      }
    }
    cacheMetaInfoArrayBuffer
  }

  override def upsert(cacheMetaInfo: CacheMetaInfo): Boolean = {
    var jedisClientInstance: Jedis = null
    try {
      jedisClientInstance = redisClientPool.getResource
      cacheMetaInfo match {
        case storeInfo: StoreCacheMetaInfo =>
          val value = storeInfo._value
          val cacheMetaInfoJson = ("offSet" -> value._offSet) ~
            ("length" -> value._length) ~
            ("host" -> value._host)
          logDebug("upsert key: " + storeInfo._key +
            "cacheMetaInfo is: " + value.toString)
          jedisClientInstance
            .zadd(storeInfo._key, value._offSet, compact(render(cacheMetaInfoJson)))
            .equals(1L)
        case evictInfo: EvictCacheMetaInfo =>
          val value = evictInfo._value
          val cacheMetaInfoJson = ("offSet" -> value._offSet) ~
            ("length" -> value._length) ~
            ("host" -> value._host)
          logDebug("evict key: " + evictInfo._key +
            "cacheMetaInfo is: " + value.toString)
          jedisClientInstance
            .zrem(evictInfo._key.asInstanceOf[String], compact(render(cacheMetaInfoJson)))
            .equals(1L)
      }
    } finally {
      if (null != jedisClientInstance) {
        jedisClientInstance.close()
      }
    }
    false
  }

  override def stop(): Unit = {
    if (null != redisClientPool) {
      redisClientPool.destroy()
      logWarning("Redis client pool closed.")
    }
  }
}
