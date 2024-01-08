package nu.westlin.springreactivetx

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.TestcontainersConfiguration

/**
 * Postgres Docker container för Testcontainers.
 */
object PostgresContainer {

    val instance by lazy { startContainer() }

    private fun startContainer(): PostgreSQLContainer<Nothing> {
        val reuse = System.getProperty("containers.reuse").toBoolean()
        TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", reuse.toString())
        return PostgreSQLContainer<Nothing>(
            DockerImageName.parse("postgres:16")
        ).apply {
            withImagePullPolicy(PullPolicy.alwaysPull())
            withDatabaseName("springtx")
            withUsername("springtx")
            withPassword("springtx")
            withReuse(reuse)
            //waitingFor(Wait.forLogMessage(".*databassystemet är redo att ta emot anslutningar.*", 2))
            start()
        }
    }
}
