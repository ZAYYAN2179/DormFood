package com.zayyan.dormfood.ui.model

sealed class SubmitReviewState {
    object Idle : SubmitReviewState()
    object Loading : SubmitReviewState()
    object Success : SubmitReviewState()
    data class Error(val message: String) : SubmitReviewState()
    object AlreadyReviewed : SubmitReviewState()
}