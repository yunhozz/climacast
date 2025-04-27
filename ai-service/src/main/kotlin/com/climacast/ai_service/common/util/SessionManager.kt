package com.climacast.ai_service.common.util

import com.climacast.ai_service.common.enums.SessionType
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import java.time.Duration

object SessionManager {
    fun createSession(webSession: Mono<WebSession>, sessionType: SessionType): Mono<String> =
        webSession.flatMap { session ->
            val sessionName = sessionType.sessionName
            session.maxIdleTime = Duration.ofMinutes(sessionType.ttl)
            val sessionId = session.getAttributeOrDefault(sessionName, session.id)
            session.attributes[sessionName] = sessionId

            Mono.just(sessionId)
        }
}