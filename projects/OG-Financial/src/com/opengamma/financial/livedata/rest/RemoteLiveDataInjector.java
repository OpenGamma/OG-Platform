/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link LiveDataInjector}.
 */
public class RemoteLiveDataInjector implements LiveDataInjector {

  private final URI _baseUri;
  private final FudgeRestClient _client;
  
  public RemoteLiveDataInjector(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }
  
  @Override
  public void addValue(ValueRequirement valueRequirement, Object value) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    ArgumentChecker.notNull(value, "value");
    _client.access(getValueRequirementUri(_baseUri, valueRequirement)).put(value);
  }

  @Override
  public void removeValue(ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    _client.access(getValueRequirementUri(_baseUri, valueRequirement)).delete();
  }
  
  private URI getValueRequirementUri(URI baseUri, ValueRequirement valueRequirement) {
    return UriBuilder.fromUri(baseUri)
        .segment(valueRequirement.getValueName())
        .segment(valueRequirement.getTargetSpecification().getType().name())
        .segment(valueRequirement.getTargetSpecification().getUniqueId().toString()).build();
  }

}
