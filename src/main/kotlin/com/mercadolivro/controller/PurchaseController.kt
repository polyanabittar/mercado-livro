package com.mercadolivro.controller

import com.mercadolivro.controller.mapper.PurchaseMapper
import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.controller.response.BookResponse
import com.mercadolivro.extension.toResponse
import com.mercadolivro.security.OnlyAdminCanAccess
import com.mercadolivro.security.UserCanOnlyAccessTheirOwnResource
import com.mercadolivro.service.PurchaseService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/purchases")
class PurchaseController(
   private val purchaseService: PurchaseService,
   private val purchaseMapper: PurchaseMapper
) {

    @PostMapping
    @OnlyAdminCanAccess
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(@RequestBody request: PostPurchaseRequest) {
        purchaseService.create(purchaseMapper.toModel(request))
    }

    @GetMapping("/purchased/{id}")
    @UserCanOnlyAccessTheirOwnResource
    fun getPurchasedBooksbyCustomer(@PathVariable id: Int): List<BookResponse> {
        return purchaseService.getPurchasedBooks(id).toResponse()
    }

    @GetMapping("/sold/{id}")
    fun getSoldBooksbyCustomer(@PathVariable id: Int): List<BookResponse> {
        return purchaseService.getSoldBooks(id).toResponse()
    }
}