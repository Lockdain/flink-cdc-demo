package ru.neoflex.flink.cdc.demo

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.elasticsearch.{ ElasticsearchSinkFunction, RequestIndexer }
import org.apache.flink.streaming.connectors.elasticsearch7.{ ElasticsearchSink, RestClientFactory }
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{ Requests, RestClientBuilder }

import java.util

/**
 * Skeleton for a Flink Job.
 *
 * You can also generate a .jar file that you can submit on your Flink
 * cluster. Just type
 * {{{
 *   sbt clean assembly
 * }}}
 * in the projects root directory. You will find the jar in
 * target/scala-2.11/Flink\ Project-assembly-0.1-SNAPSHOT.jar
 */
object PostgresCdcJob extends GeneralSourceSink {
  def main(args: Array[String]) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    setEnvParameters(env)

    val cdcSource = env
      .addSource(postgresCdcSource)

    cdcSource
      .uid("PG CDC Source")
      .name("PG CDC Source")
      .setParallelism(1)

    cdcSource
      .addSink(elasticSinkBuilder.build)
      .setParallelism(1)
      .name("Elasticsearch Sink")
      .uid("Elasticsearch Sink")

    env.execute("Postgres CDC Streaming Job")
  }

  private def setEnvParameters(env: _root_.org.apache.flink.streaming.api.environment.StreamExecutionEnvironment) = {
    env.enableCheckpointing(1000)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500)
    env.getCheckpointConfig.setCheckpointStorage(new JobManagerCheckpointStorage(4000000))
  }
}
