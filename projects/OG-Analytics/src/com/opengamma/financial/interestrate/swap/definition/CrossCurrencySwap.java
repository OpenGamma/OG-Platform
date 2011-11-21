/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;


/**
 * 
 */
public class CrossCurrencySwap implements InstrumentDerivative {

  private final FloatingRateNote _domesticLeg;
  private final FloatingRateNote _foreignLeg;
  private final double _spotFX; 


  /**
   * A float-float cross currency swap
   * @param domesticLeg The payments in domestic currency
   * @param foreignLeg The payments in foreign currency
   * @param spotFx units of domestic of one unit of foreign //TODO this is a hack until we can get this information into the calculator 
   */
  public CrossCurrencySwap(FloatingRateNote domesticLeg, FloatingRateNote foreignLeg, final double spotFx) {

    Validate.notNull(domesticLeg, "nulldomesticLeg");
    Validate.notNull(foreignLeg, "null foreignLeg");
    Validate.isTrue(domesticLeg.getCurrency() != foreignLeg.getCurrency(), "Both legs are in same currency");
    _domesticLeg = domesticLeg;
    _foreignLeg = foreignLeg;
    _spotFX = spotFx;
  }
  
  public FloatingRateNote getDomesticLeg() {
    return _domesticLeg;
  }

  public FloatingRateNote getForeignLeg() {
    return _foreignLeg;
  }
  
  //TODO remove when possible 
  public double getSpotFX() {
    return _spotFX;
  }

  
  
  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitCrossCurrencySwap(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCrossCurrencySwap(this);
  }

}
