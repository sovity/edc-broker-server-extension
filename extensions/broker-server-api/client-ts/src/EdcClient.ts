import {
    BrokerServerApi,
    Configuration,
    ConfigurationParameters,
    UIApi,
    UseCaseApi,
} from './generated';

/**
 * API Client for our sovity Broker Server Client
 */
export interface BrokerServerClient {
    uiApi: UIApi;
    useCaseApi: UseCaseApi;
    brokerServerApi: BrokerServerApi;
}

/**
 * Configure & Build new Broker Server Client
 * @param opts opts
 */
export function buildBrokerServerClient(opts: BrokerServerClientOptions): BrokerServerClient {
    const config = new Configuration({
        basePath: opts.managementApiUrl,
        headers: {
            'x-api-key': opts.managementApiKey ?? 'ApiKeyDefaultValue',
        },
        credentials: 'same-origin',
        ...opts.configOverrides,
    });

    return {
        uiApi: new UIApi(config),
        useCaseApi: new UseCaseApi(config),
        brokerServerApi: new BrokerServerApi(config),
    };
}

/**
 * Options for instantiating an EDC API Client
 */
export interface BrokerServerClientOptions {
    managementApiUrl: string;
    managementApiKey?: string;
    configOverrides?: Partial<ConfigurationParameters>;
}
