/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.msg.IdentifierLookupRequest;
import com.opengamma.engine.view.cache.msg.IdentifierLookupResponse;
import com.opengamma.engine.view.cache.msg.SpecificationLookupRequest;
import com.opengamma.engine.view.cache.msg.SpecificationLookupResponse;

/**
 * Client to a {@link IdentifierMapServer}.
 */
public class RemoteIdentifierMap implements IdentifierMap {

  private final RemoteCacheClient _client;

  public RemoteIdentifierMap(final RemoteCacheClient client) {
    _client = client;
  }

  protected RemoteCacheClient getRemoteCacheClient() {
    return _client;
  }

  @Override
  public long getIdentifier(final ValueSpecification spec) {
    final IdentifierLookupRequest request = new IdentifierLookupRequest(Collections.singleton(spec));
    final IdentifierLookupResponse response = getRemoteCacheClient().sendGetMessage(request, IdentifierLookupResponse.class);
    return response.getIdentifier().get(0);
  }

  @Override
  public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
    final IdentifierLookupRequest request = new IdentifierLookupRequest(specs);
    final IdentifierLookupResponse response = getRemoteCacheClient().sendGetMessage(request, IdentifierLookupResponse.class);
    final List<Long> identifiers = response.getIdentifier();
    final Map<ValueSpecification, Long> identifierMap = new HashMap<ValueSpecification, Long>();
    int i = 0;
    for (ValueSpecification spec : request.getSpecification()) {
      identifierMap.put(spec, identifiers.get(i++));
    }
    return identifierMap;
  }

  @Override
  public ValueSpecification getValueSpecification(long identifier) {
    final SpecificationLookupRequest request = new SpecificationLookupRequest(Collections.singleton(identifier));
    final SpecificationLookupResponse response = getRemoteCacheClient().sendGetMessage(request, SpecificationLookupResponse.class);
    return response.getSpecification().get(0);
  }

  @Override
  public Map<Long, ValueSpecification> getValueSpecifications(Collection<Long> identifiers) {
    final SpecificationLookupRequest request = new SpecificationLookupRequest(identifiers);
    final SpecificationLookupResponse response = getRemoteCacheClient().sendGetMessage(request, SpecificationLookupResponse.class);
    final List<ValueSpecification> specifications = response.getSpecification();
    final Map<Long, ValueSpecification> specificationMap = new HashMap<Long, ValueSpecification>();
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      specificationMap.put(identifier, specifications.get(i++));
    }
    return specificationMap;
  }

}
