package io.ldca

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.MySQLContainer
import java.sql.DriverManager

class MysqlTestContainerTest: FunSpec({
    val mysql = MySQLContainer<Nothing>("mysql:8.0.26").apply {
        startupAttempts = 1
        withDatabaseName("testdb")
        withUsername("testuser")
        withPassword("testpass")
        withUrlParam("connectionTimeZone", "Z")
        withUrlParam("zeroDateTimeBehavior", "convertToNull")
    }

    beforeSpec {
        mysql.start()
    }

    afterSpec {
        mysql.stop()
    }

    test("should insert and retrieve data from MySQL") {
        val connection = DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password)

        connection.use { conn ->
            val statement = conn.createStatement()
            statement.executeUpdate("CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(255))")
            statement.executeUpdate("INSERT INTO test_table (id, name) VALUES (1, 'Test Name')")

            val resultSet = statement.executeQuery("SELECT name FROM test_table WHERE id = 1")
            resultSet.next()
            val name = resultSet.getString("name")

            name shouldBe "Test Name"
        }
    }
})