package io.ldca.plugins

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaProducerConfig(bootstrapServers: String) {
    private val properties: Properties = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
    }

    private val producer: KafkaProducer<String, String> = KafkaProducer(properties)

    fun sendMessage(topic: String, key: String, value: String) {
        val record = ProducerRecord(topic, key, value)
        producer.send(record) { metadata, exception ->
            if (exception == null) {
                println("Message sent to topic ${metadata.topic()} partition ${metadata.partition()} with offset ${metadata.offset()}")
            } else {
                exception.printStackTrace()
            }
        }
    }

    fun close() {
        producer.close()
    }
}