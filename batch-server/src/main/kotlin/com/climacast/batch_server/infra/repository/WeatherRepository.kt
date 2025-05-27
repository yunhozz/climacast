package com.climacast.batch_server.infra.repository

import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.Weather
import com.climacast.batch_server.model.entity.WeatherId
import org.springframework.data.jpa.repository.JpaRepository

interface WeatherRepository<T: Weather>: JpaRepository<T, WeatherId>

interface HourlyWeatherRepository: WeatherRepository<HourlyWeather>, WeatherJdbcRepository

interface DailyWeatherRepository: WeatherRepository<DailyWeather>