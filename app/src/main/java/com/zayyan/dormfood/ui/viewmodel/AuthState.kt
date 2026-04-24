package com.zayyan.dormfood.ui.viewmodel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class LoginSuccess(
        val message: String,
        val role: String,
        val userName: String
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
