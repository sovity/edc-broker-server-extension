create table data_offer_view_count (
    connector_endpoint text                     not null,
    asset_id           text                     not null,
    date               timestamp with time zone not null
);
