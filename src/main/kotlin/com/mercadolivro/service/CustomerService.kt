package com.mercadolivro.service

import com.mercadolivro.enum.CustomerStatus
import com.mercadolivro.enum.Errors
import com.mercadolivro.enum.Roles
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val bookService: BookService,
    private val bCrypt: BCryptPasswordEncoder
) {
    fun getAll(name: String?, pageable: Pageable): Page<CustomerModel> {
        name?.let {
            return customerRepository.findByNameContaining(it, pageable)
        }
        return customerRepository.findAll(pageable)
    }

    fun createCustomer(customer: CustomerModel) {
        val customerCopy = customer.copy(
            roles = setOf(Roles.CUSTOMER),
            password = bCrypt.encode(customer.password)
        )
        customerRepository.save(customerCopy)
    }

    fun findById(id: Int): CustomerModel {
        return customerRepository.findById(id).orElseThrow{
            NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)
        }
    }

    fun updateCustomer(customer: CustomerModel) {
        if (!customerRepository.existsById(customer.id!!)) {
            throw NotFoundException(Errors.ML201.message.format(customer.id), Errors.ML201.code)
        }
        customerRepository.save(customer)
    }

    fun deleteCustomer(id: Int) {
        val customer = findById(id)
        bookService.deleteByCustomer(customer)

        customer.status = CustomerStatus.INACTIVE
        customerRepository.save(customer)
    }

    fun emailAvailable(email: String): Boolean {
        return !customerRepository.existsByEmail(email)
    }
}