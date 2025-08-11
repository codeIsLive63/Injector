package codeislive63.test.java.dependencyInjection.serviceCollectionTests.services;

public class LoggingServiceImplementation implements LoggingService {

    @Override
    public void log(String message) {
        System.out.println("Сообщение: " + message);
    }
}
