/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

/**
 * Class with the attributed required to generate an exchange traded (ET) related instrument from the market quotes.
 * The attributes are composed of the flag indicating if the quote is price-like or yield-like.
 */
public class GeneratorAttributeET extends GeneratorAttribute {
  
  /** The Price/Yield flag: True for Price and False for Yield/Rate */
  private final boolean _isPrice;

  /**
   * Constructor.
   * @param isPrice The Price/Yield flag: True for Price and False for Yield/Rate.
   */
  public GeneratorAttributeET(boolean isPrice) {
    _isPrice = isPrice;
  }

  /**
   * Returns the Price/Yield flag: True for Price and False for Yield/Rate.
   * @return The flag.
   */
  public boolean isPrice() {
    return _isPrice;
  }

}
