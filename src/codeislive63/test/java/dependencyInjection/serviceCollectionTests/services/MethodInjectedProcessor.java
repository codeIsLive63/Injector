package codeislive63.test.java.dependencyInjection.serviceCollectionTests.services;

import codeislive63.dependencyInjection.annotations.Inject;

public class MethodInjectedProcessor {
    private LoggingService loggingService;
    private DataService dataService;

    @Inject
    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Inject
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public void process() {
        loggingService.log("Начинаю выполнение действия по получению данных......");
        String data = dataService.getData();
        loggingService.log("Данные: " + data);
    }
}
