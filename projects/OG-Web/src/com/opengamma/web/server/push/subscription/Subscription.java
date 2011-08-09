/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 *
 */
public abstract class Subscription {

  private final SubscriptionType _type;
  /** ID supplied by the client and passed back with notification of a change, not used on the server side */
  private final String _handle;

  /**
   * @param type The type of this subscription
   * @param handle ID supplied by the client and passed back with notification of a change, not used on the server side
   */
  public Subscription(SubscriptionType type, String handle) {
    _type = type;
    _handle = handle;
  }

  /**
   * @return The type of this subscription
   */
  public SubscriptionType getType() {
    return _type;
  }

  public String getHandle() {
    return _handle;
  }
}
