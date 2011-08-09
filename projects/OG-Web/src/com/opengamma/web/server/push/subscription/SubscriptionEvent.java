/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import java.net.URL;

/**
 * TODO should this be called an Event? EntityEvent? EntityUpdateEvent? EntityChangeEvent?
 * TODO is this class necessary? just use a string?
 */
public class SubscriptionEvent {

  // TODO this should be a string if we're dealing with partial URLs
  private final String _url;

  public SubscriptionEvent(String url) {
    _url = url;
  }

  public String getUrl() {
    return _url;
  }
}
