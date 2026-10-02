package com.fivesense.api.auth.app;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapInitializer implements ApplicationRunner {
    private final BootstrapService bootstrap;
    public BootstrapInitializer(BootstrapService bootstrap){this.bootstrap=bootstrap;}
    @Override public void run(ApplicationArguments args){bootstrap.prepareInitialAdmin();}
}
