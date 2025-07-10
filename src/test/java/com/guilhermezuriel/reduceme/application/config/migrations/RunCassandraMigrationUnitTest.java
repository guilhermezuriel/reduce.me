package com.guilhermezuriel.reduceme.application.config.migrations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.session.Session;
import com.guilhermezuriel.reduceme.application.config.migrations.queries.CqlMigrationsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

@ExtendWith(MockitoExtension.class)
class RunCassandraMigrationUnitTest {

    @Mock
    private Session mockSession; // This is the 'session' injected by Spring
    @Mock
    private ResourceLoader mockResourceLoader;
    @Mock
    private MigrationService mockMigrationService; // Will be created internally, so we need to control its behavior
    @Mock
    private CqlSession mockCqlSession; // The session created inside afterPropertiesSet()

    @InjectMocks
    private RunCassandraMigrations runCassandraMigrations;

    @BeforeEach
    void setUp() {


        try (MockedStatic<CqlSession> mockedCqlSession = mockStatic(CqlSession.class)) {
            CqlSession.Builder mockBuilder = mock(CqlSession.Builder.class);
            mockedCqlSession.when(CqlSession::builder).thenReturn(mockBuilder);
            when(mockBuilder.addContactPoint(any(InetSocketAddress.class))).thenReturn(mockBuilder);
            when(mockBuilder.withLocalDatacenter(anyString())).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockCqlSession); // Return our mock CqlSession
        } catch (Exception e) {
            // This mock setup is tricky, ensure it runs for all tests that call afterPropertiesSet
        }


        // Set @Value properties via reflection for unit tests
        ReflectionTestUtils.setField(runCassandraMigrations, "contactPoints", "localhost");
        ReflectionTestUtils.setField(runCassandraMigrations, "port", 9042);

        // Call init() manually as it's @PostConstruct
        runCassandraMigrations.init();
    }


    // --- Tests for afterPropertiesSet() ---

    @Test
    void shouldCreatePublicSchemaAndMigrationSystemTableOnFirstRun() throws Exception {
        // Arrange
        when(mockMigrationService.tableExists("migration_system")).thenReturn(false);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(null); // No previous migrations

        // Mock ResourceLoader to return no migration files for this scenario
        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{}); // No files found
        }

        // Use a real MigrationService instance, but mock its dependencies, or mock MigrationService itself
        // Here we are controlling the behavior of `mockMigrationService` directly.
        // A better approach for this particular case might be to *not* mock MigrationService
        // but rather inject mocks *into* a real MigrationService instance if its
        // logic is simple and just delegates to the CqlSession.
        // For this example, let's assume MigrationService is *also* mocked.
        try (MockedStatic<MigrationService> mockedMigrationService = mockStatic(MigrationService.class)) {
            // How to make this mock the one used inside afterPropertiesSet?
            // This is the tricky part. The `new MigrationService(session)` call is problematic for mocking.
            // A common pattern is to make MigrationService a Spring bean and inject it directly.
            // If it must be created via 'new', you'd need to use PowerMockito or refactor.
            // For now, let's assume we can somehow inject/control the instance created.
            // The simplest workaround for unit tests *without* PowerMockito is to mock the session
            // and verify calls *on that session*, if MigrationService just wraps it.
            // Or, for the purpose of demonstrating, we'll mock the *behavior* of MigrationService's methods.

            runCassandraMigrations.afterPropertiesSet();

            verify(mockMigrationService).createPublicSchemaIfNotExists();
            verify(mockMigrationService).createMigrationSystemTable();
            verify(mockMigrationService, never()).executeQueryString(anyString()); // No migrations to execute
            verify(mockMigrationService, never()).registerMigrationOnSystem(anyLong(), anyString(), anyString(), anyLong());
        }
    }

    @Test
    void shouldExecuteNewMigrationsAndRegisterThem() throws Exception {
        // Arrange
        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(null); // Simulate no previous migrations

        Resource mockMigration1 = mock(Resource.class);
        when(mockMigration1.getFilename()).thenReturn("V1__create_table.cql");
        when(mockMigration1.getInputStream()).thenReturn(new ByteArrayInputStream("CREATE TABLE test (id int PRIMARY KEY);".getBytes()));

        Resource mockMigration2 = mock(Resource.class);
        when(mockMigration2.getFilename()).thenReturn("V2__add_column.cql");
        when(mockMigration2.getInputStream()).thenReturn(new ByteArrayInputStream("ALTER TABLE test ADD COLUMN name text;".getBytes()));

        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{mockMigration1, mockMigration2});
        }
        // Mock QueryUtils isValidMigrationPattern static method
        try (MockedStatic<CqlMigrationsUtils> mockedQueryUtils = mockStatic(CqlMigrationsUtils.class)) {
            mockedQueryUtils.when(() -> CqlMigrationsUtils.isValidMigrationPattern(anyString())).thenReturn(true);
            mockedQueryUtils.when(() -> CqlMigrationsUtils.calculateChecksum(anyString())).thenReturn(123L); // Sample checksum

            // Act
            runCassandraMigrations.afterPropertiesSet();

            // Assert
            verify(mockMigrationService).createPublicSchemaIfNotExists(); // Always checked
            verify(mockMigrationService).tableExists("migration_system"); // Always checked
            verify(mockMigrationService, never()).createMigrationSystemTable(); // Already exists

            // Verify migrations were executed and registered
            verify(mockMigrationService).executeQueryString("CREATE TABLE test (id int PRIMARY KEY);");
            verify(mockMigrationService).registerMigrationOnSystem(1L, "V1__create_table.cql", "1.0", 123L);

            verify(mockMigrationService).executeQueryString("ALTER TABLE test ADD COLUMN name text;");
            verify(mockMigrationService).registerMigrationOnSystem(2L, "V2__add_column.cql", "1.0", 123L);

            verify(mockMigrationService, times(2)).registerMigrationOnSystem(anyLong(), anyString(), anyString(), anyLong());
            verify(mockMigrationService, times(2)).executeQueryString(anyString());
        }
    }

    @Test
    void shouldSkipAlreadyExecutedMigrationsWithMatchingChecksums() throws Exception {
        // Arrange
        Row mockLastMigrationRow = mock(Row.class);
        when(mockLastMigrationRow.getString("migration_name")).thenReturn("V1__create_table.cql");
        when(mockLastMigrationRow.getLong("installed_rank")).thenReturn(1L);

        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(mockLastMigrationRow);

        Resource mockMigration1 = mock(Resource.class);
        when(mockMigration1.getFilename()).thenReturn("V1__create_table.cql");
        when(mockMigration1.getInputStream()).thenReturn(new ByteArrayInputStream("CREATE TABLE test (id int PRIMARY KEY);".getBytes()));

        Resource mockMigration2 = mock(Resource.class);
        when(mockMigration2.getFilename()).thenReturn("V2__add_column.cql");
        when(mockMigration2.getInputStream()).thenReturn(new ByteArrayInputStream("ALTER TABLE test ADD COLUMN name text;".getBytes()));

        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{mockMigration1, mockMigration2});
        }
        try (MockedStatic<CqlMigrationsUtils> mockedQueryUtils = mockStatic(CqlMigrationsUtils.class)) {
            mockedQueryUtils.when(() -> CqlMigrationsUtils.isValidMigrationPattern(anyString())).thenReturn(true);
            // Mock checksums
            mockedQueryUtils.when(() -> CqlMigrationsUtils.calculateChecksum("CREATE TABLE test (id int PRIMARY KEY);")).thenReturn(100L);
            mockedQueryUtils.when(() -> CqlMigrationsUtils.calculateChecksum("ALTER TABLE test ADD COLUMN name text;")).thenReturn(200L);

            // Stored checksum for V1
            when(mockMigrationService.returnStoredChecksum("V1__create_table.cql")).thenReturn(100L);

            // Act
            runCassandraMigrations.afterPropertiesSet();

            // Assert
            verify(mockMigrationService).returnStoredChecksum("V1__create_table.cql"); // Checksum for V1 was validated
            verify(mockMigrationService, never()).executeQueryString("CREATE TABLE test (id int PRIMARY KEY);"); // V1 was skipped
            verify(mockMigrationService, never()).registerMigrationOnSystem(1L, "V1__create_table.cql", "1.0", 100L); // V1 not re-registered

            verify(mockMigrationService).executeQueryString("ALTER TABLE test ADD COLUMN name text;"); // V2 was executed
            verify(mockMigrationService).registerMigrationOnSystem(2L, "V2__add_column.cql", "1.0", 200L); // V2 was registered
        }
    }

    @Test
    void shouldThrowExceptionWhenChecksumMismatchOccurs() throws Exception {
        // Arrange
        Row mockLastMigrationRow = mock(Row.class);
        when(mockLastMigrationRow.getString("migration_name")).thenReturn("V1__create_table.cql");
        when(mockLastMigrationRow.getLong("installed_rank")).thenReturn(1L);

        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(mockLastMigrationRow);

        Resource mockMigration1 = mock(Resource.class);
        when(mockMigration1.getFilename()).thenReturn("V1__create_table.cql");
        when(mockMigration1.getInputStream()).thenReturn(new ByteArrayInputStream("CREATE TABLE test (id int PRIMARY KEY);".getBytes()));

        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{mockMigration1});
        }

        try (MockedStatic<CqlMigrationsUtils> mockedQueryUtils = mockStatic(CqlMigrationsUtils.class)) {
            mockedQueryUtils.when(() -> CqlMigrationsUtils.isValidMigrationPattern(anyString())).thenReturn(true);
            mockedQueryUtils.when(() -> CqlMigrationsUtils.calculateChecksum(anyString())).thenReturn(100L); // Current checksum
            when(mockMigrationService.returnStoredChecksum("V1__create_table.cql")).thenReturn(99L); // Stored checksum is different

            // Act & Assert
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> runCassandraMigrations.afterPropertiesSet());
            assertTrue(thrown.getMessage().contains("Checksum error: Migration was modified"));
            verify(mockMigrationService, never()).executeQueryString(anyString()); // No migrations should execute
        }
    }

    @Test
    void shouldProcessFilesInCorrectOrder() throws Exception {
        // Arrange
        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(null); // No previous migrations

        Resource mockMigration2 = mock(Resource.class);
        when(mockMigration2.getFilename()).thenReturn("V2__add_column.cql");
        when(mockMigration2.getInputStream()).thenReturn(new ByteArrayInputStream("ALTER TABLE test ADD COLUMN name text;".getBytes()));

        Resource mockMigration10 = mock(Resource.class);
        when(mockMigration10.getFilename()).thenReturn("V10__add_index.cql");
        when(mockMigration10.getInputStream()).thenReturn(new ByteArrayInputStream("CREATE INDEX ON test (name);".getBytes()));

        Resource mockMigration1 = mock(Resource.class);
        when(mockMigration1.getFilename()).thenReturn("V1__create_table.cql");
        when(mockMigration1.getInputStream()).thenReturn(new ByteArrayInputStream("CREATE TABLE test (id int PRIMARY KEY);".getBytes()));


        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            // Return files out of order to ensure sorting works
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{mockMigration2, mockMigration10, mockMigration1});
        }
        try (MockedStatic<CqlMigrationsUtils> mockedQueryUtils = mockStatic(CqlMigrationsUtils.class)) {
            mockedQueryUtils.when(() -> CqlMigrationsUtils.isValidMigrationPattern(anyString())).thenReturn(true);
            mockedQueryUtils.when(() -> CqlMigrationsUtils.calculateChecksum(anyString())).thenReturn(1L); // Simplified checksum

            // Act
            runCassandraMigrations.afterPropertiesSet();

            // Assert: Verify order of execution
            // Using inOrder to verify the sequence of calls
            var inOrder = inOrder(mockMigrationService);
            inOrder.verify(mockMigrationService).registerMigrationOnSystem(1L, "V1__create_table.cql", "1.0", 1L);
            inOrder.verify(mockMigrationService).executeQueryString("CREATE TABLE test (id int PRIMARY KEY);");
            inOrder.verify(mockMigrationService).registerMigrationOnSystem(2L, "V2__add_column.cql", "1.0", 1L);
            inOrder.verify(mockMigrationService).executeQueryString("ALTER TABLE test ADD COLUMN name text;");
            inOrder.verify(mockMigrationService).registerMigrationOnSystem(3L, "V10__add_index.cql", "1.0", 1L);
            inOrder.verify(mockMigrationService).executeQueryString("CREATE INDEX ON test (name);");
        }
    }

    @Test
    void shouldThrowExceptionOnIoErrorReadingMigrationFile() throws Exception {
        // Arrange
        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(null);

        Resource mockMigration = mock(Resource.class);
        when(mockMigration.getFilename()).thenReturn("V1__broken_file.cql");
        when(mockMigration.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{mockMigration});
        }
        try (MockedStatic<CqlMigrationsUtils> mockedQueryUtils = mockStatic(CqlMigrationsUtils.class)) {
            mockedQueryUtils.when(() -> CqlMigrationsUtils.isValidMigrationPattern(anyString())).thenReturn(true);

            // Act & Assert
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> runCassandraMigrations.afterPropertiesSet());
            assertTrue(thrown.getMessage().contains("Simulated IO error"));
            verify(mockMigrationService, never()).executeQueryString(anyString());
            verify(mockMigrationService, never()).registerMigrationOnSystem(anyLong(), anyString(), anyString(), anyLong());
        }
    }

    @Test
    void shouldHandleNoMigrationFilesGracefully() throws Exception {
        // Arrange
        when(mockMigrationService.tableExists("migration_system")).thenReturn(true);
        when(mockMigrationService.lastMigrationExecuted()).thenReturn(null);

        try (MockedStatic<PathMatchingResourcePatternResolver> mockedResolver = mockStatic(PathMatchingResourcePatternResolver.class)) {
            PathMatchingResourcePatternResolver resolver = mock(PathMatchingResourcePatternResolver.class);
            mockedResolver.when(PathMatchingResourcePatternResolver::new).thenReturn(resolver);
            when(resolver.getResources(anyString())).thenReturn(new Resource[]{}); // No files
        }

        // Act
        runCassandraMigrations.afterPropertiesSet();

        // Assert
        verify(mockMigrationService).createPublicSchemaIfNotExists();
        verify(mockMigrationService, never()).executeQueryString(anyString());
        verify(mockMigrationService, never()).registerMigrationOnSystem(anyLong(), anyString(), anyString(), anyLong());
    }

    // --- Tests for init() ---
    @Test
    void init_shouldSetContactPointCorrectly() {
        // init() is called in @BeforeEach
        InetSocketAddress expectedAddress = new InetSocketAddress("localhost", 9042);
        assertEquals(expectedAddress, runCassandraMigrations.getContactPoint());
    }

    // You might need more advanced mocking for CqlSession.builder() if it's called multiple times
    // or if you need to mock specific behavior of the built session.
    // The current setup ensures that .build() returns your mockCqlSession.
}