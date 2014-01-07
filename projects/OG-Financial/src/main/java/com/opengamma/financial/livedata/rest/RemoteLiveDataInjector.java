/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link MarketDataInjector}.
 */
public class RemoteLiveDataInjector implements MarketDataInjector {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  public RemoteLiveDataInjector(final URI baseUri) {
    this(baseUri, FudgeRestClient.create());
  }

  public RemoteLiveDataInjector(final URI baseUri, final FudgeRestClient client) {
    _baseUri = baseUri;
    _client = client;
  }

  @Override
  public void addValue(final ValueSpecification valueSpecification, final Object value) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    final AddValueRequest request = new AddValueRequest();
    request.setValueSpecification(valueSpecification);
    request.setValue(value);
    addValue(request);
  }

  @Override
  public void addValue(final ValueRequirement valueRequirement, final Object value) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    final AddValueRequest request = new AddValueRequest();
    request.setValueRequirement(valueRequirement);
    request.setValue(value);
    addValue(request);
  }

  private void addValue(final AddValueRequest request) {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataLiveDataInjectorResource.PATH_ADD).build();
    _client.accessFudge(uri).post(request);
  }

  @Override
  public void removeValue(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    final RemoveValueRequest request = new RemoveValueRequest();
    request.setValueSpecification(valueSpecification);
    removeValue(request);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    final RemoveValueRequest request = new RemoveValueRequest();
    request.setValueRequirement(valueRequirement);
    removeValue(request);
  }

  private void removeValue(final RemoveValueRequest request) {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataLiveDataInjectorResource.PATH_REMOVE).build();
    _client.accessFudge(uri).post(request);
  }

}
