package com.climacast.subscription_service.service.handler.document.visual

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import org.springframework.stereotype.Component

@Component
class DocumentVisualizerFactory(visualizers: Set<DocumentVisualizer>) {

    private val documentVisualizerMap: Map<SubscriptionMethod, DocumentVisualizer> =
        visualizers.flatMap { visualizer ->
            visualizer.getSubscriptionMethods().map { method -> method to visualizer }
        }.toMap()

    fun createDocumentVisualizerByMethod(method: SubscriptionMethod): DocumentVisualizer =
        documentVisualizerMap[method]
            ?: throw IllegalArgumentException("No DocumentVisualizer found for SubscriptionMethod: $method")
}