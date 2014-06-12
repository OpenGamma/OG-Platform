/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;

/**
 * ISDA derivative implementation for CDS securities.
 * 
 * Time values are calculated to match the ISDA standard model. The premium is represented
 * using a specialised {@link Annuity} class which uses {@link ISDACDSCoupon} to describe payments.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see InstrumentDerivative
 * @see ISDACDSDefinition
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDACDSDerivative implements InstrumentDerivative {

  private final String _discountCurveName;
  private final String _spreadCurveName;

  private final ISDACDSPremium _premium;

  private final double _startTime;
  private final double _maturity;
  private final double _stepinTime;
  private final double _settlementTime;

  private final double _notional;
  private final double _spread;
  private final double _recoveryRate;
  private final double _accruedInterest;

  private final boolean _accrualOnDefault;
  private final boolean _payOnDefault;
  private final boolean _protectStart;

  // Not needed for ISDA pricing, but used to build the bootstrap CDS for a flat spread
  private final Frequency _couponFrequency;
  private final Convention _convention;
  private final StubType _stubType;

  /**
   * Create an (immutable) CDS derivative object ready for pricing
   * 
   * @param discountCurveName Name of the discount curve in the CDS currency (not null)
   * @param spreadCurveName Name of the credit spread curve for the CDS (not null)
   * @param premium Derivative object representing the premium payments (not null)
   * @param startTime Protection start time of the CDS contract (relative to pricing point, may be in the past)
   * @param maturity Maturity of the CDS contract (relative to pricing point)
   * @param stepinTime Time when step-in becomes effective, relative to pricing point
   * @param settlementTime Settlement time for upfront payment, relative to the pricing point
   * @param notional Notional of the CDS contract
   * @param spread Spread (a.k.a. coupon rate) of the CDS contract
   * @param recoveryRate Recovery rate against the underlying
   * @param accruedInterest Interest accrued at the settlement date; this is an amount in line with the notional
   * @param accrualOnDefault Whether, in the event of default, accrued interest must be paid for the current period up to the default date
   * @param payOnDefault Whether protection payment is due on default (true) or at maturity (false)
   * @param protectStart Whether the start date is protected (i.e. one extra day of protection)
   * @param couponFrequency The premium coupon frequency
   * @param convention The convention data
   * @param stubType the premium schedule stub type
   * @deprecated Use the constructor that does not take curve names.
   */
  @Deprecated
  public ISDACDSDerivative(final String discountCurveName, final String spreadCurveName, final ISDACDSPremium premium, final double startTime, final double maturity, final double stepinTime,
      final double settlementTime, final double notional, final double spread, final double recoveryRate, final double accruedInterest, final boolean accrualOnDefault, final boolean payOnDefault,
      final boolean protectStart, final Frequency couponFrequency, final Convention convention, final StubType stubType) {

    _discountCurveName = discountCurveName;
    _spreadCurveName = spreadCurveName;

    _premium = premium;

    _startTime = startTime;
    _maturity = maturity;
    _stepinTime = stepinTime;
    _settlementTime = settlementTime;

    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
    _accruedInterest = accruedInterest;

    _accrualOnDefault = accrualOnDefault;
    _payOnDefault = payOnDefault;
    _protectStart = protectStart;

    _couponFrequency = couponFrequency;
    _convention = convention;
    _stubType = stubType;
  }

  /**
   * Create an (immutable) CDS derivative object ready for pricing
   * 
   * @param premium Derivative object representing the premium payments (not null)
   * @param startTime Protection start time of the CDS contract (relative to pricing point, may be in the past)
   * @param maturity Maturity of the CDS contract (relative to pricing point)
   * @param stepinTime Time when step-in becomes effective, relative to pricing point
   * @param settlementTime Settlement time for upfront payment, relative to the pricing point
   * @param notional Notional of the CDS contract
   * @param spread Spread (a.k.a. coupon rate) of the CDS contract
   * @param recoveryRate Recovery rate against the underlying
   * @param accruedInterest Interest accrued at the settlement date; this is an amount in line with the notional
   * @param accrualOnDefault Whether, in the event of default, accrued interest must be paid for the current period up to the default date
   * @param payOnDefault Whether protection payment is due on default (true) or at maturity (false)
   * @param protectStart Whether the start date is protected (i.e. one extra day of protection)
   * @param couponFrequency The premium coupon frequency
   * @param convention The convention data
   * @param stubType the premium schedule stub type
   */
  public ISDACDSDerivative(final ISDACDSPremium premium, final double startTime, final double maturity, final double stepinTime, final double settlementTime, final double notional,
      final double spread, final double recoveryRate, final double accruedInterest, final boolean accrualOnDefault, final boolean payOnDefault, final boolean protectStart,
      final Frequency couponFrequency, final Convention convention, final StubType stubType) {

    _discountCurveName = null;
    _spreadCurveName = null;

    _premium = premium;

    _startTime = startTime;
    _maturity = maturity;
    _stepinTime = stepinTime;
    _settlementTime = settlementTime;

    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
    _accruedInterest = accruedInterest;

    _accrualOnDefault = accrualOnDefault;
    _payOnDefault = payOnDefault;
    _protectStart = protectStart;

    _couponFrequency = couponFrequency;
    _convention = convention;
    _stubType = stubType;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCDSDerivative(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCDSDerivative(this);
  }

  /**
   * @return The discounting curve name
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getDiscountCurveName() {
    if (_discountCurveName == null) {
      throw new IllegalStateException("Discounting curve name was not set");
    }
    return _discountCurveName;
  }

  /**
   * @return The spread curve name
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getSpreadCurveName() {
    if (_spreadCurveName == null) {
      throw new IllegalStateException("Spread curve name was not set");
    }
    return _spreadCurveName;
  }

  public ISDACDSPremium getPremium() {
    return _premium;
  }

  public double getStartTime() {
    return _startTime;
  }

  public double getMaturity() {
    return _maturity;
  }

  public double getStepinTime() {
    return _stepinTime;
  }

  public double getSettlementTime() {
    return _settlementTime;
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

  public double getAccruedInterest() {
    return _accruedInterest;
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

  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  public Convention getConvention() {
    return _convention;
  }

  public StubType getStubType() {
    return _stubType;
  }
}
