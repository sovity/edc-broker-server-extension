-- Changes to Enums are non-transactional and must be supplied in a separate migration script for flyway

-- Connector deletion due to inactivity
alter type broker_event_type add value 'CONNECTOR_DELETED_DUE_TO_INACTIVITY';
