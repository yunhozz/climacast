package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import org.springframework.stereotype.Component

@Component
class DocumentVisualizerHandlerFactory(handlers: Set<DocumentVisualizeHandler>) {

    private val documentVisualizerHandlers = mutableMapOf<SubscriptionMethod, DocumentVisualizeHandler>()

    init {
        handlers.forEach { handler ->
            handler.getSubscriptionMethods().forEach { method -> documentVisualizerHandlers[method] = handler }
        }
    }

    fun createDocumentVisualizerHandlerByMethod(method: SubscriptionMethod): DocumentVisualizeHandler =
        documentVisualizerHandlers[method]
            ?: throw IllegalArgumentException("Document Visualizer Handler with $method Not Found")
}