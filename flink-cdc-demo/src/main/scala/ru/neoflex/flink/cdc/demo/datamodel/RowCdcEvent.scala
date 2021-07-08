package ru.neoflex.flink.cdc.demo.datamodel

case class RowCdcEvent(_index: String, _type: String, _id: String, _version: Integer, _score: Integer, _source: CdcData)
case class CdcData(data: String)
