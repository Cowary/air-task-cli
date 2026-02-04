package org.cowary.config;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Singleton
@SerdeImport(LocalDateTime.class)
public class FlexibleLocalDateTimeDeserializer implements Deserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true) // 0-9 цифр дробной части
            .optionalEnd()
            .toFormatter();

    @Override
    public LocalDateTime deserialize(Decoder decoder, DecoderContext context, Argument<? super LocalDateTime> type) throws IOException, IOException {
        String value = decoder.decodeString();
        return LocalDateTime.parse(value, FORMATTER);
    }
}
