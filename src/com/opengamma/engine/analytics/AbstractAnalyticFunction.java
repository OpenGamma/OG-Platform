/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

/**
 * The base class from which most {@link AnalyticFunction} implementations
 * should inherit.
 *
 * @author kirk
 */
public abstract class AbstractAnalyticFunction implements AnalyticFunction {
  private String _uniqueIdentifier;

  /**
   * @return the uniqueIdentifier
   */
  public String getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * @param uniqueIdentifier the uniqueIdentifier to set
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }
  
}
