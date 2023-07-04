create table data_offer_view_count (
    id                 serial                   primary key,
    connector_endpoint text                     not null,
    asset_id           text                     not null,
    date               timestamp with time zone not null,
    FOREIGN KEY (connector_endpoint) REFERENCES connector (endpoint)
);
