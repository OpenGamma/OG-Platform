/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import com.opengamma.financial.equity.EquityDerivative;
import com.opengamma.financial.equity.EquityDerivativeVisitor;
//!!! (Why) must I import the above?


/**
 * 
 */
public class EquityIndexDividendFuture implements EquityDerivative {
    private final double _dtFixing;
    private final double _dtDelivery;
    private final double _price;
    private final double _strike;
    private final double _pointValue;
    // TODO: Add rest of params reqd for pricing
    
    /**
     * Skeleton. Needs to be described in full
     * @param dtFixing    date-time (in years as a double)  at which the reference index is fixed  
     * @param dtDelivery  date-time (in years as a double)  of settlement
     * @param price      Quoted asset value at time 0 
     * @param strike     Set strike price at trade time, <0
     * @param pointValue The unit value per tick, in given currency  
     * ...
     */
  
  public EquityIndexDividendFuture(final double dtFixing, final double dtDelivery, final double price, final double strike, final double pointValue) {
    // TODO: Check inputs
    
    _dtFixing=dtFixing;
    _dtDelivery=dtDelivery;
    _price=price;
    _strike=strike;
    _pointValue=pointValue;
  }
  
  /**
   * Gets the date when the reference rate is set 
   * @return the fixing date (in years as a double)
   */
  public double getFixingDate() {
    return _dtFixing;
  }
  
  /**
   * Gets the date when payments are made 
   * @return the delivery date (in years as a double)
   */
  public double getDeliveryDate() {
    return _dtDelivery;
  }
  
  @Override
  public <S, T> T accept(final EquityDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitEquityIndexDividendFuture(this, data);
  }

  @Override
  public <T> T accept(final EquityDerivativeVisitor<?, T> visitor) {
    return visitor.visitEquityIndexDividendFuture(this);
  }

}
