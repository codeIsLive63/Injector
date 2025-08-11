package codeislive63.test.java.dependencyInjection.serviceCollectionTests;

import codeislive63.dependencyInjection.ServiceLifetime;
import codeislive63.dependencyInjection.implementations.ServiceCollectionBase;
import codeislive63.dependencyInjection.interfaces.ServiceCollection;
import codeislive63.dependencyInjection.interfaces.ServiceProvider;
import codeislive63.dependencyInjection.interfaces.ServiceScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.*;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCollectionAddMethodsWithInjectionTest {

    private ServiceCollection serviceCollection;

    @BeforeEach
    void setUp() {
        serviceCollection = new ServiceCollectionBase();
    }

    @Test
    void testAddTransientConstructorInjection() {
        configureServicesForConstructorInjection(ServiceLifetime.TRANSIENT);
        var processor = getServiceFromProvider(ConstructorInjectedProcessor.class);
        assertNotNull(processor);
        processor.process();
    }

    @Test
    void testAddTransientMethodInjection() {
        configureServicesForMethodInjection(ServiceLifetime.TRANSIENT);
        var processor = getServiceFromProvider(MethodInjectedProcessor.class);
        assertNotNull(processor);
        processor.process();
    }

    @Test
    void testAddScopedConstructorInjection() {
        configureServicesForConstructorInjection(ServiceLifetime.SCOPED);
        assertScopedService(ConstructorInjectedProcessor.class);
    }

    @Test
    void testAddScopedMethodInjection() {
        configureServicesForMethodInjection(ServiceLifetime.SCOPED);
        assertScopedService(MethodInjectedProcessor.class);
    }

    @Test
    void testAddSingletonConstructorInjection() {
        configureServicesForConstructorInjection(ServiceLifetime.SINGLETON);
        assertSingletonService(ConstructorInjectedProcessor.class);
    }

    @Test
    void testAddSingletonMethodInjection() {
        configureServicesForMethodInjection(ServiceLifetime.SINGLETON);
        assertSingletonService(MethodInjectedProcessor.class);
    }

    private void configureServicesForConstructorInjection(ServiceLifetime lifetime) {
        addService(LoggingService.class, LoggingServiceImplementation.class, lifetime);
        addService(DataService.class, DataServiceImplementation.class, lifetime);
        addService(ConstructorInjectedProcessor.class, lifetime);
    }

    private void configureServicesForMethodInjection(ServiceLifetime lifetime) {
        addService(LoggingService.class, LoggingServiceImplementation.class, lifetime);
        addService(DataService.class, DataServiceImplementation.class, lifetime);
        addService(MethodInjectedProcessor.class, lifetime);
    }

    private <TAbstract, TImplementation extends TAbstract> void addService(Class<TAbstract> abstractType, Class<TImplementation> implementationType, ServiceLifetime lifetime) {
        switch (lifetime) {
            case TRANSIENT -> serviceCollection.addTransient(abstractType, implementationType);
            case SCOPED -> serviceCollection.addScoped(abstractType, implementationType);
            case SINGLETON -> serviceCollection.addSingleton(abstractType, implementationType);
        }
    }

    private <TService> void addService(Class<TService> serviceType, ServiceLifetime lifetime) {
        switch (lifetime) {
            case TRANSIENT -> serviceCollection.addTransient(serviceType);
            case SCOPED -> serviceCollection.addScoped(serviceType);
            case SINGLETON -> serviceCollection.addSingleton(serviceType);
        }
    }

    private <TService> TService getServiceFromProvider(Class<TService> serviceType) {
        try (ServiceProvider serviceProvider = serviceCollection.buildServiceProvider()) {
            return serviceProvider.getService(serviceType);
        }
    }

    private <TService> void assertScopedService(Class<TService> serviceType) {
        try (ServiceProvider serviceProvider = serviceCollection.buildServiceProvider()) {
            TService service1;
            TService service2;

            try (ServiceScope scope1 = serviceProvider.createScope()) {
                service1 = scope1.getServiceProvider().getService(serviceType);
                service2 = scope1.getServiceProvider().getService(serviceType);
                assertSame(service1, service2);
            }

            try (ServiceScope scope2 = serviceProvider.createScope()) {
                TService service3 = scope2.getServiceProvider().getService(serviceType);
                assertNotSame(service1, service3);
            }
        }
    }

    private <TService> void assertSingletonService(Class<TService> serviceType) {
        TService service1;
        TService service2;

        try (ServiceProvider serviceProvider = serviceCollection.buildServiceProvider()) {
            service1 = serviceProvider.getService(serviceType);
            service2 = serviceProvider.getService(serviceType);
        }

        assertSame(service1, service2);
    }
}
