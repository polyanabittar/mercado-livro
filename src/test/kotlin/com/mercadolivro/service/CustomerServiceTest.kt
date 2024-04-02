package com.mercadolivro.service

import com.mercadolivro.enum.CustomerStatus
import com.mercadolivro.enum.Roles
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID
import java.util.Random
import java.util.Optional

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {
    @InjectMockKs
    @SpyK
    private lateinit var customerService: CustomerService

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @Test
    fun `should return all customers`() {
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))
        val pageRequest: PageRequest = PageRequest.of(0, 10)

        every { customerRepository.findAll(any(PageRequest::class)) } returns fakeCustomers

        val customers = customerService.getAll(null, pageRequest)

        assertEquals(fakeCustomers, customers)

        verify(exactly = 1) { customerRepository.findAll(any(PageRequest::class)) }
        verify(exactly = 0) { customerRepository.findByNameContaining(any(), any()) }
    }

    @Test
    fun `should return customers when name is informed`() {
        val name = UUID.randomUUID().toString()
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))
        val pageRequest: PageRequest = PageRequest.of(0, 10)

        every {
            customerRepository.findByNameContaining(name, any(PageRequest::class))
        } returns fakeCustomers

        val customers = customerService.getAll(name, pageRequest)

        assertEquals(fakeCustomers, customers)

        verify(exactly = 0) { customerRepository.findAll(any(PageRequest::class)) }
        verify(exactly = 1) { customerRepository.findByNameContaining(name, any()) }
    }

    @Test
    fun `should create customer and encrypt password`() {
        val initialPassword = Math.random().toString()
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomer = buildCustomers(password = initialPassword)
        val fakeCustomerEncrypted = fakeCustomer.copy(password = fakePassword)

        every { bCrypt.encode(initialPassword) } returns fakePassword
        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomer

        customerService.createCustomer(fakeCustomer)

        verify(exactly = 1) { bCrypt.encode(initialPassword) }
        verify(exactly = 1) { customerRepository.save(fakeCustomerEncrypted) }
    }

    @Test
    fun `should find customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        val customer = customerService.findById(id)
        assertEquals(fakeCustomer, customer)

        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should throw not found exception when find by id`() {
        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> {
            customerService.findById(id)
        }

        assertEquals("Customer [$id] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should update customer`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        customerService.updateCustomer(fakeCustomer)

        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 1) { customerRepository.save(fakeCustomer) }
    }

    @Test
    fun `should throw not found exception when update customer`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.existsById(id) } returns false

        val error = assertThrows<NotFoundException> {
            customerService.updateCustomer(fakeCustomer)
        }

        assertEquals("Customer [$id] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `should delete customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)
        val expectedCustomer = fakeCustomer.copy(status = CustomerStatus.INACTIVE)

        every { customerService.findById(id) } returns fakeCustomer
        every { bookService.deleteByCustomer(fakeCustomer) } just runs
        every { customerRepository.save(fakeCustomer) } returns expectedCustomer

        customerService.deleteCustomer(id)

        assertEquals("INACTIVE", fakeCustomer.status.name)

        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 1) { bookService.deleteByCustomer(fakeCustomer) }
        verify(exactly = 1) { customerRepository.save(expectedCustomer) }
    }

    @Test
    fun `should throw not found exception when delete customer by id`() {
        val id = Random().nextInt()

        every { customerService.findById(id) } throws NotFoundException("Customer [$id] not exists", "ML-201")

        val error = assertThrows<NotFoundException> {
            customerService.deleteCustomer(id)
        }

        assertEquals("Customer [$id] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 0) { bookService.deleteByCustomer(any()) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `should return false when email is not available`() {
        val fakeEmail = "${UUID.randomUUID()}@email.com"

        every { customerRepository.existsByEmail(fakeEmail) } returns true

        val existsByEmail = customerService.emailAvailable(fakeEmail)

        assertFalse(existsByEmail)

        verify(exactly = 1) { customerRepository.existsByEmail(fakeEmail) }
    }

    @Test
    fun `should return true when email is available`() {
        val fakeEmail = "${UUID.randomUUID()}@email.com"

        every { customerRepository.existsByEmail(fakeEmail) } returns false

        val existsByEmail = customerService.emailAvailable(fakeEmail)

        assertTrue(existsByEmail)

        verify(exactly = 1) { customerRepository.existsByEmail(fakeEmail) }
    }

    private fun buildCustomers(
        id: Int? = null,
        name: String = "customer name",
        email: String = "${UUID.randomUUID()}@email.com",
        password: String = "password"
    ) = CustomerModel (
        id = id,
        name = name,
        email = email,
        status = CustomerStatus.ACTIVE,
        password = password,
        roles = setOf(Roles.CUSTOMER)
    )
}