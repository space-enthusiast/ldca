package io.ldca

import io.kotest.core.config.AbstractProjectConfig
import io.ldca.plugins.KafkaAdminClient
import io.ldca.plugins.kafkaAdminClient
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class KafkaTestContainerConfiguration: AbstractProjectConfig() {

    private val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()

    override suspend fun beforeProject() {
        println("Global setup before all specs")
        kafka.start()
        KafkaAdminClient.initialize(kafka.bootstrapServers)
        kafkaAdminClient = KafkaAdminClient.instance
    }

    override suspend fun afterProject() {
        println("Global teardown after all specs")
        kafka.stop()
    }
}