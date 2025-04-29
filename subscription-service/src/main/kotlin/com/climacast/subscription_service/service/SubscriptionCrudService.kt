package com.climacast.subscription_service.service

import com.climacast.subscription_service.common.enums.SubscriptionInterval
import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.exception.SubscriptionServiceException
import com.climacast.subscription_service.infra.repository.SubscriptionRepository
import com.climacast.subscription_service.model.dto.SubscriptionCreateRequestDTO
import com.climacast.subscription_service.model.dto.SubscriptionResponseDTO
import com.climacast.subscription_service.model.dto.SubscriptionUpdateRequestDTO
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionCrudService(
    private val subscriptionRepository: SubscriptionRepository
) {
    @Transactional
    fun createSubscription(dto: SubscriptionCreateRequestDTO): Long? {
        val subscriptionInfo = SubscriptionInfo(dto.email, dto.phoneNumber)
        validateSubscriptionExist(subscriptionInfo)
        return subscriptionRepository.save(dto.toEntity()).id
    }

    @Transactional
    fun updateSubscription(dto: SubscriptionUpdateRequestDTO): Long? {
        val subscription = findSubscriptionByEmailOrPhoneNumber(dto.email, dto.phoneNumber)
        subscription.updateByUserRequest(
            dto.regions,
            SubscriptionInterval.of(dto.interval),
            SubscriptionMethod.of(dto.method)
        )

        return subscription.id
    }

    @Transactional(readOnly = true)
    fun getSubscriptionByEmailAndPhoneNumber(email: String?, phoneNumber: String?): SubscriptionResponseDTO {
        val subscription = findSubscriptionByEmailOrPhoneNumber(email, phoneNumber)
        return SubscriptionResponseDTO.fromEntity(subscription)
    }

    @Transactional
    fun cancelSubscriptionByEmailAndPhoneNumber(email: String?, phoneNumber: String?) {
        val subscription = findSubscriptionByEmailOrPhoneNumber(email, phoneNumber)
        subscription.cancelByUserRequest()
    }

    private fun findSubscriptionByEmailOrPhoneNumber(email: String?, phoneNumber: String?) =
        subscriptionRepository.findByEmailOrPhoneNumber(email, phoneNumber)
            ?: throw SubscriptionServiceException.SubscriptionNotFoundException()

    private fun validateSubscriptionExist(info: SubscriptionInfo) {
        if (subscriptionRepository.existsBySubscriptionInfoAndStatus(info)) {
            throw SubscriptionServiceException.SubscriptionAlreadyExistException()
        }
    }
}