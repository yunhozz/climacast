package com.climacast.batch_server.service

import com.climacast.batch_server.infra.data.ConvertedWeatherData
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.repository.DailyWeatherRepository
import com.climacast.batch_server.model.repository.HourlyWeatherRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anySet
import org.mockito.BDDMockito.doNothing
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class WeatherDataServiceTest {
    @InjectMocks
    private lateinit var weatherDataService: WeatherDataService
    @Mock
    private lateinit var dailyWeatherRepository: DailyWeatherRepository
    @Mock
    private lateinit var hourlyWeatherRepository: HourlyWeatherRepository

    @Test
    fun saveWeatherForecastData() {
        // given
        val weatherForecastData = mock(ConvertedWeatherData.WeatherForecastData::class.java)

        doNothing()
            .`when`(hourlyWeatherRepository).upsertHourlyWeatherForecasts(anySet())

        // when
        weatherDataService.saveWeatherForecastData(weatherForecastData)

        // then
        then(hourlyWeatherRepository).should(times(1))
            .upsertHourlyWeatherForecasts(weatherForecastData.hourlyWeatherUpsertDTOs)
    }

    @Test
    fun saveWeatherHistoryData() {
        // given
        val weatherHistoryData = mock(ConvertedWeatherData.WeatherHistoryData::class.java)
        val dailyWeather = mock(DailyWeather::class.java)
        val hourlyWeather = mock(HourlyWeather::class.java)

        given(dailyWeatherRepository.saveAll(anySet()))
            .willReturn(listOf(dailyWeather))
        given(hourlyWeatherRepository.saveAll(anySet()))
            .willReturn(listOf(hourlyWeather))

        // when
        weatherDataService.saveWeatherHistoryData(weatherHistoryData)

        // then
        then(dailyWeatherRepository).should(times(1))
            .saveAll(weatherHistoryData.dailyWeathers)
        then(hourlyWeatherRepository).should(times(1))
            .saveAll(weatherHistoryData.hourlyWeathers)
    }
}