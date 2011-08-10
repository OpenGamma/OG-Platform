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
public class ViewportSubscriptionRequest extends SubscriptionRequest {

  private final UniqueId _viewClientId;
  // TODO timestamps? not sure what they're used for
  private final List<Integer> _rows;
  private final String _handle;

  public ViewportSubscriptionRequest(UniqueId viewClientId, List<Integer> rows, String handle) {
    // TODO check args
    _viewClientId = viewClientId;
    _rows = rows;
    _handle = handle;
  }

  @Override
  public void submit(String userId, String clientId, SubscriptionManagerImpl subscriptionManager) {
    subscriptionManager.createViewportSubscription(userId, clientId, this);
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
}
