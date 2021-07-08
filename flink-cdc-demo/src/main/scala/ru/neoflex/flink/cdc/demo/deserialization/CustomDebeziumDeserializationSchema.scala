package ru.neoflex.flink.cdc.demo.deserialization

import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema
import org.apache.flink.api.common.typeinfo.BasicTypeInfo
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.util.Collector
import org.apache.kafka.connect.source.SourceRecord
import play.api.libs.json.Json

class CustomDebeziumDeserializationSchema extends DebeziumDeserializationSchema[String] {
  val serialVersionUID = -3168848983285670603L

  implicit val sourceRecordWrites = Json.writes[MappedSourceRecord]

  override def deserialize(sourceRecord: SourceRecord, collector: Collector[String]): Unit = {
    val mappedSourceRecord = MappedSourceRecord(
      sourceRecord.value().toString,
      sourceRecord.timestamp,
      sourceRecord.keySchema().schema().toString,
      sourceRecord.valueSchema().valueSchema.schema().toString,
      sourceRecord.valueSchema().valueSchema.schema().toString
    )

    collector.collect(Json.toJson(mappedSourceRecord).toString)
  }
  override def getProducedType
    : _root_.org.apache.flink.api.common.typeinfo.TypeInformation[_root_.scala.Predef.String] =
    BasicTypeInfo.STRING_TYPE_INFO
}

case class MappedSourceRecord(value: String, ts: Long, db: String, schema: String, table: String)
