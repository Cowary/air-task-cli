package org.cowary.command;

import jakarta.inject.Inject;
import org.cowary.service.CompletedWeeklyService;
import org.cowary.air_task_cli.model.WeeklyWithCompletionStatus;
import org.cowary.air_task_cli.model.CompletedWeeklyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * Команда для интерактивного управления завершенными недельными задачами
 */
@Command(name = "complete-weekly", 
         description = "Интерактивная команда для отметки выполненных недельных задач",
         mixinStandardHelpOptions = true)
public class CompleteWeeklyCommand implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CompleteWeeklyCommand.class);
    
    @Inject
    private CompletedWeeklyService completedWeeklyService;
    
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Показать справку")
    private boolean helpRequested = false;
    
    @Override
    public Integer call() throws Exception {
        try {
            logger.info("Запуск команды complete-weekly");
            
            // Получаем статистику недельных задач
            List<WeeklyWithCompletionStatus> incompleteTasks = completedWeeklyService.getIncompleteTasks();
            
            if (incompleteTasks.isEmpty()) {
                System.out.println("🎉 Поздравляем! Все недельные задачи выполнены!");
                return 0;
            }
            
            System.out.println("\n📋 Не завершенные недельные задачи на этой неделе:");
            System.out.println("=".repeat(80));
            
            // Отображаем список не завершенных задач с ID от 0
            for (int i = 0; i < incompleteTasks.size(); i++) {
                WeeklyWithCompletionStatus task = incompleteTasks.get(i);
                System.out.printf("%d. %s (Проект: %s)%n", 
                    i, 
                    task.getWeeklyTaskName(), 
                    task.getProjectName());
                System.out.printf("   Требуется: %d, Выполнено: %d, Осталось: %d (%s)%n",
                    task.getRequiredCount(),
                    task.getCompletedCount(),
                    task.getRemainingCount(),
                    task.getCompletionPercentage());
                System.out.println();
            }
            
            System.out.println("=".repeat(80));
            System.out.println("Введите номер задачи, которую вы выполнили (0-" + (incompleteTasks.size() - 1) + "), или 'q' для выхода:");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                System.out.println("Выход из команды.");
                return 0;
            }
            
            try {
                int taskIndex = Integer.parseInt(input);
                
                if (taskIndex < 0 || taskIndex >= incompleteTasks.size()) {
                    System.err.println("❌ Ошибка: Неверный номер задачи. Введите число от 0 до " + (incompleteTasks.size() - 1));
                    return 1;
                }
                
                WeeklyWithCompletionStatus selectedTask = incompleteTasks.get(taskIndex);
                
                System.out.printf("✅ Вы выбрали задачу: %s%n", selectedTask.getWeeklyTaskName());
                System.out.println("Отправляем данные о выполнении на сервер...");
                
                // Создаем запись о выполненной задаче
                CompletedWeeklyResponse completedTask = completedWeeklyService.createCompletedWeeklyTask(
                    selectedTask.getWeeklyTaskId()
                );
                
                System.out.printf("✅ Задача успешно отмечена как выполненная! ID записи: %d%n", completedTask.getId());
                
                // Показываем обновленную статистику
                List<WeeklyWithCompletionStatus> updatedIncompleteTasks = completedWeeklyService.getIncompleteTasks();
                List<WeeklyWithCompletionStatus> updatedCompletedTasks = completedWeeklyService.getCompletedTasks();
                
                System.out.println("\n📊 Обновленная статистика:");
                System.out.printf("Завершенных задач: %d%n", updatedCompletedTasks.size());
                System.out.printf("Осталось выполнить: %d%n", updatedIncompleteTasks.size());
                
                if (updatedIncompleteTasks.isEmpty()) {
                    System.out.println("🎉 Поздравляем! Все недельные задачи выполнены!");
                }
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Ошибка: Введите корректный номер задачи или 'q' для выхода");
                return 1;
            }
            
            return 0;
            
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды complete-weekly", e);
            System.err.println("❌ Ошибка: " + e.getMessage());
            return 1;
        }
    }
}