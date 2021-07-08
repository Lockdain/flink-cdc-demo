package ru.neoflex.flink.cdc.demo.secondary

import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{Requests, RestClientBuilder}
import ru.neoflex.flink.cdc.demo.datamodel.{AggregatedInfo, Client, Location, Transaction}

import java.util

trait ElasticSink {
  val httpHosts = new java.util.ArrayList[HttpHost]
  httpHosts.add(
    new HttpHost(
      "elasticsearch",
      9200,
      "http"
    )
  )

  val elasticSinkStringBuilder = new ElasticsearchSink.Builder[String](
    httpHosts,
    (element: String, ctx: RuntimeContext, indexer: RequestIndexer) => {
      val json = new util.HashMap[String, String]
      json.put("data", element)

      val request: IndexRequest = Requests.indexRequest
        .index("clients-index")
        .`type`("clients")
        .source(json)

      indexer.add(request)
    }
  )

  val elasticSinkClientBuilder = new ElasticsearchSink.Builder[Client](
    httpHosts,
    (element: Client, ctx: RuntimeContext, indexer: RequestIndexer) => {
      val json = new util.HashMap[String, String]
      json.put("data", element.asJson.noSpaces)

      val request: IndexRequest = Requests.indexRequest
        .index("clients-index")
        .`type`("clients")
        .source(json)

      indexer.add(request)
    }
  )

  val elasticSinkLocationBuilder = new ElasticsearchSink.Builder[Location](
    httpHosts,
    (element: Location, ctx: RuntimeContext, indexer: RequestIndexer) => {
      val json = new util.HashMap[String, String]
      json.put("data", element.asJson.noSpaces)

      val request: IndexRequest = Requests.indexRequest
        .index("locations-index")
        .`type`("locations")
        .source(json)

      indexer.add(request)
    }
  )

  val elasticSinkTransactionBuilder = new ElasticsearchSink.Builder[Transaction](
    httpHosts,
    (element: Transaction, ctx: RuntimeContext, indexer: RequestIndexer) => {
      val json = new util.HashMap[String, String]
      json.put("data", element.asJson.noSpaces)

      val request: IndexRequest = Requests.indexRequest
        .index("transactions-index")
        .`type`("transactions")
        .source(json)

      indexer.add(request)
    }
  )

  val elasticSinkAggregatedBuilder = new ElasticsearchSink.Builder[AggregatedInfo](
    httpHosts,
    (element: AggregatedInfo, ctx: RuntimeContext, indexer: RequestIndexer) => {
      val json = new util.HashMap[String, String]
      json.put("data", element.asJson.noSpaces)

      val request: IndexRequest = Requests.indexRequest
        .index("aggregations-index")
        .`type`("aggregations")
        .source(json)

      indexer.add(request)
    }
  )

  elasticSinkStringBuilder.setRestClientFactory((restClientBuilder: RestClientBuilder) => {})
}
