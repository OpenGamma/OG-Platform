/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link LiveMarketDataSourceRegistry}.
 */
public class RemoteLiveMarketDataSourceRegistry implements LiveMarketDataSourceRegistry {

  private final URI _baseUri;
  private FudgeRestClient _client;

  public RemoteLiveMarketDataSourceRegistry(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<String> getDataSources() {
    return _client.access(_baseUri).get(List.class);
  }

}
