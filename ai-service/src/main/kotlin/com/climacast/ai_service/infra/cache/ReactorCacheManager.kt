package com.climacast.ai_service.infra.cache

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import reactor.cache.CacheFlux
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.util.function.Supplier

@Component
class ReactorCacheManager(
    private val cacheManager: CacheManager
) {
    fun findCachedMono(
        cacheName: String,
        key: Any,
        retriever: Supplier<Mono<Any>>,
        classType: Class<*>?
    ): Mono<Any> {
        val cache = cacheManager.getCache(cacheName)
        assert(cache != null)

        return CacheMono.lookup({ k ->
            val result = cache!!.get(k, classType)
            Mono.justOrEmpty(result).map { Signal.next(it) }
        }, key)
            .onCacheMissResume(Mono.defer(retriever))
            .andWriteWith { k, signal ->
                Mono.fromRunnable {
                    if (!signal.isOnError)
                        cache!!.put(k, signal.get())
                }
            }
    }

    fun findCachedFlux(
        cacheName: String,
        key: Any,
        retriever: Supplier<Flux<Any>>
    ): Flux<Any> {
        val cache = cacheManager.getCache(cacheName)
        assert(cache != null)

        return CacheFlux.lookup<Any, Any>({ k ->
            val result = cache!!.get(k, List::class.java)
            Mono.justOrEmpty(result)
                .flatMap { list ->
                    Flux.fromIterable(list).materialize()
                        .collectList()
                }
        }, key)
            .onCacheMissResume(Flux.defer(retriever))
            .andWriteWith { k, signalList ->
                Flux.fromIterable(signalList).dematerialize<Any>()
                    .collectList()
                    .doOnNext { list -> cache!!.put(k, list) }
                    .then()
            }
    }
}