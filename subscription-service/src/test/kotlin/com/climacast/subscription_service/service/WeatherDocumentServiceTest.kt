package com.climacast.subscription_service.service

import com.climacast.global.enums.WeatherType
import com.climacast.subscription_service.infra.repository.ForecastWeatherSearchRepository
import com.climacast.subscription_service.infra.repository.HistoryWeatherSearchRepository
import com.climacast.subscription_service.model.document.ForecastWeather
import com.climacast.subscription_service.model.document.HistoryWeather
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyList
import org.mockito.BDDMockito.doNothing
import org.mockito.BDDMockito.mock
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.then
import org.mockito.kotlin.times

@ExtendWith(MockitoExtension::class)
class WeatherDocumentServiceTest {
    @InjectMocks
    private lateinit var weatherDocumentService: WeatherDocumentService
    @Mock
    private lateinit var forecastWeatherSearchRepository: ForecastWeatherSearchRepository
    @Mock
    private lateinit var historyWeatherSearchRepository: HistoryWeatherSearchRepository

    @Test
    fun saveForecastWeathers() {
        // given
        val forecastWeather = mock(ForecastWeather::class.java)

        doNothing()
            .`when`(forecastWeatherSearchRepository).upsertWeatherDocuments(anyList(), any())

        // when
        weatherDocumentService.saveForecastWeathers(listOf(forecastWeather))

        // then
        then(forecastWeatherSearchRepository).should(times(1))
            .upsertWeatherDocuments(listOf(forecastWeather), WeatherType.FORECAST)
    }

    @Test
    fun saveHistoryWeathers() {
        // given
        val historyWeather = mock(HistoryWeather::class.java)

        doNothing()
            .`when`(historyWeatherSearchRepository).upsertWeatherDocuments(anyList(), any())

        // when
        weatherDocumentService.saveHistoryWeathers(listOf(historyWeather))

        // then
        then(historyWeatherSearchRepository).should(times(1))
            .upsertWeatherDocuments(listOf(historyWeather), WeatherType.HISTORY)
    }
}