package org.cowary.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Inject;
import org.cowary.air_task_cli.model.WeeklyResponse;
import org.cowary.service.WeeklyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

/**
 * Команда для просмотра Weekly задач проекта
 */
@Command(
    name = "list-weekly",
    description = "Просмотр Weekly задач проекта",
    mixinStandardHelpOptions = true
)
@Introspected
public class ListWeeklyCommand implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(ListWeeklyCommand.class);
    
    @Inject
    private WeeklyService weeklyService;
    
    @Option(
        names = {"-p", "--project"},
        description = "Название проекта (если не указано, будет показана информация по всем проектам)"
    )
    private String projectName;
    
    @Option(
        names = {"--project-id"},
        description = "ID проекта (если не указано, будет показана информация по всем проектам)"
    )
    private Long projectId;
    
    @Option(
        names = {"-s", "--status"},
        description = "Фильтр по статусу: IN_PROGRESS, DONE"
    )
    private String statusFilter;
    
    @Option(
        names = {"-t", "--table"},
        description = "Отображать в виде таблицы (по умолчанию: true)",
        defaultValue = "true"
    )
    private boolean useTable;
    
    @Override
    public void run() {
        try {
            listWeeklyTasks();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды просмотра Weekly задач", e);
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
    }
    
    private void listWeeklyTasks() {
        System.out.println("📋 Список Weekly задач\n");
        
        try {
            if (projectId != null || projectName != null) {
                // Получение Weekly задач для конкретного проекта
                Long targetProjectId = projectId;
                
                if (targetProjectId == null && projectName != null) {
                    List<org.cowary.air_task_cli.model.ProjectResponse> projects = weeklyService.getProjects();
                    org.cowary.air_task_cli.model.ProjectResponse project = weeklyService.findProjectByName(projects, projectName);
                    
                    if (project == null) {
                        System.err.println("❌ Проект с названием '" + projectName + "' не найден");
                        return;
                    }
                    
                    targetProjectId = project.getId();
                    System.out.printf("📁 Проект: %s (ID: %d)%n%n", project.getName(), project.getId());
                } else {
                    System.out.printf("📁 Проект ID: %d%n%n", targetProjectId);
                }
                
                List<WeeklyResponse> weeklyTasks = weeklyService.getWeeklyTasksByProject(targetProjectId);
                displayWeeklyTasks(weeklyTasks, "Weekly задачи проекта");
                
            } else {
                // Получение Weekly задач по всем проектам
                System.out.println("📊 Сводка по всем проектам:\n");
                List<org.cowary.air_task_cli.model.ProjectResponse> projects = weeklyService.getProjects();
                
                if (projects.isEmpty()) {
                    System.out.println("📭 Нет доступных проектов");
                    return;
                }
                
                for (org.cowary.air_task_cli.model.ProjectResponse project : projects) {
                    System.out.printf("📁 %s (ID: %d)%n", project.getName(), project.getId());
                    List<WeeklyResponse> weeklyTasks = weeklyService.getWeeklyTasksByProject(project.getId());
                    displayWeeklyTasks(weeklyTasks, "  Weekly задачи");
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении Weekly задач: " + e.getMessage());
            logger.error("Ошибка при получении Weekly задач", e);
        }
    }
    
    private void displayWeeklyTasks(List<WeeklyResponse> weeklyTasks, String title) {
        if (weeklyTasks.isEmpty()) {
            System.out.println("  📭 Нет Weekly задач");
            return;
        }
        
        // Фильтрация по статусу если указан
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            weeklyTasks = weeklyTasks.stream()
                .filter(task -> task.getStatus() != null && 
                    task.getStatus().toString().equalsIgnoreCase(statusFilter.trim()))
                .collect(Collectors.toList());
            
            if (weeklyTasks.isEmpty()) {
                System.out.println("  📭 Нет Weekly задач со статусом '" + statusFilter + "'");
                return;
            }
        }
        
        if (useTable) {
            displayAsTable(weeklyTasks, title);
        } else {
            displayAsList(weeklyTasks, title);
        }
    }
    
    private void displayAsTable(List<WeeklyResponse> weeklyTasks, String title) {
        System.out.println("  " + title + ":");
        System.out.println("  " + "═".repeat(70));
        
        // Подсчет статистики
        long inProgressCount = weeklyTasks.stream()
            .filter(task -> "IN_PROGRESS".equals(task.getStatus().toString()))
            .count();
            
        long doneCount = weeklyTasks.stream()
            .filter(task -> "DONE".equals(task.getStatus().toString()))
            .count();
            
        System.out.printf("  📊 Всего: %d | В работе: %d | Выполнено: %d%n", 
            weeklyTasks.size(), inProgressCount, doneCount);
        System.out.println("  " + "═".repeat(70));
        
        // Создание данных для таблицы
        Object[][] data = new Object[weeklyTasks.size()][6];

        for (int i = 0; i < weeklyTasks.size(); i++) {
            WeeklyResponse task = weeklyTasks.get(i);
            data[i][0] = task.getId();
            data[i][1] = task.getName();
            data[i][2] = task.getCount();
            data[i][3] = task.getPriority();
            data[i][4] = task.getStatus();
            data[i][5] = formatDate(task.getCreatedTs());
        }

        // Создание таблицы в текстовом формате
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append(String.format("  %-6s | %-30s | %-10s | %-10s | %-10s | %-20s%n",
            "ID", "Название", "Количество", "Приоритет", "Статус", "Создано"));
        tableBuilder.append("  ").append("-".repeat(80)).append("\n");

        for (Object[] row : data) {
            tableBuilder.append(String.format("  %-6s | %-30s | %-10s | %-10s | %-10s | %-20s%n",
                row[0], row[1], row[2], row[3], row[4], row[5]));
        }
        
        // Отображение таблицы
        System.out.println(tableBuilder.toString());
    }
    
    private void displayAsList(List<WeeklyResponse> weeklyTasks, String title) {
        System.out.println("  " + title + ":");
        System.out.println("  " + "═".repeat(70));
        
        for (WeeklyResponse task : weeklyTasks) {
            System.out.printf("  📋 ID: %d%n", task.getId());
            System.out.printf("  📝 Название: %s%n", task.getName());
            System.out.printf("  🔢 Количество: %d%n", task.getCount());
            System.out.printf("  ⚡ Приоритет: %s%n", task.getPriority());
            System.out.printf("  📊 Статус: %s%n", task.getStatus());
            if (task.getCreatedTs() != null) {
                System.out.printf("  🕒 Создано: %s%n", formatDate(task.getCreatedTs()));
            }
            System.out.println("  " + "-".repeat(50));
        }
        System.out.println("  " + "═".repeat(70));
    }
    
    private String formatDate(java.time.OffsetDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}