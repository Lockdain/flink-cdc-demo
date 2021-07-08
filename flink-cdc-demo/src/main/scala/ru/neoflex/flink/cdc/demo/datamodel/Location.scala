package ru.neoflex.flink.cdc.demo.datamodel

case class Location (id: Integer, coordinates: String, nearestCity: String, ts: Long)
