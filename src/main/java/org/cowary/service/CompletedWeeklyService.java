package org.cowary.service;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Produces;
import io.micronaut.core.annotation.*;
import io.micronaut.http.client.annotation.Client;
import org.cowary.air_task_cli.api.CompletedWeeklyTaskManagementApi;
import org.cowary.air_task_cli.model.*;
import jakarta.annotation.Generated;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис для работы с завершенными недельными задачами через API
 */
@Singleton
@Generated(value="org.openapitools.codegen.languages.JavaMicronautClientCodegen", comments = "Generator version: 7.18.0")
public class CompletedWeeklyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CompletedWeeklyService.class);
    
    @Inject
    @Client("${openapi-micronaut-client-base-path}")
    private CompletedWeeklyTaskManagementApi completedWeeklyApi;
    
    /**
     * Получить статистику недельных задач за текущую неделю
     */
    public WeeklyTaskStatisticsResponse getWeeklyTaskStatistics() {
        try {
            logger.info("Получение статистики недельных задач за текущую неделю");
            ApiResponseWeeklyTaskStatisticsResponse response = completedWeeklyApi.getWeeklyTaskStatistics();
            
            if (response.getIsSuccess() != null && response.getIsSuccess()) {
                logger.info("Статистика успешно получена");
                return response.getData();
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Неизвестная ошибка";
                logger.error("Ошибка при получении статистики: {}", errorMsg);
                throw new RuntimeException("Ошибка при получении статистики: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики недельных задач", e);
            throw new RuntimeException("Ошибка при получении статистики недельных задач: " + e.getMessage(), e);
        }
    }
    
    /**
     * Создать запись о завершенной недельной задаче
     */
    public CompletedWeeklyResponse createCompletedWeeklyTask(Long weeklyEntityId) {
        try {
            logger.info("Создание записи о завершенной недельной задаче для entity ID: {}", weeklyEntityId);
            
            CompletedWeeklyCreateRequest request = new CompletedWeeklyCreateRequest();
            request.setWeeklyEntityId(weeklyEntityId);
            request.completedDate(OffsetDateTime.now());
            
            ApiResponseCompletedWeeklyResponse response = completedWeeklyApi.createCompletedWeeklyTask(request);
            
            if (response.getIsSuccess() != null && response.getIsSuccess()) {
                logger.info("Запись о завершенной задаче успешно создана с ID: {}", response.getData().getId());
                return response.getData();
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Неизвестная ошибка";
                logger.error("Ошибка при создании записи о завершенной задаче: {}", errorMsg);
                throw new RuntimeException("Ошибка при создании записи о завершенной задаче: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Ошибка при создании записи о завершенной недельной задаче", e);
            throw new RuntimeException("Ошибка при создании записи о завершенной недельной задаче: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить список не завершенных задач (incomplete tasks)
     */
    public List<WeeklyWithCompletionStatus> getIncompleteTasks() {
        WeeklyTaskStatisticsResponse statistics = getWeeklyTaskStatistics();
        return statistics.getIncompleteTasks() != null ? statistics.getIncompleteTasks() : List.of();
    }
    
    /**
     * Получить список завершенных задач (completed tasks)
     */
    public List<WeeklyWithCompletionStatus> getCompletedTasks() {
        WeeklyTaskStatisticsResponse statistics = getWeeklyTaskStatistics();
        return statistics.getCompletedTasks() != null ? statistics.getCompletedTasks() : List.of();
    }
}