/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.cache.msg.IdentifierLookupRequest;
import com.opengamma.engine.cache.msg.IdentifierLookupResponse;
import com.opengamma.engine.cache.msg.SpecificationLookupRequest;
import com.opengamma.engine.cache.msg.SpecificationLookupResponse;
import com.opengamma.engine.value.ValueSpecification;

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
  public Object2LongMap<ValueSpecification> getIdentifiers(Collection<ValueSpecification> specs) {
    final IdentifierLookupRequest request = new IdentifierLookupRequest(specs);
    final IdentifierLookupResponse response = getRemoteCacheClient().sendGetMessage(request, IdentifierLookupResponse.class);
    final List<Long> identifiers = response.getIdentifier();
    final Object2LongMap<ValueSpecification> identifierMap = new Object2LongOpenHashMap<ValueSpecification>();
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
    return MemoryUtils.instance(response.getSpecification().get(0));
  }

  @Override
  public Long2ObjectMap<ValueSpecification> getValueSpecifications(LongCollection identifiers) {
    final SpecificationLookupRequest request = new SpecificationLookupRequest(identifiers);
    final SpecificationLookupResponse response = getRemoteCacheClient().sendGetMessage(request, SpecificationLookupResponse.class);
    final List<ValueSpecification> specifications = response.getSpecification();
    final Long2ObjectMap<ValueSpecification> specificationMap = new Long2ObjectOpenHashMap<ValueSpecification>();
    int i = 0;
    for (Long identifier : request.getIdentifier()) {
      specificationMap.put(identifier, MemoryUtils.instance(specifications.get(i++)));
    }
    return specificationMap;
  }

}
