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
  private final List<ViewportRow> _rows;

  public ViewportDefinition(UniqueId viewClientId, String clientId, List<ViewportRow> rows) {
    // TODO check args
    _viewClientId = viewClientId;
    _rows = rows;
  }

  public UniqueId getViewClientId() {
    return _viewClientId;
  }

  public List<ViewportRow> getRows() {
    return _rows;
  }
}
