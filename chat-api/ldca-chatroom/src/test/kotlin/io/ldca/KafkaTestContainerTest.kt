package io.ldca

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Properties

class KafkaTestContainerTest: FunSpec({

    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()

    beforeSpec {
        kafka.start()
    }

    afterSpec {
        kafka.stop()
    }

    test("produce and consume message with kafka test container") {

        // Create AdminClient to manage Kafka topics
        val adminClient = AdminClient.create(mapOf("bootstrap.servers" to kafka.bootstrapServers))

        // Define the topic configuration
        val newTopic = NewTopic("test-topic", 1, 1.toShort())

        // Create the topic
        adminClient.createTopics(listOf(newTopic)).all().get()

        // Kafka Producer configuration
        val producerProps = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }

        // Kafka Consumer configuration
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

        val topic = "test-topic"

        // Create Kafka Producer
        val producer = KafkaProducer<String, String>(producerProps)

        // Send a message to the Kafka topic
        producer.send(ProducerRecord(topic, "key", "value"))
        producer.flush()

        // Create Kafka Consumer
        val consumer = KafkaConsumer<String, String>(consumerProps)
        consumer.subscribe(listOf(topic))

        // Poll for the message
        val records = consumer.poll(Duration.ofSeconds(5))

        // Assertions
        records.count() shouldBe 1
        records.first().value() shouldBe "value"

        // Cleanup
        adminClient.close()
        producer.close()
        consumer.close()
    }
})