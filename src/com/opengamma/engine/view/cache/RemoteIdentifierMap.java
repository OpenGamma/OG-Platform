/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.engine.view.cache.msg.LookupRequest;
import com.opengamma.engine.view.cache.msg.LookupResponse;

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
    final LookupRequest request = new LookupRequest(Collections.singleton(spec));
    final LookupResponse response = getRemoteCacheClient().sendMessage(request, LookupResponse.class);
    final long identifier = response.getIdentifier().get(0);
    return identifier;
  }

  @Override
  public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
    final LookupRequest request = new LookupRequest(specs);
    final LookupResponse response = getRemoteCacheClient().sendMessage(request, LookupResponse.class);
    final List<Long> identifiers = response.getIdentifier();
    final Map<ValueSpecification, Long> identifierMap = new HashMap<ValueSpecification, Long>();
    int i = 0;
    for (ValueSpecification spec : request.getSpecification()) {
      identifierMap.put(spec, identifiers.get(i++));
    }
    return identifierMap;
  }

}
