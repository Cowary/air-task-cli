package org.cowary.command;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cowary.service.TimerService;
import picocli.CommandLine;

@Singleton
@Slf4j
@CommandLine.Command(name = "timer")
@RequiredArgsConstructor
public class TimerCommand implements Runnable {
    @Inject
    private TimerService timerService;

    @Override
    public void run() {
        System.out.println("Start timer!");
        timerService.processTimer(2);
        System.out.println("End timer!");
    }
}
