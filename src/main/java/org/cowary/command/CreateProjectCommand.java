package org.cowary.command;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Inject;
import org.cowary.air_task_cli.api.ProjectManagementApi;
import org.cowary.air_task_cli.model.ApiResponseProjectResponse;
import org.cowary.air_task_cli.model.ProjectCreateRequest;
import org.cowary.service.TimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "create-project", 
         description = "Создать новый проект",
         mixinStandardHelpOptions = true)
@Introspected
@ReflectiveAccess
public class CreateProjectCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CreateProjectCommand.class);

    @Inject
    private ProjectManagementApi projectManagementApi;

    @Inject 
    private TimerService timerService;

    @Option(names = {"-n", "--name"}, 
            description = "Название проекта", 
            required = true)
    private String name;

    @Option(names = {"-s", "--status"}, 
            description = "Статус проекта: IN_PROGRESS или DONE", 
            required = false)
    private ProjectCreateRequest.StatusEnum status = ProjectCreateRequest.StatusEnum.IN_PROGRESS;

    @Option(names = {"-p", "--priority"}, 
            description = "Приоритет проекта: HIGH, MIDDLE или LOW", 
            required = false)
    private ProjectCreateRequest.PriorityEnum priority = ProjectCreateRequest.PriorityEnum.MIDDLE;

    @Option(names = {"-i", "--interactive"}, 
            description = "Интерактивный режим ввода данных", 
            required = false)
    private boolean interactive = false;

    @Override
    public Integer call() throws Exception {
        try {
            // Если включен интерактивный режим, используем Scanner для ввода данных
            if (interactive || name == null) {
                return runInteractive();
            }

            // Создаем проект с переданными параметрами
            return createProject();
            
        } catch (Exception e) {
            log.error("Ошибка при создании проекта: {}", e.getMessage(), e);
            System.err.println("❌ Ошибка: " + e.getMessage());
            return 1;
        }
    }

    private Integer runInteractive() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("🎯 Создание нового проекта");
        System.out.println("=".repeat(40));
        
        // Ввод названия проекта
        if (name == null || name.trim().isEmpty()) {
            System.out.print("📝 Введите название проекта: ");
            name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                System.err.println("❌ Название проекта не может быть пустым");
                return 1;
            }
        }

        // Выбор статуса проекта
        System.out.println("\n📊 Выберите статус проекта:");
        System.out.println("1. IN_PROGRESS - В процессе");
        System.out.println("2. DONE - Завершен");
        
        if (status == null) {
            System.out.print("Ваш выбор (1-2) [по умолчанию: 1]: ");
            String statusChoice = scanner.nextLine().trim();
            
            switch (statusChoice) {
                case "2":
                    status = ProjectCreateRequest.StatusEnum.DONE;
                    break;
                default:
                    status = ProjectCreateRequest.StatusEnum.IN_PROGRESS;
                    break;
            }
        }

        // Выбор приоритета проекта
        System.out.println("\n🔢 Выберите приоритет проекта:");
        System.out.println("1. HIGH - Высокий");
        System.out.println("2. MIDDLE - Средний");
        System.out.println("3. LOW - Низкий");
        
        if (priority == null) {
            System.out.print("Ваш выбор (1-3) [по умолчанию: 2]: ");
            String priorityChoice = scanner.nextLine().trim();
            
            switch (priorityChoice) {
                case "1":
                    priority = ProjectCreateRequest.PriorityEnum.HIGH;
                    break;
                case "3":
                    priority = ProjectCreateRequest.PriorityEnum.LOW;
                    break;
                default:
                    priority = ProjectCreateRequest.PriorityEnum.MIDDLE;
                    break;
            }
        }

        return createProject();
    }

    private Integer createProject() {
        try {
            // Создаем запрос на создание проекта
            ProjectCreateRequest request = new ProjectCreateRequest()
                    .name(name)
                    .status(status)
                    .priority(priority);

            System.out.println("\n🚀 Создание проекта...");
            System.out.println("📋 Детали проекта:");
            System.out.println("   Название: " + name);
            System.out.println("   Статус: " + status.getValue());
            System.out.println("   Приоритет: " + priority.getValue());
            System.out.println();

            // Отправляем запрос на сервер
            ApiResponseProjectResponse response = projectManagementApi.createProject(request);

            // Проверяем результат
            if (Boolean.TRUE.equals(response.getIsSuccess())) {
                System.out.println("✅ Проект успешно создан!");
                System.out.println("🎉 ID проекта: " + response.getData().getId());
                System.out.println("📌 Название: " + response.getData().getName());
                System.out.println("📊 Статус: " + response.getData().getStatus());
                System.out.println("🔢 Приоритет: " + response.getData().getPriority());
                System.out.println("🕒 Создан: " + response.getData().getCreatedTs());
                
                if (response.getData().getUpdatedTs() != null) {
                    System.out.println("🔄 Обновлен: " + response.getData().getUpdatedTs());
                }
                
                return 0;
            } else {
                System.err.println("❌ Ошибка при создании проекта!");
                if (response.getErrorMessage() != null) {
                    System.err.println("💬 Сообщение: " + response.getErrorMessage());
                }
                if (response.getErrorType() != null) {
                    System.err.println("🔍 Тип ошибки: " + response.getErrorType());
                }
                return 1;
            }

        } catch (Exception e) {
            log.error("Ошибка при вызове API создания проекта: {}", e.getMessage(), e);
            System.err.println("❌ Сетевая ошибка: " + e.getMessage());
            return 1;
        }
    }
}