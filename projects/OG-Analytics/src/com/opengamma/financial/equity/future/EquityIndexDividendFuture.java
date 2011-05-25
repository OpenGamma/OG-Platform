/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import com.opengamma.financial.equity.EquityDerivative;
import com.opengamma.financial.equity.EquityDerivativeVisitor;

/**
 * 
 */
public class EquityIndexDividendFuture implements EquityDerivative {
  private final double _fixingDate;   
  private final double _deliveryDate;   
  private final double _strike;        
  private final double _pointValue;   
  private final String _indexName;    
  private final String _curveName; 
  
  /**
   * Skeleton. Needs to be described in full
   * @param fixingDate    date-time (in years as a double)  at which the reference index is fixed  
   * @param deliveryDate  date-time (in years as a double)  of settlement
   * @param strike        Set strike price at trade time. Note that we may handle margin by resetting this at the end of each trading day
   * @param pointValue    The unit value per tick, in given currency  
   * @param indexName     Market reference to the underlying dividend index
   * @param curveName     Market reference to the discounting curve
   */
  
  public EquityIndexDividendFuture(final double fixingDate, final double deliveryDate, final double strike, final double pointValue,
          final String indexName, final String curveName) {
    
    _fixingDate = fixingDate;
    _deliveryDate = deliveryDate;
    _strike = strike;
    _pointValue = pointValue;
    _indexName = indexName;
    _curveName = curveName;
  }
  
  /**
   * Gets the date when the reference rate is set 
   * @return the fixing date (in years as a double)
   */
  public double getFixingDate() {
    return _fixingDate;
  }
  
  /**
   * Gets the date when payments are made 
   * @return the delivery date (in years as a double)
   */
  public double getDeliveryDate() {
    return _deliveryDate;
  }
  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }
  /**
   * Gets the point value.
   * @return the point value
   */
  public double getPointValue() {
    return _pointValue;
  }

  @Override
  /// @export "accept-visitor"
  public <S, T> T accept(final EquityDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitEquityIndexDividendFuture(this, data);
  } 
  /// @end
  @Override
  public <T> T accept(final EquityDerivativeVisitor<?, T> visitor) {
    return visitor.visitEquityIndexDividendFuture(this);
  }



}
