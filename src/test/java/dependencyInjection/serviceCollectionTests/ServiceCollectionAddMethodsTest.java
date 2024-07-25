package test.java.dependencyInjection.serviceCollectionTests;

import dependencyInjection.implementations.ServiceCollectionBase;
import dependencyInjection.interfaces.ServiceCollection;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.java.dependencyInjection.serviceCollectionTests.services.TestService;
import test.java.dependencyInjection.serviceCollectionTests.services.TestServiceImplementation;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCollectionAddMethodsTest {

    private ServiceCollection serviceCollection;

    @BeforeEach
    void setUp() {
        serviceCollection = new ServiceCollectionBase();
    }

    @Test
    void testAddTransientAbstractAndImplementation() {
        serviceCollection.addTransient(TestService.class, TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertTransientServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddTransientFactory() {
        serviceCollection.addTransient(TestService.class, provider -> new TestServiceImplementation());
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertTransientServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddTransientImplementation() {
        serviceCollection.addTransient(TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertTransientServices(serviceProvider, TestServiceImplementation.class);
    }

    private void assertTransientServices(@NotNull ServiceProvider serviceProvider, Class<? extends TestService> testClass) {
        TestService service1 = serviceProvider.getService(testClass);
        TestService service2 = serviceProvider.getService(testClass);

        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2);
    }

    @Test
    void testAddScopedAbstractAndImplementation() {
        serviceCollection.addScoped(TestService.class, TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertScopedServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddScopedFactory() {
        serviceCollection.addScoped(TestService.class, provider -> new TestServiceImplementation());
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertScopedServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddScopedImplementation() {
        serviceCollection.addScoped(TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertScopedServices(serviceProvider, TestServiceImplementation.class);
    }

    private void assertScopedServices(@NotNull ServiceProvider serviceProvider, Class<? extends TestService> testClass) {
        TestService service1;
        TestService service2;

        try (ServiceScope scope1 = serviceProvider.createScope()) {
            service1 = scope1.getServiceProvider().getService(testClass);
            service2 = scope1.getServiceProvider().getService(testClass);
            assertSame(service1, service2);
        }

        try (ServiceScope scope2 = serviceProvider.createScope()) {
            TestService service3 = scope2.getServiceProvider().getService(testClass);
            assertNotSame(service1, service3);
        }
    }

    @Test
    void testAddSingletonAbstractAndImplementation() {
        serviceCollection.addSingleton(TestService.class, TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertSingletonServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddSingletonFactory() {
        serviceCollection.addSingleton(TestService.class, provider -> new TestServiceImplementation());
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertSingletonServices(serviceProvider, TestService.class);
    }

    @Test
    void testAddSingletonImplementation() {
        serviceCollection.addSingleton(TestServiceImplementation.class);
        ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();
        assertSingletonServices(serviceProvider, TestServiceImplementation.class);
    }

    private void assertSingletonServices(@NotNull ServiceProvider serviceProvider, Class<? extends TestService> testClass) {
        TestService service1 = serviceProvider.getService(testClass);
        TestService service2 = serviceProvider.getService(testClass);

        assertNotNull(service1);
        assertNotNull(service2);
        assertSame(service1, service2);
    }
}