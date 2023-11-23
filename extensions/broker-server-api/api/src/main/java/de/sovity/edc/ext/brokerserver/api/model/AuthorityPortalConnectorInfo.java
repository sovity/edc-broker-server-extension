package de.sovity.edc.ext.brokerserver.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information for one connector, as required for the Authority Portal.", requiredMode = Schema.RequiredMode.REQUIRED)
public class AuthorityPortalConnectorInfo {
    @Schema(description = "Connector Endpoint", requiredMode = Schema.RequiredMode.REQUIRED)
    private String connectorEndpoint;
    @Schema(description = "Connector Participant ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String participantId;
    @Schema(description = "Number of Data Offers in this connector", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dataOfferCount;
    @Schema(description = "Connector Online Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private ConnectorOnlineStatus onlineStatus;
    @Schema(description = "Last successful refresh time stamp of the online status", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime onlineStatusRefreshedAt;
}
