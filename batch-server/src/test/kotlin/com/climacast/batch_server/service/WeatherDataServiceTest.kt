package com.climacast.batch_server.service

import com.climacast.batch_server.infra.data.ConvertedWeatherData
import com.climacast.batch_server.infra.repository.DailyWeatherRepository
import com.climacast.batch_server.infra.repository.HourlyWeatherRepository
import com.climacast.batch_server.model.dto.HourlyWeatherUpsertRequestDTO
import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.DailyWeatherData
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.HourlyWeatherData
import com.climacast.global.enums.WeatherStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class WeatherDataServiceMockTest {

    @InjectMocks
    private lateinit var weatherDataService: WeatherDataService
    @Mock
    private lateinit var dailyWeatherRepository: DailyWeatherRepository
    @Mock
    private lateinit var hourlyWeatherRepository: HourlyWeatherRepository

    @Nested
    @DisplayName("날씨 예보 데이터 저장")
    inner class SaveWeatherForecastDataTest {
        @Test
        @DisplayName("성공")
        fun success() {
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
    }

    @Nested
    @DisplayName("날씨 히스토리 데이터 저장")
    inner class SaveWeatherHistoryDataTest {
        @Test
        @DisplayName("성공")
        fun success() {
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
}

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeatherDataServiceJpaTest {

    @Autowired
    private lateinit var dailyWeatherRepository: DailyWeatherRepository

    @Autowired
    private lateinit var hourlyWeatherRepository: HourlyWeatherRepository

    private lateinit var weatherDataService: WeatherDataService

    @BeforeEach
    fun setUp() {
        weatherDataService = WeatherDataService(dailyWeatherRepository, hourlyWeatherRepository)
    }

    @Nested
    @DisplayName("날씨 예보 데이터 저장")
    inner class SaveWeatherForecastDataTest {
        @Test
        @DisplayName("성공")
        fun success() {
            // given
            val numOfRegions = 252
            val now = LocalDateTime.now()
            val weatherStatus = WeatherStatus.CLEAR.name

            val requests = mutableSetOf<HourlyWeatherUpsertRequestDTO>().apply {
                repeat(numOfRegions) {
                    add(HourlyWeatherUpsertRequestDTO(
                        parentRegion = "Region",
                        childRegion = "$it",
                        latitude = 123.000,
                        longitude = 456.000,
                        status = weatherStatus,
                        time = now,
                        weatherCode = 1,
                        temperature2m = 111.000,
                        temperature80m = 222.000,
                        temperature120m = 333.000,
                        temperature180m = 444.000,
                        windSpeed10m = 12.000,
                        windSpeed80m = 34.000,
                        windSpeed120m = 56.000,
                        windSpeed180m = 78.000,
                        humidity2m = 123
                    ))
                }
            }

            val weatherForecastData = ConvertedWeatherData.WeatherForecastData(requests)

            // when
            weatherDataService.saveWeatherForecastData(weatherForecastData)

            // then
            assertThatNoException()
            assertThat(hourlyWeatherRepository.findAll()).hasSize(numOfRegions)
        }
    }

    @Nested
    @DisplayName("날씨 히스토리 데이터 저장")
    inner class SaveWeatherHistoryDataTest {
        @Test
        @DisplayName("성공")
        fun success() {
            // given
            val numOfRegions = 252
            val days = 30
            val dataSize = numOfRegions * days
            val localDate = LocalDate.now()
            val localDateTime = LocalDateTime.now()
            val dailyWeatherData = mock(DailyWeatherData::class.java)
            val hourlyWeatherData = mock(HourlyWeatherData::class.java)

            val dailyWeathers = mutableSetOf<DailyWeather>().apply {
                repeat(dataSize) {
                    add(DailyWeather(
                        parentRegion = "Region",
                        childRegion = "$it",
                        latitude = 123.000,
                        longitude = 456.000,
                        status = WeatherStatus.CLEAR,
                        time = localDate,
                        data = dailyWeatherData
                    ))
                }
            }

            val hourlyWeathers = mutableSetOf<HourlyWeather>().apply {
                repeat(dataSize) {
                    add(HourlyWeather(
                        parentRegion = "Region",
                        childRegion = "$it",
                        latitude = 123.000,
                        longitude = 456.000,
                        status = WeatherStatus.CLEAR,
                        time = localDateTime,
                        data = hourlyWeatherData
                    ))
                }
            }

            val weatherHistoryData = ConvertedWeatherData.WeatherHistoryData(dailyWeathers, hourlyWeathers)

            // when
            weatherDataService.saveWeatherHistoryData(weatherHistoryData)

            // then
            assertThatNoException()
            assertThat(dailyWeatherRepository.findAll()).hasSize(dataSize)
            assertThat(hourlyWeatherRepository.findAll()).hasSize(dataSize)
        }
    }
}