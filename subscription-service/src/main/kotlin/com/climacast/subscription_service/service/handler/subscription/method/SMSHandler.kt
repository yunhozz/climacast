package com.climacast.subscription_service.service.handler.subscription.method

import com.climacast.global.utils.logger
import com.climacast.subscription_service.service.handler.subscription.SubscriberInfo
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandler
import com.climacast.subscription_service.service.handler.subscription.SubscriptionHandlerName
import com.twilio.Twilio
import com.twilio.exception.ApiException
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component
class SMSHandler : SubscriptionHandler {

    @Value("\${twilio.account.sid}")
    private lateinit var accountSID: String

    @Value("\${twilio.auth.token}")
    private lateinit var authToken: String

    @Value("\${twilio.phone-number}")
    private lateinit var fromNumber: String

    private lateinit var toNumber: String

    private val log = logger()

    @PostConstruct
    fun init() = Twilio.init(accountSID, authToken)

    override fun setSubscriberInfo(info: SubscriberInfo) {
        toNumber = info.phoneNumber!!
    }

    override fun send(data: Any) {
        val weatherImage = data as File
        val to = PhoneNumber(toNumber)
        val from = PhoneNumber(fromNumber)

        try {
            val message = Message.creator(to, from, "Here is Weather Forecast!")
                .setSendAsMms(true)
                .setMediaUrl(weatherImage.toURI())
                .create()
            if (message.status == Message.Status.FAILED) {
                log.error("Fail to send SMS: code=${message.errorCode}, message=${message.errorMessage}")
            }
            log.info("Success to send SMS: SID=${message.sid}, created=${message.dateCreated}, sent=${message.dateSent}")
        } catch (e: ApiException) {
            throw IllegalArgumentException(e.localizedMessage, e)
        }
    }

    override fun getHandlerName() = SubscriptionHandlerName.SMS
}