package io.ldca.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

class KafkaConsumerConfig(bootstrapServers: String, groupId: String) {

    private val properties: Properties = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    private val consumer: KafkaConsumer<String, String> = KafkaConsumer(properties)
    @OptIn(DelicateCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext("KafkaConsumerThread") + Job())

    fun consumeMessages(topic: String, onMessage: suspend (String, String) -> Unit) {
        consumer.subscribe(listOf(topic))
        scope.launch {
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                for (record in records) {
                    onMessage(record.key(), record.value())
                }
            }
        }
    }

    fun close() {
        scope.cancel()
        consumer.close()
    }
}