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
 * Derivative implementation for CDS securities
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * 
 * @see CDSDefinition
 * @see CDSSecurity
 * @see InstrumentDerivative
 */
public class CDSDerivative implements InstrumentDerivative {

  private final String _discountCurveName;
  private final String _spreadCurveName;
  private final String _underlyingDiscountCurveName;
  
  private final AnnuityCouponFixed _premium;
  private final AnnuityPaymentFixed _payout;

  private final double _startTime;
  private final double _maturity;
  
  private final double _notional;
  private final double _spread;
  private final double _recoveryRate;
  
  private final boolean _accrualOnDefault;
  private final boolean _payOnDefault;
  private final boolean _protectStart;

  /**
   * Create an (immutable) CDS derivative object ready for pricing
   * 
   * @param discountCurveName Name of the discount curve in the CDS currency (not null)
   * @param spreadCurveName Name of the credit spread curve for the CDS (not null)
   * @param underlyingDiscountCurveName Name of the discount curve in the currency of the underlying bond (may be null)
   * @param premium Derivative object representing the premium payments (not null)
   * @param payout Derivative object representing possible default payouts (may be null)
   * @param startTime Protection start time of the CDS contract (relative to pricing point, may be in the past)
   * @param maturity Maturity of the CDS contract (relative to pricing point)
   * @param notional Notional of the CDS contract
   * @param spread Spread (a.k.a. coupon rate) of the CDS contract
   * @param recoveryRate Recovery rate against the underlying
   * @param accrualOnDefault Whether, in the event of default, accrued interest must be paid for the current period up to the default date
   * @param payOnDefault Whether protection payment is due on default (true) or at maturity (false)
   * @param protectStart Whether the start date is protected (i.e. one extra day of protection)
   */
  public CDSDerivative(final String discountCurveName, final String spreadCurveName, final String underlyingDiscountCurveName,
    final AnnuityCouponFixed premium, final AnnuityPaymentFixed payout,
    final double startTime, final double maturity,
    final double notional, final double spread, final double recoveryRate,
    final boolean accrualOnDefault, final boolean payOnDefault, final boolean protectStart) {
    
    _discountCurveName = discountCurveName;
    _spreadCurveName = spreadCurveName;
    _underlyingDiscountCurveName = underlyingDiscountCurveName;
    
    _premium = premium;
    _payout = payout;
    
    _startTime = startTime;
    _maturity = maturity;
    
    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
    
    _accrualOnDefault = accrualOnDefault;
    _payOnDefault = payOnDefault;
    _protectStart = protectStart;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitCDSDerivative(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCDSDerivative(this);
  }
  
  public String getDiscountCurveName() {
    return _discountCurveName;
  }
  
  public String getSpreadCurveName() {
    return _spreadCurveName;
  }
  
  public String getUnderlyingDiscountCurveName() {
    return _underlyingDiscountCurveName;
  }

  public AnnuityCouponFixed getPremium() {
    return _premium;
  }

  public AnnuityPaymentFixed getPayout() {
    return _payout;
  }
  
  public double getStartTime() {
    return _startTime;
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

  public boolean isAccrualOnDefault() {
    return _accrualOnDefault;
  }

  public boolean isPayOnDefault() {
    return _payOnDefault;
  }
  
  public boolean isProtectStart() {
    return _protectStart;
  }

}
