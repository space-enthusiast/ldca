package io.ldca

import io.ktor.server.application.Application
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import java.util.Properties

object KafkaAdminClient {
    private lateinit var _bootstrapServer: String
    private val properties: Properties by lazy {
        Properties().apply {
            put("bootstrap.servers", _bootstrapServer)
        }
    }

    val instance: AdminClient by lazy {
        AdminClient.create(properties)
    }

    fun initialize(bootstrapServer: String) {
        _bootstrapServer = bootstrapServer
    }
}

lateinit var kafkaAdminClient: AdminClient

fun Application.configureKafkaAdminClient(kafkaBootStrapServers: String) {
    KafkaAdminClient.initialize(kafkaBootStrapServers)
    kafkaAdminClient = KafkaAdminClient.instance
}