package com.climacast.batch_server.config

import com.climacast.batch_server.es.document.Region
import com.climacast.batch_server.es.repository.RegionRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files

@Configuration
class RegionInitConfig(
    private val regionRepository: RegionRepository
) {

    companion object {
        private const val CSV_PATH = "static/region-list.csv"
    }

    @PostConstruct
    fun initRegions() {
        val resource = ClassPathResource(CSV_PATH)
        val regions = arrayListOf<Region>()

        Files.lines(resource.file.toPath()).forEach { line ->
            line.split(",").let {
                val region = Region(
                    parent = it[0],
                    child = it[1],
                    nx = it[2],
                    ny = it[3],
                )
                regions.add(region)
            }
        }

        regionRepository.saveAll(regions)
    }
}