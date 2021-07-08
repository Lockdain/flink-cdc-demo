package ru.neoflex.flink.cdc.demo.datamodel

case class AggregatedInfo(
  id: Integer,
  name: String,
  surname: String,
  gender: String,
  coordinates: String,
  nearestCity: String,
  amount: BigDecimal)
