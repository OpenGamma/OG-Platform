/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import java.util.Map;

/**
 * A set of analytics results for a {@link Viewport}. Currently wraps a {@code Map<String, Object>} for historic
 * reasons but it would be good to switch to being a proper class.
 */
public class ViewportResults {

  /** The actual results */
  private final Map<String, Object> _results;

  public ViewportResults(Map<String, Object> results) {
    _results = results;
  }

  public String toJSON() {
    
  }
}
