package com.mercadolivro.controller.response

data class PageResponse<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long
)