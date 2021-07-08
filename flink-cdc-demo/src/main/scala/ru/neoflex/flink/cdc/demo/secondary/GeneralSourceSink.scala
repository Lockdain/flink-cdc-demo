package ru.neoflex.flink.cdc.demo.secondary

import org.apache.flink.api.common.typeinfo.TypeInformation
import ru.neoflex.flink.cdc.demo.datamodel.Client

trait GeneralSourceSink extends PostgresSource {
  implicit val stringTypeInfo = TypeInformation.of(classOf[String])
  implicit val clientTypeInfo = TypeInformation.of(classOf[Client])

}
