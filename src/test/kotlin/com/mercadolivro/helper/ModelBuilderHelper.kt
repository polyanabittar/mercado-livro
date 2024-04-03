package com.mercadolivro.helper

import com.mercadolivro.enum.BookStatus
import com.mercadolivro.enum.CustomerStatus
import com.mercadolivro.enum.Roles
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.model.PurchaseModel
import java.math.BigDecimal
import java.util.UUID

fun buildCustomers(
    id: Int? = null,
    name: String = "customer name",
    email: String = "${UUID.randomUUID()}@email.com",
    status: CustomerStatus = CustomerStatus.ACTIVE,
    password: String = "password"
) = CustomerModel(
    id = id,
    name = name,
    email = email,
    status = status,
    password = password,
    roles = setOf(Roles.CUSTOMER)
)

fun buildPurchase(
    id: Int? = null,
    customer: CustomerModel = buildCustomers(),
    books: MutableList<BookModel> = mutableListOf(),
    nfe: String = UUID.randomUUID().toString(),
    price: BigDecimal = BigDecimal.TEN
) = PurchaseModel(
    id = id,
    customer = customer,
    books = books,
    nfe = nfe,
    price = price
)

fun buildBook(
    id: Int? = null,
    name: String = "name",
    price: BigDecimal = BigDecimal.TEN,
    customer: CustomerModel = buildCustomers(),
    status: BookStatus = BookStatus.ATIVO
) = BookModel(
    id = id,
    name = name,
    price = price,
    customer = customer,
    status = status
)