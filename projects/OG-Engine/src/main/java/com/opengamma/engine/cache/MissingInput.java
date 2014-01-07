/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import com.opengamma.engine.calcnode.MissingValue;

/**
 * Instances of this class are saved in the computation cache for each item of missing market data.
 * See [PLAT-1262].
 */
public enum MissingInput implements MissingValue {

  /**
   * Value used in place of the input when market data could not be obtained.
   */
  MISSING_MARKET_DATA("Missing market data"),
  /**
   * Value used in place of the input when market data was available but could not be used.
   */
  INSUFFICIENT_MARKET_DATA("Insufficient market data");
    
  private final String _reason;

  private MissingInput(final String reason) {
    _reason = reason;
  }

  public String toString() {
    return _reason;
  }
  
}
