package com.climacast.ai_service.infra.cache

import com.climacast.global.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.ParameterizedType

@Component
@Aspect
class ReactorCacheAspect(
    private val reactorCacheManager: ReactorCacheManager
) {
    private val log = logger()

    @Around("@annotation(com.climacast.ai_service.infra.cache.ReactorCacheable)")
    fun cacheAround(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val parametrizedType = method.genericReturnType as ParameterizedType
        val rawType = parametrizedType.rawType

        if (!rawType.equals(Mono::class.java) && !rawType.equals(Flux::class.java)) {
            throw IllegalArgumentException("The return type is not Mono/Flux. Use Mono/Flux for return type. method: " + method.name);
        }

        val reactorCacheable = method.getAnnotation(ReactorCacheable::class.java)
        val cacheName = reactorCacheable.name
        val args = joinPoint.args

        if (rawType.equals(Mono::class.java)) {
            val returnTypeInsideMono = parametrizedType.actualTypeArguments[0]
            val returnClass = ResolvableType.forType(returnTypeInsideMono).resolve()
            val retriever = { joinPoint.proceed(args) as Mono<Any> }

            return reactorCacheManager
                .findCachedMono(cacheName, generateKey(args), retriever, returnClass)
                .doOnError { ex ->
                    log.error(ex.localizedMessage, ex)
                }

        } else {
            val retriever = { joinPoint.proceed(args) as Flux<Any> }
            return reactorCacheManager
                .findCachedFlux(cacheName, generateKey(args), retriever)
                .doOnError { ex ->
                    log.error(ex.localizedMessage, ex)
                }
        }
    }

    private fun generateKey(vararg objects: Any?) = objects.joinToString(":") { obj -> obj?.toString() ?: "" }
}