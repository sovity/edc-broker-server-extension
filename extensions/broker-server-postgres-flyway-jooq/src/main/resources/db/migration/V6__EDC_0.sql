-- Migration Script for Broker from MS8 to EDC 0

-- Migrates an Asset ID
create
    or replace function pg_temp.migrate_asset_id(asset_id text) returns text as
$$
begin
    return replace(replace(asset_id::text, 'urn:artifact:', ''), ':', '-');
end;
$$
    language plpgsql;

-- Migrates a Connector Endpoint to EDC 0
create
    or replace function pg_temp.migrate_connector_endpoint(endpoint text) returns text as
$$
begin
    return pg_temp.replace_suffix(endpoint, '/api/v1/ids/data', '/api/dsp');
end;
$$
    language plpgsql;

-- Creates a valid Asset JSON-LD from an Asset ID and Asset Title
create
    or replace function pg_temp.build_asset_json_ld(asset_id text, asset_title text) returns jsonb as
$$
begin
    return jsonb_build_object(
        '@id', asset_id,
        'https://w3id.org/edc/v0.0.1/ns/properties', jsonb_build_object(
            'https://w3id.org/edc/v0.0.1/ns/id', asset_id,
            'http://purl.org/dc/terms/title', asset_title
            )
        );
end;
$$
    language plpgsql;

-- Utility Function: replaceSuffix
create
    or replace function pg_temp.replace_suffix(str text, old_suffix text, new_suffix text) returns text as
$$
begin
    return case
               when pg_temp.ends_with(str, old_suffix) then
                   left(str, length(str) - length(old_suffix)) || new_suffix
               else
                   str
        end;
end;
$$
    language plpgsql;

-- Utility Function: endsWith
create or replace function pg_temp.ends_with(str text, suffix text)
    returns boolean as
$$
begin
    return right(str, length(suffix)) = suffix;
end;
$$ language plpgsql;


-- Remove Connector Tables
-- All connector tables should be empty
-- There should be no references from broker tables to connector tables
drop table edc_asset cascade;
drop table edc_asset_dataaddress cascade;
drop table edc_asset_property cascade;
drop table edc_contract_agreement cascade;
drop table edc_contract_definitions cascade;
drop table edc_contract_negotiation cascade;
drop table edc_data_plane_instance cascade;
drop table edc_data_request cascade;
drop table edc_lease cascade;
drop table edc_policydefinitions cascade;
drop table edc_transfer_process cascade;


-- Drop constraints
alter table data_offer
    drop constraint data_offer_connector_endpoint_fkey;
alter table data_offer_contract_offer
    drop constraint data_offer_contract_offer_connector_endpoint_fkey;
alter table data_offer_contract_offer
    drop constraint data_offer_contract_offer_connector_endpoint_fkey1;

-- Migrate Connector Endpoints
update broker_event_log
set connector_endpoint = pg_temp.migrate_connector_endpoint(connector_endpoint);
update broker_execution_time_measurement
set connector_endpoint = pg_temp.migrate_connector_endpoint(connector_endpoint);
update connector
set endpoint = pg_temp.migrate_connector_endpoint(endpoint);
update data_offer
set connector_endpoint = pg_temp.migrate_connector_endpoint(connector_endpoint);
update data_offer_contract_offer
set connector_endpoint = pg_temp.migrate_connector_endpoint(connector_endpoint);
update data_offer_view_count
set connector_endpoint = pg_temp.migrate_connector_endpoint(connector_endpoint);


-- Migrate Asset IDs
update broker_event_log
set asset_id = pg_temp.migrate_asset_id(asset_id);
update data_offer
set asset_id = pg_temp.migrate_asset_id(asset_id);
update data_offer_contract_offer
set asset_id = pg_temp.migrate_asset_id(asset_id);
update data_offer_view_count
set asset_id = pg_temp.migrate_asset_id(asset_id);

-- Rename data_offer_contract_offer to contract_offer
alter table data_offer_contract_offer
    rename to contract_offer;

-- Rename Connector ID to Participant ID
alter table connector
    rename column connector_id to participant_id;

-- Add constraints
alter table data_offer
    add constraint data_offer_connector_endpoint_fkey
        foreign key (connector_endpoint) references connector (endpoint);
alter table contract_offer
    add constraint contract_offer_data_offer_fkey
        foreign key (connector_endpoint, asset_id) references data_offer (connector_endpoint, asset_id);
alter table contract_offer
    add constraint contract_offer_connector_fkey
        foreign key (connector_endpoint) references connector (endpoint);

-- Migrate to Asset JSON-LD
alter table data_offer
    rename column asset_properties to asset_json_ld;
alter table data_offer
    rename column asset_name to asset_title;
update data_offer
set asset_json_ld = pg_temp.build_asset_json_ld(asset_id, asset_title);
