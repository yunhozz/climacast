package com.climacast.ai_service.infra.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReactorCacheable(
    val name: String = ""
)