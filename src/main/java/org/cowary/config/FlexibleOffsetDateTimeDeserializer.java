package org.cowary.config;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.annotation.SerdeImport;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Singleton
@SerdeImport(OffsetDateTime.class)
public class FlexibleOffsetDateTimeDeserializer implements Deserializer<OffsetDateTime> {

    private static final DateTimeFormatter FORMATTER_WITHOUT_OFFSET = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true) // 0-9 цифр дробной части
            .optionalEnd()
            .toFormatter();

    private static final DateTimeFormatter FORMATTER_WITH_OFFSET = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .optionalStart()
            .appendOffset("+HH:MM", "Z")
            .optionalEnd()
            .toFormatter();

    @Override
    public OffsetDateTime deserialize(Decoder decoder, DecoderContext context, Argument<? super OffsetDateTime> type) throws IOException {
        String value = decoder.decodeString();

        try {
            // Попытка распарсить с смещением
            return OffsetDateTime.parse(value, FORMATTER_WITH_OFFSET);
        } catch (Exception e) {
            // Если смещения нет, парсим как LocalDateTime и добавляем смещение +00:00
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(value, FORMATTER_WITHOUT_OFFSET);
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
    }
}
