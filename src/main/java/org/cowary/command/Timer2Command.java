package org.cowary.command;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cowary.air_task_cli.model.BoardRs;
import org.cowary.air_task_cli.model.TaskListRs;
import org.cowary.service.TimerService;
import org.jline.reader.LineReader;
import picocli.CommandLine;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@CommandLine.Command(
        name = "timer2",
        description = "Timer-related commands"
)
@RequiredArgsConstructor
public class Timer2Command implements Runnable {

    @Inject
    private TimerService timerService;

    @Inject
    private LineReader lineReader;

    // Вспомогательный метод: выбор из списка
    private long selectFromList(String prompt, List<Choice> choices) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("ID", "Name");
        table.addRule();
        for (var choice : choices) {
            table.addRow(choice.id(), choice.name());
            table.addRule();
        }
        table.setTextAlignment(TextAlignment.CENTER);
        System.out.println(prompt + ":");
        System.out.println(table.render());

        while (true) {
            String input = lineReader.readLine("Enter ID (or 'q' to quit): ");
            if (input == null || "q".equalsIgnoreCase(input.trim())) {
                throw new RuntimeException("Operation cancelled by user.");
            }
            try {
                long id = Long.parseLong(input.trim());
                if (choices.stream().anyMatch(c -> c.id() == id)) {
                    return id;
                } else {
                    System.out.println("Invalid ID. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    record Choice(long id, String name) {
    }

    @Override
    public void run() {
        try {
            // Шаг 1: Получить проекты
//            var projectList = timerService.getAllProjects().getProjectList();
//            var projectChoices = projectList.stream()
//                    .map(p -> new Choice(p.getId(), p.getName()))
//                    .toList();
//
//            long projectId = selectCommand("project number");
//            var selectedProject = projectList.stream()
//                    .filter(p -> p.getId().equals(projectId))
//                    .findFirst()
//                    .orElseThrow();
//
//            // Шаг 2: Получить все задачи в проекте
//            var taskList = timerService.getAllTaskByProjectId(projectId);
//            var taskChoices = new ArrayList<Choice>();
//            long taskId = -1;
//            String taskName = "";
//
//            for (var board : taskList) {
//                for (var taskListRs : board.getTaskList()) {
//                    for (var task : taskListRs.getTasks()) {
//                        taskChoices.add(new Choice(task.getId(), task.getName()));
//                    }
//                }
//            }
//
//            final long taskId = selectCommand("project number");
//            long finalTaskId = taskId;
//            taskName = taskChoices.stream()
//                    .filter(t -> t.id() == finalTaskId)
//                    .map(Choice::name)
//                    .findFirst()
//                    .orElse("Unknown Task");
            Long projectId;
            Long taskId;
            Long time;

            System.out.println("Start timer!");
            var projectList = getProjects();
            final long input1 = selectCommand("project number");
            projectId = projectList.stream().filter(p -> p.id().equals(input1)).findFirst().map(TimerCommand.Board::projectId).orElseThrow();
            var taskList = getTask(projectId);
            final long input2 = selectCommand("task number");
            taskId = taskList.stream().filter(t -> t.id().equals(input2)).findFirst().map(TimerCommand.Task::projectId).orElseThrow();
//            time = selectCommand("Time in minute");
//            timerService.processTimer(time);
//            time = time * 60;
//            timerService.updateTime(taskId, time);
//            System.out.println("End timer! with " + time + " minutes");

            // Шаг 3: Запуск таймера
//            System.out.println("\n✅ Timer started for task: " + taskName);
            System.out.println("Press Enter to stop the timer...");
            Instant start = Instant.now();

            // Блокируем поток до нажатия Enter
            System.in.read(); // или использовать Scanner

            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            long seconds = duration.getSeconds();
            long minutes = seconds / 60;

            System.out.printf("⏱️ Timer stopped. Duration: %d min %d sec%n", minutes, seconds % 60);

            // Шаг 4: Отправка времени на бэкенд
            timerService.updateTime(taskId, Long.valueOf((int) seconds)); // предполагаем, что метод принимает секунды
            System.out.println("✅ Time logged successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            log.error("Timer command failed", e);
        }
    }

    public List<TimerCommand.Board> getProjects() {
        var list = timerService.getAllProjects();
        var listEntity = new ArrayList<TimerCommand.Board>();
        for (int i1 = 0; i1 < list.getProjectList().size(); i1++) {
            listEntity.add(new TimerCommand.Board((long) i1, list.getProjectList().get(i1).getName(), list.getProjectList().get(i1).getId()));
        }
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow("Id", "Name");
        asciiTable.addRule();
        for (TimerCommand.Board board : listEntity) {
            asciiTable.addRow(board.id(), board.name());
            asciiTable.addRule();
        }
        asciiTable.setTextAlignment(TextAlignment.CENTER);
        String render = asciiTable.render();
        System.out.println("Project list: ");
        System.out.println(render);
        return listEntity;
    }

    record Board(Long id, String name, Long projectId) {}
    record Task(Long id, String name, Long projectId) {}

    public List<TimerCommand.Task> getTask(Long projectId) {
        var list = timerService.getAllTaskByProjectId(projectId);
        var listEntity = new ArrayList<List<TimerCommand.Task>>();
//        for (int i1 = 0; i1 < list.size(); i1++) {
//            listEntity.add(new Task((long) i1, list.get(i1).getName(), list.get(i1).getId()));
//        }
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        List<String> listNames = list.stream().map(BoardRs::getName).toList();
        asciiTable.addRow(listNames);
        asciiTable.addRule();
        long i = 0;
        var k = new ArrayList<String>();
        var maxTable = 0;
        Map<String, ArrayList<TimerCommand.Task>> map = new HashMap<>();
        for (var board : list) {

            var l = new ArrayList<TimerCommand.Task>();

            for (TaskListRs taskListRs : board.getTaskList()) {
                for (var task : taskListRs.getTasks()) {
                    l.add(new TimerCommand.Task(i++, task.getName(), task.getId()));
                }
                maxTable = Math.max(maxTable, taskListRs.getTasks().size());
            }
            map.put(board.getName(), l);
            listEntity.add(l);
//            k.add(l.stream().map(e -> e.id() + " : " + e.name() + System.lineSeparator()).collect(Collectors.joining()));
//            System.out.println(l);

//            asciiTable.addRow(l.stream().map(t -> t.id + " : " + t.name).toList());
        }
        System.out.println("List name: " + listNames
        );
        System.out.println("Map: " + map);
        for (int i1 = 0; i1 <= maxTable; i1++) {
            List<String> list1 = new ArrayList<>();
            for (int i2 = 0; i2 < listNames.size(); i2++) {
                var tasks = map.get(listNames.get(i2));
                if (tasks.size() > i1) {
                    list1.add(tasks.get(i1).id() + ") " + tasks.get(i1).name());
                } else {
                    list1.add("-----");
                }
            }
            asciiTable.addRow(list1);
        }

//        asciiTable.addRow(k);

//        for (Task board : list) {
//            asciiTable.addRow(board.id, board.name);
//            asciiTable.addRule();
//        }
        asciiTable.setTextAlignment(TextAlignment.CENTER);

        String render = asciiTable.render();
        System.out.println("Task table: ");
        System.out.println(render);
        return listEntity.stream().flatMap(List::stream).toList();
    }

    private @Nonnull Long selectCommand(String text) {
//        System.out.println("\nAvailable commands:");
        System.out.println("Select: " + text);

        // Запрашиваем выбор
        while (true) {
            String input = lineReader.readLine("\nSelect number (or 'q' to quit): ");
            if (input == null || "q".equalsIgnoreCase(input.trim())) {
                return null;
            }

            try {
                long choice = Long.parseLong(input.trim());
//                if (commandMap.containsKey(choice)) {
//                    return choice;
//                } else {
//                    System.out.println("Invalid choice. Please try again.");
//                }
                return choice;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}
