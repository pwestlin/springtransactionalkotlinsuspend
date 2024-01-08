package nu.westlin.springreactivetx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
class SpringreactivetxApplicationTests(
    @Autowired
    private val fooService: FooService,
    @Autowired
    private val personRepository: PersonRepository,
    @Autowired
    private val addressRepository: AddressRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @BeforeEach
    @AfterEach
    fun clearDatabase() {
        fooService.truncateDatabaseTables()
        assertThat(personRepository.names()).isEmpty()
        assertThat(addressRepository.cities()).isEmpty()
    }

    @Test
    fun `regular (no suspend) functions`() {
        assertThatThrownBy { fooService.save() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Foo")

        assertThat(personRepository.names()).isEmpty()
        assertThat(addressRepository.cities()).isEmpty()
    }

    @Test
    fun `suspend functions i main thread`() {
        assertThatThrownBySuspendable { fooService.suspendableSave() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Foo")

        assertThat(personRepository.names()).isEmpty()
        assertThat(addressRepository.cities()).isEmpty()
    }

    private fun assertThatThrownBySuspendable(executable: suspend () -> Unit): AbstractThrowableAssert<*, out Throwable> = Assertions.assertThatThrownBy {
        runBlocking {
            executable()
        }
    }

    companion object {
        private val postgreSQLContainer = PostgresContainer.instance

        @Suppress("unused")
        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }
    }
}
