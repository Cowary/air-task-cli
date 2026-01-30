package org.cowary.command;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cowary.air_task_cli.model.BoardRs;
import org.cowary.air_task_cli.model.TaskListRs;
import org.cowary.service.TimerService;
import org.jline.reader.LineReader;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@CommandLine.Command(name = "timer")
@RequiredArgsConstructor
public class TimerCommand implements Runnable {
    @Inject
    private TimerService timerService;
    @Inject
    private LineReader lineReader;

    @Override
    public void run() {
        Long projectId;
        Long taskId;
        Long time;
        System.out.println("Start timer!");
        var projectList = getProjects();
        final long input1 = selectCommand("project number");
        projectId = projectList.stream().filter(p -> p.id.equals(input1)).findFirst().map(Board::projectId).orElseThrow();
        var taskList = getTask(projectId);
        final long input2 = selectCommand("task number");
        taskId = taskList.stream().filter(t -> t.id.equals(input2)).findFirst().map(Task::projectId).orElseThrow();
        time = selectCommand("Time in minute");
        timerService.processTimer(time);
        time = time * 60;
        timerService.updateTime(taskId, time);
        System.out.println("End timer! with " + time + " minutes");
    }

    public List<Board> getProjects() {
        var list = timerService.getAllProjects();
        var listEntity = new ArrayList<Board>();
        for (int i1 = 0; i1 < list.getProjectList().size(); i1++) {
            listEntity.add(new Board((long) i1, list.getProjectList().get(i1).getName(), list.getProjectList().get(i1).getId()));
        }
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow("Id", "Name");
        asciiTable.addRule();
        for (Board board : listEntity) {
            asciiTable.addRow(board.id, board.name);
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

    public List<Task> getTask(Long projectId) {
        var list = timerService.getAllTaskByProjectId(projectId);
        var listEntity = new ArrayList<List<Task>>();
//        for (int i1 = 0; i1 < list.size(); i1++) {
//            listEntity.add(new Task((long) i1, list.get(i1).getName(), list.get(i1).getId()));
//        }
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow(list.stream().map(BoardRs::getName).toList());
        asciiTable.addRule();
        long i = 0;
        var k = new ArrayList<String>();
        for (var board : list) {
            var l = new ArrayList<Task>();

            for (TaskListRs taskListRs : board.getTaskList()) {
                for (var task : taskListRs.getTasks()) {
                    l.add(new Task(i++, task.getName(), task.getId()));
                }
            }
            listEntity.add(l);
            k.add(l.stream().map(e -> e.id() + " : " + e.name() + System.lineSeparator()).collect(Collectors.joining()));
            System.out.println(l);

//            asciiTable.addRow(l.stream().map(t -> t.id + " : " + t.name).toList());
        }
        asciiTable.addRow(k);

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
