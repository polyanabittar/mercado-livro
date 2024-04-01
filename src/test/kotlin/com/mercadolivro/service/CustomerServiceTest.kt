package com.mercadolivro.service

import com.mercadolivro.enum.CustomerStatus
import com.mercadolivro.enum.Roles
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*


@ExtendWith(MockKExtension::class)
class CustomerServiceTest {
    @InjectMockKs
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