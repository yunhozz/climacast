package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import org.springframework.stereotype.Component

@Component
class DocumentVisualizerFactory(handlers: Set<DocumentVisualizer>) {

    private val documentVisualizers = mutableMapOf<SubscriptionMethod, DocumentVisualizer>()

    init {
        handlers.forEach { handler ->
            handler.getSubscriptionMethods().forEach { method -> documentVisualizers[method] = handler }
        }
    }

    fun createDocumentVisualizerByMethod(method: SubscriptionMethod): DocumentVisualizer =
        documentVisualizers[method]
            ?: throw IllegalArgumentException("Document Visualizer Handler with $method Not Found")
}