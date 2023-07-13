package de.sovity.edc.ext.brokerserver.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Event Log Event Type")
public enum EventLogEventType {
    CONNECTOR_UPDATED,
    CONNECTOR_STATUS_CHANGE_ONLINE,
    CONNECTOR_STATUS_CHANGE_OFFLINE,
    CONNECTOR_STATUS_CHANGE_FORCE_DELETED,
    CONTRACT_OFFER_UPDATED,
    CONTRACT_OFFER_CLICK

}
