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

package org.apache.spark.sql.execution.datasources.oap.filecache

import org.apache.spark.SparkEnv
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution.datasources.oap.filecache.FiberType.FiberType
import org.apache.spark.sql.internal.oap.OapConf
import org.apache.spark.sql.test.oap.SharedOapContext

class OapCacheSuite extends SharedOapContext with Logging{

  override def beforeAll(): Unit = super.beforeAll()

  override def afterAll(): Unit = super.afterAll()

  test("detectPM guava cache offheap memory manager") {
    val sparkenv = SparkEnv.get
    sparkenv.conf.set("spark.oap.cache.strategy", "guava")
    sparkenv.conf.set("spark.sql.oap.fiberCache.memory.manager", "offheap")
    val cacheMemory: Long = 10000
    val cacheGuardianMemory: Long = 20000
    val fiberType: FiberType = FiberType.DATA
    val guavaCache: OapCache = OapCache(sparkenv, cacheMemory, cacheGuardianMemory, fiberType)
    assert(guavaCache.isInstanceOf[GuavaOapCache])
  }

  test("detectPM guava cache pm memory manager") {
    val sparkenv = SparkEnv.get
    val cacheMemory: Long = 10000
    val cacheGuardianMemory: Long = 20000
    val fiberType: FiberType = FiberType.DATA
    sparkenv.conf.set("spark.oap.cache.strategy", "guava")
    sparkenv.conf.set("spark.sql.oap.fiberCache.memory.manager", "pm")
    val simpleOapCache: OapCache = OapCache(sparkenv, cacheMemory, cacheGuardianMemory, fiberType)
    assert(simpleOapCache.isInstanceOf[SimpleOapCache])
  }

  test("detectPM vmem cache pm memory manager") {
    val sparkenv = SparkEnv.get
    val cacheMemory: Long = 10000
    val cacheGuardianMemory: Long = 20000
    val fiberType: FiberType = FiberType.DATA
    sparkenv.conf.set("spark.oap.cache.strategy", "vmem")
    sparkenv.conf.set("spark.sql.oap.fiberCache.memory.manager", "pm")
    sparkenv.conf.set("spark.oap.detectPmem.enabled", "false")
    val vmemCache: OapCache = OapCache(sparkenv, OapConf.OAP_FIBERCACHE_STRATEGY,
      cacheMemory, cacheGuardianMemory, fiberType)
    assert(vmemCache.isInstanceOf[VMemCache])
  }

  test("detectPM noevict cache pm memory manager") {
    val sparkenv = SparkEnv.get
    val cacheMemory: Long = 10000
    val cacheGuardianMemory: Long = 20000
    val fiberType: FiberType = FiberType.DATA
    sparkenv.conf.set("spark.oap.cache.strategy", "noevict")
    sparkenv.conf.set("spark.sql.oap.fiberCache.memory.manager", "pm")
    sparkenv.conf.set("spark.oap.detectPmem.enabled", "false")
    val noevictCache: OapCache = OapCache(sparkenv, OapConf.OAP_FIBERCACHE_STRATEGY,
      cacheMemory, cacheGuardianMemory, fiberType)
    assert(noevictCache.isInstanceOf[NonEvictPMCache])
  }

  test("detectPM external cache pm memory manager") {
    val sparkenv = SparkEnv.get
    val cacheMemory: Long = 10000
    val cacheGuardianMemory: Long = 20000
    val fiberType: FiberType = FiberType.DATA
    sparkenv.conf.set("spark.oap.cache.strategy", "external")
    sparkenv.conf.set("spark.sql.oap.fiberCache.memory.manager", "pm")
    sparkenv.conf.set("spark.oap.detectPmem.enabled", "false")
    val externalCache: OapCache = OapCache(sparkenv, OapConf.OAP_FIBERCACHE_STRATEGY,
      cacheMemory, cacheGuardianMemory, fiberType)
    assert(externalCache.isInstanceOf[ExternalCache])
  }
}
