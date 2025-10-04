package com.calometer.shared.core

/**
 * Lightweight Either-like result type for domain operations.
 */
sealed interface AppResult<out T, out E> {
    data class Success<T>(val value: T) : AppResult<T, Nothing>
    data class Error<E>(val reason: E, val cause: Throwable? = null) : AppResult<Nothing, E>

    fun getOrNull(): T? = (this as? Success<T>)?.value
    fun errorOrNull(): E? = (this as? Error<E>)?.reason

    inline fun <R> map(transform: (T) -> R): AppResult<R, E> = when (this) {
        is Success -> Success(transform(value))
        is Error -> this
    }
}

inline fun <T, E> AppResult<T, E>.onSuccess(block: (T) -> Unit): AppResult<T, E> = apply {
    if (this is AppResult.Success) block(value)
}

inline fun <T, E> AppResult<T, E>.onError(block: (E, Throwable?) -> Unit): AppResult<T, E> = apply {
    if (this is AppResult.Error) block(reason, cause)
}

fun <T> AppResult<T, *>.requireValue(): T = when (this) {
    is AppResult.Success -> value
    is AppResult.Error -> throw IllegalStateException("Expected success but was $reason", cause)
}
