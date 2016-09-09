package ru.wutiarn.edustor.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.format.Formatter
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.time.LocalDate
import java.util.*

@Configuration
open class CustomConversionUtils() : WebMvcConfigurerAdapter() {
    @Autowired
    fun registerCustomSerializers(mapper: ObjectMapper) {
        val module = SimpleModule("ru.edustor.datatype.custom", Version(1, 0, 0, null, "ru.edustor", "edustor"))
        module.addSerializer(LocalDateJsonSerializer())
        module.addDeserializer(LocalDate::class.java, LocalDateJsonDeserializer())
        mapper.registerModule(module)
    }

    private class LocalDateJsonSerializer : StdScalarSerializer<LocalDate>(LocalDate::class.java) {
        override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeNumber(value.toEpochDay())
        }
    }

    private class LocalDateJsonDeserializer : StdScalarDeserializer<LocalDate>(LocalDate::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): LocalDate {
            return LocalDate.ofEpochDay(p.longValue)
        }
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addFormatter(LocalDateFormatter())
    }

    private class LocalDateFormatter : Formatter<LocalDate> {
        override fun parse(text: String, locale: Locale?): LocalDate {
            return LocalDate.ofEpochDay(text.toLong())
        }

        override fun print(`object`: LocalDate, locale: Locale?): String {
            return `object`.toEpochDay().toString()
        }
    }
}

