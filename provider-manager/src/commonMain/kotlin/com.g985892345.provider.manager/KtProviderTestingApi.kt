package com.g985892345.provider.manager

/**
 * Marks low-level APIs reserved for KtProvider testing infrastructure.
 *
 * Application code should use the public test helpers instead of opting into this API directly.
 */
@RequiresOptIn(
  message = "This API is intended for KtProvider testing infrastructure only.",
  level = RequiresOptIn.Level.ERROR,
)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class KtProviderTestingApi
