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
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import ru.neoflex.flink.cdc.demo.datamodel.{AggregatedInfo, Client, Location, Transaction}
import ru.neoflex.flink.cdc.demo.secondary.GeneralSourceSink

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
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    setEnvParameters(env)
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)

    // Materialize Clients CDC
    tableEnv.executeSql(
      "CREATE TABLE clients (id INT, name STRING, surname STRING, gender STRING, address STRING) " +
        "WITH ('connector' = 'postgres-cdc', " +
        "'hostname' = 'postgres', " +
        "'port' = '5432', " +
        "'username' = 'test', " +
        "'password' = 'test', " +
        "'database-name' = 'account',  " +
        "'schema-name' = 'accounts', " +
        "'table-name' = 'Clients'," +
        "'debezium.slot.name' = 'clients_cdc')"
    )

    // Materialize Locations CDC
    tableEnv.executeSql(
      "CREATE TABLE locations (client_l_id INT, coordinates STRING, nearest_city STRING, ts_l DECIMAL) " +
        "WITH ('connector' = 'postgres-cdc', " +
        "'hostname' = 'postgres', " +
        "'port' = '5432', " +
        "'username' = 'test', " +
        "'password' = 'test', " +
        "'database-name' = 'account', " +
        "'schema-name' = 'accounts', " +
        "'table-name' = 'ClientLocation'," +
        "'debezium.slot.name' = 'locations_cdc')"
    )

    // Materialize Transactions CDC
    tableEnv.executeSql(
      "CREATE TABLE transactions (client_t_id INT, account_id STRING, amount DECIMAL, ts_t DECIMAL) " +
        "WITH ('connector' = 'postgres-cdc', " +
        "'hostname' = 'postgres', " +
        "'port' = '5432', " +
        "'username' = 'test', " +
        "'password' = 'test', " +
        "'database-name' = 'account', " +
        "'schema-name' = 'accounts', " +
        "'table-name' = 'ClientTransaction'," +
        "'debezium.slot.name' = 'transactions_cdc')"
    )

    // Updates from clients
    val clients: Table = tableEnv.sqlQuery("SELECT * FROM clients")

    // Updates from locations
    val locations: Table = tableEnv.sqlQuery("SELECT * FROM locations")

    // Updates from transactions
    val transactions: Table = tableEnv.sqlQuery("SELECT * FROM transactions")

    // Clients to change stream
    val clientsDataStream: DataStream[Row] = tableEnv.toChangelogStream(clients)

    // Locations to change stream
    val locationsRowDataStream: DataStream[Row] = tableEnv.toChangelogStream(locations)

    // Transactions to change stream
    val transactionsRowDataStream: DataStream[Row] = tableEnv.toChangelogStream(transactions)

    // Send Clients to Elasticsearch for monitoring purposes
    val clientsStream: DataStream[Client] = clientsDataStream.map { row =>
      Client(
        row.getFieldAs[Integer]("id"),
        row.getFieldAs[String]("name"),
        row.getFieldAs[String]("surname"),
        row.getFieldAs[String]("gender"),
        row.getFieldAs[String]("address")
      )
    }
    clientsStream.addSink(elasticSinkClientBuilder.build)

    // Send Locations to Elasticsearch for monitoring purposes
    val locationsStream: DataStream[Location] = locationsRowDataStream.map { row =>
      Location(
        row.getFieldAs[Integer]("client_l_id"),
        row.getFieldAs[String]("coordinates"),
        row.getFieldAs[String]("nearest_city"),
        row.getFieldAs[java.math.BigDecimal]("ts_l").longValue
      )
    }
    locationsStream.addSink(elasticSinkLocationBuilder.build)

    // Send Transactions to Elasticsearch for monitoring purposes
    val transactionsStream: DataStream[Transaction] = transactionsRowDataStream.map { row =>
      Transaction(
        row.getFieldAs[Integer]("client_t_id"),
        row.getFieldAs[String]("account_id"),
        row.getFieldAs[java.math.BigDecimal]("amount"),
        row.getFieldAs[java.math.BigDecimal]("ts_t").longValue
      )
    }
    transactionsStream.addSink(elasticSinkTransactionBuilder.build)

    // Joining transactions on locations
    val preAggregateStream: DataStream[AggregatedInfo] = transactionsStream
      .join(locationsStream)
      .where(_.id)
      .equalTo(_.id)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(2)))
      .apply { (transaction, location) =>
        AggregatedInfo(
          transaction.id,
          "",
          "",
          "",
          location.coordinates,
          location.nearestCity,
          transaction.amount
        )
      }

//    preAggregateStream
//      .addSink(elasticSinkAggregatedBuilder.build)

    // Joining aggregate on client info
    val fullyAggregatedStream: DataStream[AggregatedInfo] = clientsStream
      .join(preAggregateStream)
      .where(_.id)
      .equalTo(_.id)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
      .allowedLateness(Time.seconds(30))
      .apply { (clientInfo, preAggregate) =>
        AggregatedInfo(
          clientInfo.id,
          clientInfo.name,
          clientInfo.surname,
          clientInfo.gender,
          preAggregate.coordinates,
          preAggregate.nearestCity,
          preAggregate.amount
        )
      }

    fullyAggregatedStream
      .addSink(elasticSinkAggregatedBuilder.build)

    env.execute("Postgres CDC Streaming Job")
  }

  private def setEnvParameters(env: StreamExecutionEnvironment): Unit = {
    env.enableCheckpointing(1000)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500)
    env.getCheckpointConfig.setCheckpointStorage(new JobManagerCheckpointStorage(4000000))
  }
}
