package io.ldca

import io.kotest.core.spec.style.FunSpec
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class KafkaTestContainerTest: FunSpec({

    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()

    beforeSpec {
        kafka.start()
    }

    afterSpec {
        kafka.stop()
    }

    test("crate a kafka test container and stop it") {

    }
})