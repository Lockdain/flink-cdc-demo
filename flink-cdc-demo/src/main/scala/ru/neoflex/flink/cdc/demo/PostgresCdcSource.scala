package ru.neoflex.flink.cdc.demo

import com.alibaba.ververica.cdc.connectors.postgres.PostgreSQLSource
import com.alibaba.ververica.cdc.debezium.StringDebeziumDeserializationSchema

trait PostgresCdcSource {
  val postgresCdcSource = PostgreSQLSource.builder[String]
    .hostname("postgres")
    .port(5432)
    .database("account")
//    .tableList("Clients")
    .username("test")
    .password("test")
    .deserializer(new StringDebeziumDeserializationSchema())
    .decodingPluginName("pgoutput")
    .build
}
