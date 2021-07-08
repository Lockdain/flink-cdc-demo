package ru.neoflex.flink.cdc.demo.datamodel

case class Transaction (id: Integer, accountId: String, amount: BigDecimal, ts: Long)
