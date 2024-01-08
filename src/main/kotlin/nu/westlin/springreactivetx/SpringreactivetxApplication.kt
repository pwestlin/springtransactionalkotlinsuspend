package nu.westlin.springreactivetx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForList
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager

@SpringBootApplication
class SpringreactivetxApplication

private val logger: Logger = LoggerFactory.getLogger(SpringreactivetxApplication::class.java)

@Suppress("SimpleRedundantLet", "LoggingStringTemplateAsArgument")
        /*suspend */fun main(args: Array<String>) {
    /*
        runApplication<SpringreactivetxApplication>(*args).let { ctx ->
            runBlocking {
                ctx.getBean<FooService>().doShit()
            }
        }
    */

/*
    runApplication<SpringreactivetxApplication>(*args).let { ctx ->
        //logger.debug("ctx.getBean<TransactionManager>() = ${ctx.getBean<TransactionManager>()}")
        try {
            runBlocking {
                ctx.getBean<FooService>().let { service ->
                    service.truncateDatabaseTables()
                    withContext(Dispatchers.Default) {
                        service.save()
                    }
                }
            }
        } catch (e: RuntimeException) {
            logger.error("Det gick på skit")
        }

        logger.info("All person names:\n ${ctx.getBean<PersonRepository>().names()}")
        logger.info("All address citys:\n ${ctx.getBean<AddressRepository>().cities()}")
    }
*/
}

data class Person(val id: Long, val name: String)
data class Address(val id: Long, val city: String, val personId: Long)

@Repository
class PersonRepository(private val jdbcTemplate: JdbcTemplate) {

    fun save(person: Person) {
        SimpleJdbcInsert(jdbcTemplate)
            .withTableName("person")
            .execute(
                mapOf(
                    "id" to person.id,
                    "name" to person.name
                )
            )
    }

    fun truncate() {
        //jdbcTemplate.execute("truncate person")
        @Suppress("SqlWithoutWhere")
        jdbcTemplate.execute("delete from person")
    }

    fun names(): List<String> {
        return jdbcTemplate.queryForList<String>("select name from person")
    }
}

@Repository
class AddressRepository(private val jdbcTemplate: JdbcTemplate) {

    fun save(address: Address) {
        SimpleJdbcInsert(jdbcTemplate)
            .withTableName("address")
            .execute(
                mapOf(
                    "id" to address.id,
                    "city" to address.city,
                    "person_id" to address.personId,
                )
            )
    }

    fun truncate() {
        //jdbcTemplate.execute("truncate address")
        @Suppress("SqlWithoutWhere")
        jdbcTemplate.execute("delete from address")
    }

    fun cities(): List<String> {
        return jdbcTemplate.queryForList<String>("select city from address")
    }
}

@Service
class FooService(
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository,
    private val barService: BarService
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    //@Transactional
    fun truncateDatabaseTables() {
        addressRepository.truncate()
        personRepository.truncate()
    }

    @Transactional
    fun save() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        val person = Person(id = 1, name = "Peter")
        val address = Address(id = 1, city = "Hedemora", personId = person.id)
        personRepository.save(person)
        addressRepository.save(address)
        barService.save()
        throw RuntimeException("Foo")
    }

    @Transactional
    suspend fun suspendableSave() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        val person = Person(id = 1, name = "Peter")
        val address = Address(id = 1, city = "Hedemora", personId = person.id)
        personRepository.save(person)
        addressRepository.save(address)
        barService.saveSuspendable()
        throw RuntimeException("Foo")
    }
}

@Service
class BarService(
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional(propagation = Propagation.MANDATORY)
    fun save() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")

        personRepository.save(Person(id = 9249, name = "Mabel Howard"))
    }

    @Transactional(propagation = Propagation.MANDATORY)
    suspend fun saveSuspendable() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")

        personRepository.save(Person(id = 9249, name = "Mabel Howard"))
    }
}

/*
@Service
class BarService {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    suspend fun doShit() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")
    }
}

@Service
class FooService(
    private val barService: BarService
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    suspend fun doShit() {
        logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
        logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
        logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")

        //withContext(Dispatchers.IO) {
            logger.debug("TransactionSynchronizationManager.isActualTransactionActive() = ${TransactionSynchronizationManager.isActualTransactionActive()}")
            logger.debug("TransactionSynchronizationManager.isSynchronizationActive() = ${TransactionSynchronizationManager.isSynchronizationActive()}")
            logger.debug("TransactionSynchronizationManager.getCurrentTransactionName() = ${TransactionSynchronizationManager.getCurrentTransactionName()}")
            barService.doShit()
        //}

    }
}*/
