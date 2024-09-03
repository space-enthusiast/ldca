package io.ldca.plugins

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    fun consumeMessages(topic: String, channel: Channel<String>): Job {
        consumer.subscribe(listOf(topic))
        return scope.launch {
            while (true) {
                if (channel.isClosedForSend) {
                    consumer.close()
                    break
                }
                val consumedMessages = pollAndConsume(consumer)
                for (message in consumedMessages) {
                    channel.send(message)
                }
            }
        }
    }

    @Synchronized
    private fun pollAndConsume(
        consumer: KafkaConsumer<String, String>,
    ): List<String> {
        val consumedMessages = mutableListOf<String>()
        val records = consumer.poll(Duration.ofMillis(100))
        for (record in records) {
            val message = record.value()
            consumedMessages.add(message)
            println("Consumed message: ${record.value()}")
        }
        return consumedMessages.also {
            consumer.commitSync()
        }
    }
}