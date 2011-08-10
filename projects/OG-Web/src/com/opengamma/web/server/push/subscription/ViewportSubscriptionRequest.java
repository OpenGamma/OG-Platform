/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;

import java.util.List;

/**
 *
 */
public class ViewportSubscriptionRequest {

  private final UniqueId _viewClientId;
  private final String _clientId;
  // TODO timestamps? not sure what they're used for
  private final List<Integer> _rows;
  private final String _handle;

  public ViewportSubscriptionRequest(UniqueId viewClientId, String clientId, List<Integer> rows, String handle) {
    // TODO check args
    _viewClientId = viewClientId;
    _clientId = clientId;
    _rows = rows;
    _handle = handle;
  }

  // this should allow Jersey to create the object from JSON
  public static ViewportSubscriptionRequest valueOf(String json) {
    throw new UnsupportedOperationException("TODO");
  }

  public UniqueId getViewClientId() {
    return _viewClientId;
  }

  public List<Integer> getRows() {
    return _rows;
  }

  public String getHandle() {
    return _handle;
  }

  public ViewportBounds getViewportBounds() {
    // TODO implement ViewportSubscriptionRequest.getViewportBounds()
    throw new UnsupportedOperationException("getViewportBounds not implemented");
  }

  public String getClientId() {
    return _clientId;
  }
}