package de.sovity.edc.ext.brokerserver.services.refreshing.offers;

import de.sovity.edc.ext.brokerserver.BrokerServerExtension;
import de.sovity.edc.ext.brokerserver.services.logging.BrokerEventLogger;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOfferContractOffer;
import org.eclipse.edc.spi.system.configuration.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataOfferLimitsEnforcerTest {
    DataOfferLimitsEnforcer dataOfferLimitsEnforcer;
    Config config;
    BrokerEventLogger brokerEventLogger;

    @BeforeEach
    void setup() {
        config = mock(Config.class);
        brokerEventLogger = mock(BrokerEventLogger.class);
        dataOfferLimitsEnforcer = new DataOfferLimitsEnforcer(config, brokerEventLogger);
    }

    @Test
    void no_limit_zero_and_no_dataofffers_should_result_to_none() {
        // arrange
        int maxDataOffers = -1;
        int maxContractOffers = -1;
        when(config.getInteger(eq(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR), any())).thenReturn(maxDataOffers);
        when(config.getInteger(eq(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR), any())).thenReturn(maxContractOffers);

        List<FetchedDataOffer> dataOffers = List.of();

        // act
        var enforcedLimits = dataOfferLimitsEnforcer.enforceLimits(dataOffers);
        var actual = new ArrayList(enforcedLimits.abbreviatedDataOffers());
        var contractOffersLimitExceeded = enforcedLimits.contractOfferLimitsExceeded();
        var dataOffersLimitExceeded = enforcedLimits.dataOfferLimitsExceeded();

        // assert
        assertThat(actual).isEmpty();
        assertFalse(contractOffersLimitExceeded);
        assertFalse(dataOffersLimitExceeded);
    }

    @Test
    void limit_zero_and_one_dataoffers_should_result_to_none() {
        // arrange
        int maxDataOffers = 0;
        int maxContractOffers = 0;
        when(config.getInteger(eq(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR), any())).thenReturn(maxDataOffers);
        when(config.getInteger(eq(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR), any())).thenReturn(maxContractOffers);

        var dataOffers = List.of(new FetchedDataOffer());

        // act
        var enforcedLimits = dataOfferLimitsEnforcer.enforceLimits(dataOffers);
        var actual = new ArrayList(enforcedLimits.abbreviatedDataOffers());
        var contractOffersLimitExceeded = enforcedLimits.contractOfferLimitsExceeded();
        var dataOffersLimitExceeded = enforcedLimits.dataOfferLimitsExceeded();

        // assert
        assertThat(actual).isEmpty();
        assertFalse(contractOffersLimitExceeded);
        assertTrue(dataOffersLimitExceeded);
    }

    @Test
    void limit_one_and_two_dataoffers_should_result_to_one() {
        // arrange
        int maxDataOffers = 1;
        int maxContractOffers = 1;
        when(config.getInteger(eq(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR), any())).thenReturn(maxDataOffers);
        when(config.getInteger(eq(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR), any())).thenReturn(maxContractOffers);

        var myDataOffer = new FetchedDataOffer();
        myDataOffer.setContractOffers(List.of(new FetchedDataOfferContractOffer(), new FetchedDataOfferContractOffer()));
        var dataOffers = List.of(myDataOffer, myDataOffer);

        // act
        var enforcedLimits = dataOfferLimitsEnforcer.enforceLimits(dataOffers);
        var actual = new ArrayList(enforcedLimits.abbreviatedDataOffers());
        var contractOffersLimitExceeded = enforcedLimits.contractOfferLimitsExceeded();
        var dataOffersLimitExceeded = enforcedLimits.dataOfferLimitsExceeded();

        // assert
        assertThat(actual).hasSize(1);
        assertThat(((FetchedDataOffer) actual.get(0)).getContractOffers()).hasSize(1);
        assertTrue(contractOffersLimitExceeded);
        assertTrue(dataOffersLimitExceeded);
    }
}
