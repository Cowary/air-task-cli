package org.cowary;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import lombok.extern.slf4j.Slf4j;
import org.cowary.command.AirCommands;
import org.jline.console.SystemRegistry;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AirTaskCliCommand {

    public static void main(String[] args) throws Exception {
        try (ApplicationContext context = ApplicationContext.run(Environment.CLI)) {
            log.info("Air Task CLI application started");

            AirCommands commands = context.getBean(AirCommands.class);

            MicronautFactory micronautFactory = new MicronautFactory(context);
            CommandLine cmd = new CommandLine(commands, micronautFactory);
            PicocliCommands picocliCommands = new PicocliCommands(cmd);

            SystemRegistry systemRegistry = context.getBean(SystemRegistry.class);
            systemRegistry.setCommandRegistries(picocliCommands);
            systemRegistry.register("help", picocliCommands);

            LineReader lineReader = context.getBean(LineReader.class);

            String line;
            while (true) {
                try {
                    String prompt = "air-task-cli> ";
                    line = lineReader.readLine(prompt, null, (MaskingCallback) null, null);
                    if (line == null || "exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                        break;
                    }
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    systemRegistry.cleanUp();
                    systemRegistry.execute(line);
                } catch (EndOfFileException e) {
                    System.out.println("Exiting...");
                    break;
                } catch (Exception e) {
                    log.error("Error while running command: {}", e.getMessage(), e);
                    System.err.println("Error: " + e.getMessage());
                }
            }
            log.debug("Air Task CLI application finished");
        } catch (Exception e) {
            log.error("Не удалось инициализировать терминал", e);
            throw new RuntimeException("Ошибка ввода-вывода при запуске CLI", e);
        }
    }


}
