package com.shaun.stocktracker.util

/**
 * Sealed class wrapping all network responses.
 * Repositories return this instead of throwing raw exceptions.
 * Consumers use when() for clean, exhaustive handling.
 */
sealed class NetworkResult<out T> {

    /** Successful response with data. */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /** Non-auth error — network failure, server error, parse error, etc. */
    data class Error(
        val message: String,
        val code: Int? = null
    ) : NetworkResult<Nothing>()

    /** Backend returned 401/403 — session has expired, needs re-auth. */
    data class Unauthorized(val message: String = "Session expired") : NetworkResult<Nothing>()

    /** Optional loading state for UI consumers. */
    object Loading : NetworkResult<Nothing>()

    /** Convenience checks */
    val isSuccess: Boolean get() = this is Success
    val isUnauthorized: Boolean get() = this is Unauthorized

    /**
     * Returns the data if Success, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if Success, defaultValue otherwise.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }

    /**
     * Maps the data inside Success, passes through errors unchanged.
     */
    fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Unauthorized -> this
        is Loading -> this
    }
}
