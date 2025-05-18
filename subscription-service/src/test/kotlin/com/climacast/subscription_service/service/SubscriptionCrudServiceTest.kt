package com.climacast.subscription_service.service

import com.climacast.subscription_service.common.enums.SubscriptionMethod
import com.climacast.subscription_service.common.exception.SubscriptionServiceException
import com.climacast.subscription_service.infra.repository.SubscriptionRepository
import com.climacast.subscription_service.model.dto.SubscriptionCreateRequestDTO
import com.climacast.subscription_service.model.entity.Subscription
import com.climacast.subscription_service.model.entity.SubscriptionInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@ExtendWith(MockitoExtension::class)
class SubscriptionCrudServiceMockTest {

    @InjectMocks
    private lateinit var subscriptionCrudService: SubscriptionCrudService

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Nested
    @DisplayName("구독 생성")
    inner class CreateSubscriptionTest {
        @Test
        @DisplayName("성공")
        fun success() {
            // given
            val subscriptionInfo = SubscriptionInfo(subscriptionCreateRequestDTO.email, subscriptionCreateRequestDTO.phoneNumber)

            given(subscriptionRepository.existsBySubscriptionInfoEmailAndStatus(subscriptionInfo.email!!, true))
                .willReturn(false)

            given(subscriptionRepository.save(any<Subscription>()))
                .willReturn(subscriptionCreateRequestDTO.toEntity())

            // when
            subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)

            // then
            then(subscriptionRepository).should(times(1))
                .existsBySubscriptionInfoEmailAndStatus(subscriptionInfo.email!!, true)
            then(subscriptionRepository).should(times(1)).save(any<Subscription>())
        }

        @Test
        @DisplayName("실패: 중복 불가")
        fun fail() {
            // given
            val subscriptionInfo = SubscriptionInfo(subscriptionCreateRequestDTO.email, subscriptionCreateRequestDTO.phoneNumber)

            given(subscriptionRepository.existsBySubscriptionInfoEmailAndStatus(subscriptionInfo.email!!, true))
                .willReturn(true)

            // when
            assertThrows<SubscriptionServiceException.SubscriptionAlreadyExistException> {
                subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)
            }

            // then
            then(subscriptionRepository).should(times(1))
                .existsBySubscriptionInfoEmailAndStatus(subscriptionInfo.email!!, true)
            then(subscriptionRepository).should(never()).save(any())
        }
    }
}

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubscriptionCrudServiceJpaTest {

    private lateinit var subscriptionCrudService: SubscriptionCrudService

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @BeforeEach
    fun setUp() {
        subscriptionCrudService = SubscriptionCrudService(subscriptionRepository)
    }

    @Nested
    @DisplayName("구독 생성")
    inner class CreateSubscription {
        @Test
        @DisplayName("성공")
        fun success() {
            // given

            // when
            val subscriptionId = subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)
            val savedSubscription = subscriptionRepository.getReferenceById(subscriptionId!!)

            // then
            assertThatNoException()
            assertThat(subscriptionId).isNotNull()
            assertThat(savedSubscription.regions).containsOnly(*subscriptionCreateRequestDTO.regions.toTypedArray())
            assertThat(savedSubscription.method).isEqualTo(SubscriptionMethod.of(subscriptionCreateRequestDTO.method))
        }

        @Test
        @DisplayName("실패: 중복 불가")
        fun fail() {
            // given

            // when
            val createSubscription: (Int) -> Unit = {
                subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)
            }

            // then
            assertThatThrownBy { repeat(2, createSubscription) }
            assertThat(subscriptionRepository.findAll()).singleElement()
        }
    }

    @Nested
    @DisplayName("구독 취소")
    inner class CancelSubscription {
        @Test
        @DisplayName("성공")
        fun success() {
            // given
            val email = subscriptionCreateRequestDTO.email
            val phoneNumber = subscriptionCreateRequestDTO.phoneNumber

            // when
            val subscriptionId = subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)!!
            subscriptionCrudService.cancelSubscriptionByEmailAndPhoneNumber(email, phoneNumber)

            // then
            assertThatNoException()
            assertThat(subscriptionRepository.getReferenceById(subscriptionId).status).isFalse()
        }

        @Test
        @DisplayName("실패: 재취소 불가")
        fun fail() {
            // given
            val email = subscriptionCreateRequestDTO.email
            val phoneNumber = subscriptionCreateRequestDTO.phoneNumber

            // when
            val subscriptionId = subscriptionCrudService.createSubscription(subscriptionCreateRequestDTO)!!

            val subscription = subscriptionRepository.getReferenceById(subscriptionId)
            subscription.cancelByUserRequest() // already canceled

            // then
            assertThat(subscription.status).isFalse()
            assertThatThrownBy {
                subscriptionCrudService.cancelSubscriptionByEmailAndPhoneNumber(email, phoneNumber)
            }
        }
    }
}

private val subscriptionCreateRequestDTO = SubscriptionCreateRequestDTO(
    email = "test@example.com",
    phoneNumber = null,
    regions = setOf("region1", "region2", "region3"),
    weatherType = "날씨 예보",
    interval = "30분",
    method = "이메일"
)