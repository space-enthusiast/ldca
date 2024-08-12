package io.ldca.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

const val KAFKA_BOOTSTRAP_SERVER = "kafka:9092"

lateinit var producer: KafkaProducer<String, ByteArray>
lateinit var consumer: KafkaConsumer<String, ByteArray>

val producerProps = mapOf(
    "bootstrap.servers" to KAFKA_BOOTSTRAP_SERVER,
    "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
    "security.protocol" to "PLAINTEXT"
)

val consumerProps = mapOf(
    "bootstrap.servers" to KAFKA_BOOTSTRAP_SERVER,
    "auto.offset.reset" to "earliest",
    "key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
    "value.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer",
    "group.id" to "someGroup",
    "security.protocol" to "PLAINTEXT"
)

fun Application.configureKafkaProducer() {
    log.info("Configuring Kafka producer")
    producer = KafkaProducer(producerProps)
}

fun Application.configureKafkaConsumer() {
    log.info("Configuring Kafka consumer")
    launch {
        consumer = KafkaConsumer<String, ByteArray>(consumerProps)
        consumer.subscribe(listOf("test"))
        while (true) {
            consumer.poll(100.milliseconds.toJavaDuration())
                .forEach { record ->
                    log.info("Received message: ${String(record.value())}")
                }
        }
    }
}

suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException)
                ?: continuation.resume(metadata)
        }
    }