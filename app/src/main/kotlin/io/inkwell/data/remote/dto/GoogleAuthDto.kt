package io.inkwell.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)

@Serializable
data class GoogleAuthResponse(val token: String)
