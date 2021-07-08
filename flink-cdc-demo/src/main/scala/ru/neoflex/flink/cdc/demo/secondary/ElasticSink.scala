package ru.neoflex.flink.cdc.demo.secondary

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch7.{ElasticsearchSink, RestClientFactory}
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{Requests, RestClientBuilder}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import ru.neoflex.flink.cdc.demo.datamodel.Client

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
    new ElasticsearchSinkFunction[String] {
      override def process(element: String, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
        val json = new util.HashMap[String, String]
        json.put("data", element)

        val request: IndexRequest = Requests.indexRequest
          .index("clients-index")
          .`type`("clients")
          .source(json)

        indexer.add(request)
      }
    }
  )

  val elasticSinkClientBuilder = new ElasticsearchSink.Builder[Client](
    httpHosts,
    new ElasticsearchSinkFunction[Client] {
      override def process(element: Client, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
        val json = new util.HashMap[String, String]
        json.put("data", element.asJson.noSpaces)

        val request: IndexRequest = Requests.indexRequest
          .index("clients-index")
          .`type`("clients")
          .source(json)

        indexer.add(request)
      }
    }
  )

  elasticSinkStringBuilder.setRestClientFactory(new RestClientFactory {
    override def configureRestClientBuilder(restClientBuilder: RestClientBuilder): Unit = {}
  })
}
