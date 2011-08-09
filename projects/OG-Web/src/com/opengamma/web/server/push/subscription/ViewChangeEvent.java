/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import java.net.URL;

/**
 * TODO should this be called an Event? ViewUpdateEvent? ViewChangeEvent?
 */
public class ViewChangeEvent extends SubscriptionEvent {

  private final String _handle;

  public ViewChangeEvent(URL url, String handle) {
    super(url);
    _handle = handle;
  }

  public String getHandle() {
    return _handle;
  }
}
