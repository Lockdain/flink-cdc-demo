package ru.neoflex.flink.cdc.demo

import com.alibaba.ververica.cdc.connectors.postgres.PostgreSQLSource
import com.alibaba.ververica.cdc.debezium.{DebeziumSourceFunction, StringDebeziumDeserializationSchema}

trait PostgresSource extends ElasticSink {
  // PG Source
  val postgresCdcSource: DebeziumSourceFunction[String] = PostgreSQLSource.builder[String]
    .hostname("postgres")
    .port(5432)
    .database("account")
    .username("test")
    .password("test")
    .deserializer(new StringDebeziumDeserializationSchema())
    .slotName("flink_cdc")
    .decodingPluginName("wal2json")
    .build
}
