/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

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

  private final String _cdsCcyCurveName;
  private final String _bondCcyCurveName;
  private final String _spreadCurveName;
  
  private final AnnuityCouponFixed _premium;
  private final AnnuityPaymentFixed _payout;

  private final double _protectionStartTime;
  private final double _maturity;
  
  private final double _notional;
  private final double _spread;
  private final double _recoveryRate;

  public CDSDerivative(final String cdsCcyCurveName, final String bondCcyCurveName, final String spreadCurveName,
    final AnnuityCouponFixed premium, final AnnuityPaymentFixed payout,
    final double protectionStartTime, final double maturity,
    final double notional, final double spread, final double recoveryRate) {
    _cdsCcyCurveName = cdsCcyCurveName;
    _bondCcyCurveName = bondCcyCurveName;
    _spreadCurveName = spreadCurveName;
    
    _premium = premium;
    _payout = payout;
    
    _protectionStartTime = protectionStartTime;
    _maturity = maturity;
    
    _notional = notional;
    _spread = spread;
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
  
  public String getCdsCcyCurveName() {
    return _cdsCcyCurveName;
  }
  
  public String getBondCcyCurveName() {
    return _bondCcyCurveName;
  }
  
  public String getSpreadCurveName() {
    return _spreadCurveName;
  }

  /**
   * 
   * @return Annuity coupon series for premium payments
   */
  public AnnuityCouponFixed getPremium() {
    return _premium;
  }

  /**
   * Returns a series of payouts describing the value that would be paid in the event of a default on each possible default date
   * In reality a maximum of one of these payments will ever be made, payments should be multiplied by the default probability
   * @return Annuity payment series for default payouts
   */
  public AnnuityPaymentFixed getPayout() {
    return _payout;
  }
  
  public double getProtectionStartTime() {
    return _protectionStartTime;
  }
  
  public double getMaturity() {
    return _maturity;
  }

  public double getNotional() {
    return _notional;
  }
  
  public double getSpread() {
    return _spread;
  }
  
  public double getRecoveryRate() {
    return _recoveryRate;
  }

}
