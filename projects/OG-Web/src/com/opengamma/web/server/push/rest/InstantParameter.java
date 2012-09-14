/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.time.Instant;
import javax.time.InstantProvider;

/**
 * This class is intended to be used for form parameters in JAX-RS request methods that represent instants.
 * Instances of this class are created automatically by the framework which is less verbose than doing a null
 * check and parsing the parameter every time.
 */
public class InstantParameter implements InstantProvider {

  /** The instant, may be null. */
  private final Instant _instant;

  /**
   * Creates an instance from an ISO-8601 format string (as used by {@link Instant#parse}).
   * @param instantStr ISO-8601 format string or null
   */
  public InstantParameter(String instantStr) {
    if (instantStr != null) {
      _instant = Instant.parse(instantStr);
    } else {
      _instant = null;
    }
  }

  /**
   * @return The instant, possibly null
   */
  @Override
  public Instant toInstant() {
    return _instant;
  }
}
