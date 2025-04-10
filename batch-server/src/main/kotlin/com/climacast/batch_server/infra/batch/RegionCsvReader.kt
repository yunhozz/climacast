package com.climacast.batch_server.infra.batch

import com.climacast.batch_server.model.dto.Region
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

@Component
class RegionCsvReader : FlatFileItemReader<Region>() {

    init {
        val regionFields = Region::class.memberProperties
            .map { it.name }
            .toTypedArray()
        val lineMapper = DefaultLineMapper<Region>().apply {
            setLineTokenizer(DelimitedLineTokenizer(",").apply {
                setNames(*regionFields)
            })
            setFieldSetMapper { fields ->
                Region(
                    fields.readString(regionFields[0]),
                    fields.readString(regionFields[1]),
                    fields.readDouble(regionFields[2]),
                    fields.readDouble(regionFields[3])
                )
            }
        }
        this.setResource(ClassPathResource(BatchConstants.CSV_PATH))
        this.setLineMapper(lineMapper)
    }
}