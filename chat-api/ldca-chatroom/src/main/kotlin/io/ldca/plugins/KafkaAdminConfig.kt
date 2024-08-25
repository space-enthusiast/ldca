package io.ldca.plugins

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

    fun close() {
        instance.close()
    }
}

fun createTopic(topicName: String, numPartitions: Int, replicationFactor: Short) {
    val adminClient = KafkaAdminClient.instance
    val newTopic = NewTopic(topicName, numPartitions, replicationFactor)
    try {
        adminClient.createTopics(listOf(newTopic)).all().get()
        println("Topic $topicName created successfully.")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteTopic(topicName: String) {
    val adminClient = KafkaAdminClient.instance
    try {
        adminClient.deleteTopics(listOf(topicName)).all().get()
        println("Topic $topicName deleted successfully.")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getTopics(): Collection<String> {
    return kafkaAdminClient.listTopics().names().get()
        ?: emptyList()
}

lateinit var kafkaAdminClient: AdminClient

fun Application.configureKafkaAdminClient() {
    KafkaAdminClient.initialize("kafka:9092")
    kafkaAdminClient = KafkaAdminClient.instance
}