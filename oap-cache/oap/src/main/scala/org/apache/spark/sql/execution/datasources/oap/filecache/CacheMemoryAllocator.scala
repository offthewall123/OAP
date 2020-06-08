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
import org.apache.spark.sql.internal.oap.OapConf

/**
 * A cache memory allocator which handles memory allocating from underlayer
 * memory manager and handles index and data cache seperation.
 */
private[filecache] class CacheMemoryAllocator(sparkEnv: SparkEnv)
  extends Logging {
  private val (memoryManager) = init()
  private val (_dataCacheMemorySize, _dataCacheGuardianMemorySize) = calculateSizes()

  private def calculateSizes(): (Long, Long) = {
    val memorySize = memoryManager.memorySize
    ((memorySize * 0.9).toLong, (memorySize * 0.1).toLong)
  }

  private def init(): MemoryManager = {
    MemoryManager(sparkEnv)
  }

  def allocateDataMemory(size: Long): MemoryBlockHolder = {
    memoryManager.allocate(size)
  }

  def freeDataMemory(block: MemoryBlockHolder): Unit = {
    memoryManager.free(block)
  }

  def isDcpmmUsed(): Boolean = {
    memoryManager.isDcpmmUsed()
  }

  def stop(): Unit = {
    memoryManager.stop()
  }

  def dataCacheMemorySize: Long = _dataCacheMemorySize
  def dataCacheGuardianMemorySize: Long = _dataCacheGuardianMemorySize
}

private[sql] object CacheMemoryAllocator {
  def apply(sparkEnv: SparkEnv): CacheMemoryAllocator = {
    new CacheMemoryAllocator(sparkEnv)
  }
}
