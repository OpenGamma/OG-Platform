/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cds;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;

/**
 * InstrumentDerivative implementation for CDS securities
 * @author Martin Traverse
 * @see CDSDefinition
 * @see CDSSecurity
 * @see InstrumentDerivative
 */
public class CDSDerivative implements InstrumentDerivative {
  
  
  private AnnuityCouponFixed _premium;
  private AnnuityPaymentFixed _payout;
  
  private double _recoveryRate;
  
  public CDSDerivative(final AnnuityCouponFixed premium, final AnnuityPaymentFixed payout, final double recoveryRate) {
    _premium = premium;
    _payout = payout;
    _recoveryRate = recoveryRate;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitCDSDerivative(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCDSDerivative(this);
  }
  
  public AnnuityCouponFixed getPremium() {
    return _premium;
  }
  
  public AnnuityPaymentFixed getPayout() {
    return _payout;
  }
  
  public double getRecoveryRate() {
    return _recoveryRate;
  }

}
