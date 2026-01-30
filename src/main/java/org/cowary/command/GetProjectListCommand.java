package org.cowary.command;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cowary.service.TimerService;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@Singleton
@Slf4j
@CommandLine.Command(name = "task")
@RequiredArgsConstructor
public class GetProjectListCommand implements Runnable{

    @Inject
    private TimerService timerService;

    @Override
    public void run() {
//        var list = timerService.getAllProjects();
//        var listEntity = new ArrayList<Entity>();
//        for (int i1 = 0; i1 < list.getProjectList().size(); i1++) {
//            listEntity.add(new Entity(i1, list.getProjectList().get(i1).getName(), list.getProjectList().get(i1).getId()));
//        }
//        AsciiTable asciiTable = new AsciiTable();
//        asciiTable.addRule();
//        asciiTable.addRow("Id", "Name");
//        asciiTable.addRule();
//        for (Entity entity : listEntity) {
//            asciiTable.addRow(entity.id, entity.name);
//            asciiTable.addRule();
//        }
//        asciiTable.setTextAlignment(TextAlignment.CENTER);
//        String render = asciiTable.render();
//        System.out.println(render);
    }

    record Entity(Integer id, String name, Long projectId) {}
}
