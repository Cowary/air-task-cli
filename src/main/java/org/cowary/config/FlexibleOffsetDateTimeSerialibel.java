package org.cowary.config;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serializer;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

//@Singleton
//@SerdeImport(OffsetDateTime.class)
//public class FlexibleOffsetDateTimeSerialibel implements Serializer<OffsetDateTime> {
//
//    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//
//    @Override
//    public void serialize(@NonNull Encoder encoder, @NonNull EncoderContext context, @NonNull Argument<? extends OffsetDateTime> type, @NonNull OffsetDateTime value) throws IOException {
//        // Преобразуем OffsetDateTime в LocalDateTime (отбрасываем смещение)
//        String formatted = value.toLocalDateTime().format(LOCAL_DATE_TIME_FORMATTER);
//        encoder.encodeString(formatted);
//    }
//}
