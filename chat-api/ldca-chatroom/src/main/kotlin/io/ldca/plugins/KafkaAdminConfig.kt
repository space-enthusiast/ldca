package io.ldca.plugins

import io.ktor.server.application.Application
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import java.util.Properties

private object KafkaAdminClient {
    private val properties = Properties().apply {
        put("bootstrap.servers", "kafka:9092") // Your broker(s) address here
    }

    val instance: AdminClient by lazy {
        AdminClient.create(properties)
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

fun getTopics(): Collection<String> {
    return kafkaAdminClient.listTopics().names().get()
        ?: emptyList()
}

lateinit var kafkaAdminClient: AdminClient

fun Application.configureKafkaAdminClient() {
    kafkaAdminClient = KafkaAdminClient.instance
}