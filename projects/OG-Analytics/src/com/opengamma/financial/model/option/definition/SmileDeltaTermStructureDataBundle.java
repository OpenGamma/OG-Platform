/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 */
public class SmileDeltaTermStructureDataBundle extends YieldCurveBundle {

  /**
   * The smile parameters.
   */
  private final SmileDeltaTermStructureParameter _smile;
  /**
   * The spot value.
   */
  private final double _spot;

  /**
   * Constructor from the smile parameters and the curves.
   * @param smile The smile parameters.
   * @param spot The spot rate.
   * @param bundle The curves bundle.
   */
  public SmileDeltaTermStructureDataBundle(final SmileDeltaTermStructureParameter smile, final double spot, final YieldCurveBundle bundle) {
    super(bundle);
    Validate.notNull(smile, "Smile parameters");
    _smile = smile;
    _spot = spot;
  }

  /**
   * Gets the smile parameters.
   * @return The smile parameters.
   */
  public SmileDeltaTermStructureParameter getSmile() {
    return _smile;
  }

  /**
   * Gets the spot value.
   * @return The spot.
   */
  public double getSpot() {
    return _spot;
  }

}
