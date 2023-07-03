package de.sovity.edc.ext.brokerserver.services.api;

import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.utils.UrlUtils;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(EdcExtension.class)
class ConnectorServiceTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of(
        )));
    }

    @Test
    void addConnector_valid_Endpoints_should_add() {
        TEST_DATABASE.testTransaction(dsl -> {
            var connectorService = new ConnectorService();
            connectorService.addConnector(dsl, "http://localhost1:8080");
            connectorService.addConnector(dsl, "http://localhost2:8080");
            connectorService.addConnector(dsl, "http://localhost3:8080");

            var c = Tables.CONNECTOR;
            var connectorDbRow = dsl.selectFrom(c).execute();

            assertThat(connectorDbRow).isEqualTo(3);
        });
    }

    @Test
    void addConnector_valid_Endpoints_but_one_twice_should_add_once() {
        TEST_DATABASE.testTransaction(dsl -> {
            var connectorService = new ConnectorService();
            connectorService.addConnector(dsl, "http://localhost1:8080");
            connectorService.addConnector(dsl, "http://localhost2:8080");
            connectorService.addConnector(dsl, "http://localhost3:8080");
            connectorService.addConnector(dsl, "http://localhost3:8080");

            var c = Tables.CONNECTOR;
            var connectorDbRow = dsl.selectFrom(c).execute();

            assertThat(connectorDbRow).isEqualTo(3);
        });
    }

    @Test
    void test_urlUtils() {
        assertTrue(UrlUtils.isValidUrl("http://localhost:8080"));
        assertTrue(UrlUtils.isValidUrl(" http://localhost:8080"));

        assertFalse(UrlUtils.isValidUrl("test"));
        assertFalse(UrlUtils.isValidUrl(""));
        assertFalse(UrlUtils.isValidUrl(" "));
        assertFalse(UrlUtils.isValidUrl(null));
    }
}
