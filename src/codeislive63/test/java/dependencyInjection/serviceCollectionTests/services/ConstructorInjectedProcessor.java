package codeislive63.test.java.dependencyInjection.serviceCollectionTests.services;

import codeislive63.dependencyInjection.annotations.Inject;

public class ConstructorInjectedProcessor {
    private final LoggingService loggingService;
    private final DataService dataService;

    @Inject
    public ConstructorInjectedProcessor(LoggingService loggingService, DataService dataService) {
        this.loggingService = loggingService;
        this.dataService = dataService;
    }

    public void process() {
        loggingService.log("Начинаю выполнение действия по получению данных...");
        String data = dataService.getData();
        loggingService.log("Данные: " + data);
    }
}
