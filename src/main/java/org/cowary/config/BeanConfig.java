package org.cowary.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.cowary.air_task_cli.api.ProjectControllerApi;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

@Factory
public class BeanConfig {

    @Bean
    @Singleton
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .build();
    }

    @Bean
    @Singleton
    public Parser parser() {
        return new DefaultParser();
    }

    @Bean
    @Singleton
    public SystemRegistry systemRegistry(Terminal terminal, Parser parser) {
        Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
        Builtins builtins = new Builtins(workDir, new ConfigurationPath(workDir.get(), workDir.get()), null);

        SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
        systemRegistry.setCommandRegistries(builtins);

        return systemRegistry;
    }

    @Bean
    @Singleton
    public LineReader lineReader(Terminal terminal, Parser parser, SystemRegistry systemRegistry) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(systemRegistry.completer())
                .parser(parser)
                .variable(LineReader.LIST_MAX, 50)
                .build();
    }
//
//    @Bean
//    @Singleton
//    @Primary
//    public ProjectControllerApiClient projectControllerApi() {
//        return new ProjectControllerApiClient();
//    }
}

