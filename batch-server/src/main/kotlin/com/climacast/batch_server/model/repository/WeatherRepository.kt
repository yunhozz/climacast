package com.climacast.batch_server.model.repository

import com.climacast.batch_server.model.entity.DailyWeather
import com.climacast.batch_server.model.entity.HourlyWeather
import com.climacast.batch_server.model.entity.Weather
import org.springframework.data.jpa.repository.JpaRepository

interface WeatherRepository<T: Weather>: JpaRepository<T, Long>

interface HourlyWeatherRepository: WeatherRepository<HourlyWeather>, WeatherJdbcRepository

interface DailyWeatherRepository: WeatherRepository<DailyWeather>