/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

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
  public long getIdentifier(ValueSpecification spec) {
    final LookupRequest request = new LookupRequest(spec);
    final LookupResponse response = getRemoteCacheClient().sendMessage(request, LookupResponse.class);
    return response.getIdentifier();
  }

}
