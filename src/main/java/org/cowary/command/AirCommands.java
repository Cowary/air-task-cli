package org.cowary.command;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

import java.io.PrintWriter;

@Singleton
@Slf4j
@Command(name = "air-task", mixinStandardHelpOptions = true, version = "0.0.1-SNAPSHOT",
        description = "Air Task commands",
        subcommands = {
        TimerCommand.class, Timer2Command.class
        })
public class AirCommands implements Runnable {

    PrintWriter out;

    public AirCommands() {}

    public void setReader(LineReader reader){
        out = reader.getTerminal().writer();
    }

    @Override
    public void run() {
        System.out.println("Use --help to see available subcommands");
    }
}
