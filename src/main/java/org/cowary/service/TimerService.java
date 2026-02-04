package org.cowary.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cowary.air_task_cli.api.PlankaProjectControllerApi;
import org.cowary.air_task_cli.api.PlankaTaskControllerApi;
import org.cowary.air_task_cli.api.ProjectManagementApi;
import org.cowary.air_task_cli.model.ProjectCreateRequest;
import org.cowary.air_task_cli.model.ApiResponseProjectResponse;
import org.cowary.air_task_cli.model.BoardRs;
import org.cowary.air_task_cli.model.ProjectListDtoRs;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TimerService {
    @Inject
    private PlankaProjectControllerApi projectControllerApiClient;
    @Inject
    public PlankaTaskControllerApi taskControllerApi;
    @Inject
    private ProjectManagementApi projectManagementApi;

    public ProjectListDtoRs getAllProjects() {
        System.out.println(projectControllerApiClient.getClass());
        return projectControllerApiClient.getProjects().getData();
    }

    public List<BoardRs> getAllTaskByProjectId(final Long projectId) {
        return taskControllerApi.taskByProject(projectId);
    }

    public Boolean updateTime(final Long taskId, final Long time) {
        return taskControllerApi.updateTime(taskId, time);
    }

    /**
     * Создает новый проект через API
     * @param name название проекта
     * @param status статус проекта
     * @param priority приоритет проекта
     * @return результат создания проекта
     */
    public ApiResponseProjectResponse createProject(String name,
                                                   ProjectCreateRequest.StatusEnum status,
                                                   ProjectCreateRequest.PriorityEnum priority) {
        ProjectCreateRequest request = new ProjectCreateRequest()
                .name(name)
                .status(status)
                .priority(priority);

        return projectManagementApi.createProject(request);
    }

    public long processTimer(long minute) {
        long durationInSeconds = minute * 60;
        System.out.println("Starting countdown for " + durationInSeconds + " seconds...");
        // Запускаем таймер в отдельном потоке, чтобы не блокировать основной поток,
        // если Picocli/Micronaut ожидают завершения `run()` быстро.
        // Однако, если это простое CLI приложение, можно выполнить цикл здесь.
        for (long i = durationInSeconds; i >= 0; i--) {
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
        Toolkit.getDefaultToolkit().beep();
        System.out.println("\nTimer finished!"); // Перевод строки после окончания    }
        return minute;
    }
}
