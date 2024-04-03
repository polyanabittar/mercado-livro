package com.mercadolivro.repository

import com.mercadolivro.helper.buildCustomer
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var repository: CustomerRepository

    @BeforeEach
    fun setUp() = repository.deleteAll()

    @Test
    fun `should find by name containing`() {
        val marcos = repository.save(buildCustomer(name = "Marcos"))
        val matheus = repository.save(buildCustomer(name = "Matheus"))
        val alex = repository.save(buildCustomer(name = "Alex"))
        val pageRequest: PageRequest = PageRequest.of(0, 10)

        val customers = repository.findByNameContaining("Ma", pageRequest)

        assertTrue(customers.content.contains(marcos))
        assertTrue(customers.content.contains(matheus))
        assertFalse(customers.content.contains(alex))
    }

    @Nested
    inner class ExistByEmail {
        @Test
        fun `should return true when exist by email`() {
            val fakeEmail = "${UUID.randomUUID()}@email.com"
            repository.save(buildCustomer(email = fakeEmail))

            val exists = repository.existsByEmail(fakeEmail)

            assertTrue(exists)
        }

        @Test
        fun `should return false when not exist by email`() {
            val fakeEmail = "${UUID.randomUUID()}@email.com"

            val exists = repository.existsByEmail(fakeEmail)

            assertFalse(exists)
        }
    }

    @Nested
    inner class FindByEmail {
        @Test
        fun `should return customer when found by email`() {
            val fakeEmail = "${UUID.randomUUID()}@email.com"
            val fakeCustomer = buildCustomer(email = fakeEmail)
            repository.save(fakeCustomer)

            val customer = repository.findByEmail(fakeEmail)

            assertEquals(fakeCustomer, customer)
        }

        @Test
        fun `should return null when not found by email`() {
            val fakeEmail = "${UUID.randomUUID()}@email.com"

            val customer = repository.findByEmail(fakeEmail)

            assertNull(customer)
        }
    }
}