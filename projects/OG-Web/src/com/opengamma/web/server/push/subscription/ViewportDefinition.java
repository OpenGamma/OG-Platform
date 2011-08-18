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
public class ViewportDefinition {

  private final UniqueId _viewClientId;
  private final String _clientId;
  // TODO timestamps? not sure what they're used for
  private final List<Integer> _rows;

  public ViewportDefinition(UniqueId viewClientId, String clientId, List<Integer> rows) {
    // TODO check args
    _viewClientId = viewClientId;
    _clientId = clientId;
    _rows = rows;
  }

  // this should allow Jersey to create the object from JSON
  public static ViewportDefinition valueOf(String json) {
    throw new UnsupportedOperationException("TODO");
  }

  public UniqueId getViewClientId() {
    return _viewClientId;
  }

  public List<Integer> getRows() {
    return _rows;
  }

  public ViewportBounds getViewportBounds() {
    // TODO implement ViewportDefinition.getViewportBounds()
    throw new UnsupportedOperationException("getViewportBounds not implemented");
  }

  public String getClientId() {
    return _clientId;
  }
}
