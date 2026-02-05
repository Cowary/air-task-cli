package org.cowary.service;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Produces;
import io.micronaut.core.annotation.*;
import io.micronaut.http.client.annotation.Client;
import org.cowary.air_task_cli.api.ProjectManagementApi;
import org.cowary.air_task_cli.api.WeeklyTaskManagementApi;
import org.cowary.air_task_cli.model.*;
import jakarta.annotation.Generated;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис для работы с Weekly задачами через API
 */
@Singleton
@Generated(value="org.openapitools.codegen.languages.JavaMicronautClientCodegen", comments = "Generator version: 7.18.0")
public class WeeklyService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeeklyService.class);
    
    @Inject
    @Client("${openapi-micronaut-client-base-path}")
    private WeeklyTaskManagementApi weeklyApi;
    
    @Inject
    @Client("${openapi-micronaut-client-base-path}")
    private ProjectManagementApi projectApi;
    
    /**
     * Создать новую Weekly задачу
     */
    public WeeklyResponse createWeeklyTask(WeeklyCreateRequest request) {
        try {
            logger.info("Создание Weekly задачи: {}", request.getName());
            ApiResponseWeeklyResponse response = weeklyApi.createWeeklyTask(request);
            
            if (response.getIsSuccess() != null && response.getIsSuccess()) {
                logger.info("Weekly задача успешно создана с ID: {}", response.getData().getId());
                return response.getData();
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Неизвестная ошибка";
                logger.error("Ошибка при создании Weekly задачи: {}", errorMsg);
                throw new RuntimeException("Ошибка при создании Weekly задачи: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Ошибка при создании Weekly задачи", e);
            throw new RuntimeException("Ошибка при создании Weekly задачи: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить список всех проектов
     */
    public List<ProjectResponse> getProjects() {
        try {
            logger.info("Получение списка проектов");
            ApiResponseProjectListResponse response = projectApi.getAllProjects();
            
            if (response.getIsSuccess() != null && response.getIsSuccess()) {
                List<ProjectResponse> projects = response.getData().getProjects();
                logger.info("Найдено {} проектов", projects != null ? projects.size() : 0);
                return projects != null ? projects : List.of();
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Неизвестная ошибка";
                logger.error("Ошибка при получении списка проектов: {}", errorMsg);
                throw new RuntimeException("Ошибка при получении списка проектов: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении списка проектов", e);
            throw new RuntimeException("Ошибка при получении списка проектов: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить Weekly задачи по проекту
     */
    public List<WeeklyResponse> getWeeklyTasksByProject(Long projectId) {
        try {
            logger.info("Получение Weekly задач для проекта ID: {}", projectId);
            ApiResponseListWeeklyResponse response = weeklyApi.getWeeklyTasksByProject(projectId);
            
            if (response.getIsSuccess() != null && response.getIsSuccess()) {
                List<WeeklyResponse> weeklyTasks = response.getData();
                logger.info("Найдено {} Weekly задач для проекта", weeklyTasks != null ? weeklyTasks.size() : 0);
                return weeklyTasks != null ? weeklyTasks : List.of();
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Неизвестная ошибка";
                logger.error("Ошибка при получении Weekly задач: {}", errorMsg);
                throw new RuntimeException("Ошибка при получении Weekly задач: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении Weekly задач", e);
            throw new RuntimeException("Ошибка при получении Weekly задач: " + e.getMessage(), e);
        }
    }
    
    /**
     * Найти проект по имени
     */
    public ProjectResponse findProjectByName(List<ProjectResponse> projects, String projectName) {
        return projects.stream()
            .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(projectName.trim()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Найти проект по ID
     */
    public ProjectResponse findProjectById(List<ProjectResponse> projects, Long projectId) {
        return projects.stream()
            .filter(p -> p.getId() != null && p.getId().equals(projectId))
            .findFirst()
            .orElse(null);
    }
}