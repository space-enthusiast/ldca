package io.ldca

import io.kotest.core.config.AbstractProjectConfig
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class KafkaTestContainerConfiguration: AbstractProjectConfig() {

    override suspend fun beforeProject() {
        println("Global setup before all specs")
        kafkaTestContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()
        kafkaTestContainer.start()
    }

    override suspend fun afterProject() {
        println("Global teardown after all specs")
        kafkaTestContainer.stop()
    }
}

lateinit var kafkaTestContainer: KafkaContainer