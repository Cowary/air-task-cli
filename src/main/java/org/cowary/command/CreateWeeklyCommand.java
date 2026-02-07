package org.cowary.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.cowary.air_task_cli.model.WeeklyCreateRequest;
import org.cowary.air_task_cli.model.WeeklyResponse;
import org.cowary.service.WeeklyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * Команда для создания Weekly задач
 */
@Command(
    name = "create-weekly",
    description = "Создать новую Weekly задачу для проекта",
    mixinStandardHelpOptions = true
)
@Introspected
@Singleton
public class CreateWeeklyCommand implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateWeeklyCommand.class);
    
    @Inject
    private WeeklyService weeklyService;
    
    @Option(
        names = {"-n", "--name"},
        description = "Название Weekly задачи",
        required = true
    )
    private String name;
    
    @Option(
        names = {"-c", "--count"},
        description = "Количество (по умолчанию: 1)",
        defaultValue = "1"
    )
    private Integer count;
    
    @Option(
        names = {"-p", "--project"},
        description = "Название проекта (если не указано, будет предложен выбор)"
    )
    private String projectName;
    
    @Option(
        names = {"--project-id"},
        description = "ID проекта (если не указано, будет предложен выбор)"
    )
    private Long projectId;
    
    @Option(
        names = {"--priority"},
        description = "Приоритет: HIGH, MIDDLE, LOW (по умолчанию: MIDDLE)",
        defaultValue = "MIDDLE"
    )
    private String priorityStr;
    
    @Option(
        names = {"--status"},
        description = "Статус: IN_PROGRESS, DONE (по умолчанию: IN_PROGRESS)",
        defaultValue = "IN_PROGRESS"
    )
    private String statusStr;
    
    @Option(
        names = {"-i", "--interactive"},
        description = "Интерактивный режим (будет запрошен ввод всех параметров)"
    )
    private boolean interactive;
    
    @Override
    public Integer call() throws Exception {
        try {
            if (interactive) {
                return runInteractiveMode();
            } else {
                return runCommandLineMode();
            }
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды создания Weekly задачи", e);
            System.err.println("❌ Ошибка: " + e.getMessage());
            return 1;
        }
    }
    
    private Integer runInteractiveMode() {
        System.out.println("🔧 Создание Weekly задачи (интерактивный режим)\n");
        
        Scanner scanner = new Scanner(System.in);
        
        // Ввод названия
        if (name == null || name.trim().isEmpty()) {
            System.out.print("📝 Введите название Weekly задачи: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.err.println("❌ Название не может быть пустым");
                return 1;
            }
        }
        
        // Ввод количества
        if (count == null) {
            System.out.print("🔢 Введите количество (по умолчанию 1): ");
            String countStr = scanner.nextLine().trim();
            try {
                count = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                System.err.println("❌ Некорректное количество. Используется значение по умолчанию: 1");
                count = 1;
            }
        }
        
        // Выбор проекта
        Long selectedProjectId = projectId;
        String selectedProjectName = projectName;
        
        if (selectedProjectId == null && (selectedProjectName == null || selectedProjectName.trim().isEmpty())) {
            System.out.println("\n📋 Доступные проекты:");
            List<org.cowary.air_task_cli.model.ProjectResponse> projects = weeklyService.getProjects();
            
            if (projects.isEmpty()) {
                System.err.println("❌ Нет доступных проектов");
                return 1;
            }
            
            for (int i = 0; i < projects.size(); i++) {
                org.cowary.air_task_cli.model.ProjectResponse project = projects.get(i);
                System.out.printf("  %d. %s (ID: %d)%n", i + 1, project.getName(), project.getId());
            }
            
            System.out.print("Выберите номер проекта: ");
            String projectChoice = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(projectChoice);
                if (choice >= 1 && choice <= projects.size()) {
                    org.cowary.air_task_cli.model.ProjectResponse selectedProject = projects.get(choice - 1);
                    selectedProjectId = selectedProject.getId();
                    selectedProjectName = selectedProject.getName();
                } else {
                    System.err.println("❌ Неверный номер проекта");
                    return 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Некорректный номер проекта");
                return 1;
            }
        }
        
        // Выбор приоритета
        if (priorityStr == null || priorityStr.trim().isEmpty()) {
            System.out.println("\n⚡ Выберите приоритет:");
            System.out.println("  1. HIGH (высокий)");
            System.out.println("  2. MIDDLE (средний)");
            System.out.println("  3. LOW (низкий)");
            System.out.print("Выберите номер приоритета (по умолчанию 2): ");
            String priorityChoice = scanner.nextLine().trim();
            
            switch (priorityChoice) {
                case "1" -> priorityStr = "HIGH";
                case "2" -> priorityStr = "MIDDLE";
                case "3" -> priorityStr = "LOW";
                default -> priorityStr = "MIDDLE";
            }
        }
        
        // Выбор статуса
        if (statusStr == null || statusStr.trim().isEmpty()) {
            System.out.println("\n📊 Выберите статус:");
            System.out.println("  1. IN_PROGRESS (в работе)");
            System.out.println("  2. DONE (выполнено)");
            System.out.print("Выберите номер статуса (по умолчанию 1): ");
            String statusChoice = scanner.nextLine().trim();
            
            switch (statusChoice) {
                case "1" -> statusStr = "IN_PROGRESS";
                case "2" -> statusStr = "DONE";
                default -> statusStr = "IN_PROGRESS";
            }
        }
        
        // Создание Weekly задачи
        return createWeeklyTask(selectedProjectId, selectedProjectName);
    }
    
    private Integer runCommandLineMode() {
        // Валидация обязательных параметров
        if (name == null || name.trim().isEmpty()) {
            System.err.println("❌ Название Weekly задачи обязательно для указания");
            return 1;
        }
        
        // Определение проекта
        Long selectedProjectId = projectId;
        String selectedProjectName = projectName;
        
        if (selectedProjectId == null && (selectedProjectName == null || selectedProjectName.trim().isEmpty())) {
            System.err.println("❌ Необходимо указать проект (--project или --project-id)");
            return 1;
        }
        
        return createWeeklyTask(selectedProjectId, selectedProjectName);
    }
    
    private Integer createWeeklyTask(Long projectId, String projectName) {
        try {
            // Создание запроса
            WeeklyCreateRequest request = new WeeklyCreateRequest()
                .name(name.trim())
                .count(count != null ? count : 1)
                .projectId(projectId);
            
            // Установка приоритета
            try {
                WeeklyCreateRequest.PriorityEnum priority = WeeklyCreateRequest.PriorityEnum.valueOf(priorityStr.toUpperCase());
                request.priority(priority);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Некорректный приоритет. Используется MIDDLE");
                request.priority(WeeklyCreateRequest.PriorityEnum.MIDDLE);
            }
            
            // Установка статуса
            try {
                WeeklyCreateRequest.StatusEnum status = WeeklyCreateRequest.StatusEnum.valueOf(statusStr.toUpperCase());
                request.status(status);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Некорректный статус. Используется IN_PROGRESS");
                request.status(WeeklyCreateRequest.StatusEnum.IN_PROGRESS);
            }
            
            System.out.println("🔄 Создание Weekly задачи...");
            
            // Создание Weekly задачи
            WeeklyResponse weekly = weeklyService.createWeeklyTask(request);
            
            // Вывод результата
            System.out.println("\n✅ Weekly задача успешно создана!");
            System.out.println("═".repeat(50));
            System.out.printf("📋 ID: %d%n", weekly.getId());
            System.out.printf("📝 Название: %s%n", weekly.getName());
            System.out.printf("🔢 Количество: %d%n", weekly.getCount());
            System.out.printf("🏢 Проект: %s (ID: %d)%n", projectName, projectId);
            System.out.printf("⚡ Приоритет: %s%n", weekly.getPriority());
            System.out.printf("📊 Статус: %s%n", weekly.getStatus());
            if (weekly.getCreatedTs() != null) {
                System.out.printf("🕒 Создано: %s%n", weekly.getCreatedTs());
            }
            System.out.println("═".repeat(50));
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании Weekly задачи: " + e.getMessage());
            logger.error("Ошибка при создании Weekly задачи", e);
            return 1;
        }
    }
}