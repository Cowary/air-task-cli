package org.cowary.service;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class TimerService {

    public Boolean processTimer(int minute) {
        int durationInSeconds = minute * 60;
        System.out.println("Starting countdown for " + durationInSeconds + " seconds...");
        // Запускаем таймер в отдельном потоке, чтобы не блокировать основной поток,
        // если Picocli/Micronaut ожидают завершения `run()` быстро.
        // Однако, если это простое CLI приложение, можно выполнить цикл здесь.
        for (int i = durationInSeconds; i >= 0; i--) {
            // \r возвращает каретку в начало строки
            // \033[2K очищает строку
            System.out.print("\r\033[2KTime remaining: " + i + " seconds");
            System.out.flush(); // Важно сбросить буфер, чтобы сразу увидеть изменения
            try {
                TimeUnit.SECONDS.sleep(1); // Ждём 1 секунду
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Выходим из цикла, если поток прерван
            }
        }
        System.out.println("\nTimer finished!"); // Перевод строки после окончания    }
        return Boolean.TRUE;
    }
}
