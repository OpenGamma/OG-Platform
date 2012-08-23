/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.CDSDerivative;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Definition implementation for CDS securities
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * 
 * @see CDSSecurity
 * @see CDSDerivative
 * @see InstrumentDefinition
 */
public class CDSDefinition implements InstrumentDefinition<CDSDerivative> {
  
  private final static DayCount ACT_365F = new ActualThreeSixtyFive();
  
  private final CDSPremiumDefinition _premium;
  private final AnnuityPaymentFixedDefinition _payout;
  
  private final ZonedDateTime _startDate;
  private final ZonedDateTime _maturity;
  
  private final double _notional;
  private final double _spread;
  private final double _recoveryRate;
  
  private final boolean _accrualOnDefault;
  private final boolean _payOnDefault;
  private final boolean _protectStart;
  
  private final DayCount _dayCount;
  
  /**
   * Create an (immutable) CDS definition
   * 
   * @param premium Definition for the premium payments (not null)
   * @param payout Definition for the possible default payouts (may be null)
   * @param startDate Protection start date of the CDS contract (may be in the past, not null)
   * @param maturity Maturity date of the CDS contract (not null)
   * @param notional Notional of the CDS contract
   * @param spread Spread (a.k.a. coupon rate) of the CDS contract
   * @param recoveryRate Recovery rate against the underlying
   * @param accrualOnDefault Whether, in the event of default, accrued interest must be paid for the current period up to the default date
   * @param payOnDefault Whether protection payment is due on default (true) or at maturity (false)
   * @param protectStart Whether the start date is protected (i.e. one extra day of protection)
   */
  public CDSDefinition(final CDSPremiumDefinition premium, final AnnuityPaymentFixedDefinition payout,
    final ZonedDateTime startDate, final ZonedDateTime maturity,
    final double notional, final double spread, final double recoveryRate,
    final boolean accrualOnDefault, final boolean payOnDefault, final boolean protectStart,
    final DayCount dayCount) {
    
    ArgumentChecker.notNull(premium, "premium");
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(maturity, "maturity");
    
    _premium = premium;
    _payout = payout;
    _startDate = startDate;
    _maturity = maturity;
    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
    _accrualOnDefault = accrualOnDefault;
    _payOnDefault = payOnDefault;
    _protectStart = protectStart;
    _dayCount = dayCount;
  }

  /**
   * Create a {@link CDSDerivative} object for pricing relative to the given pricing date 
   * 
   * @param pricingDate Pricing point for offsetting t values
   * @param yieldCurveNames Curve names: 0 = discount, 1 = credit spread, 2 = discount for underlying (optional)
   * @return CDS derivative object ready for pricing
   */
  @Override
  public CDSDerivative toDerivative(ZonedDateTime pricingDate, String... yieldCurveNames) {
    
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length >= 1, "At least one curve required (discount, credit spread, discount for underlying is optional)");
    
    final String discountCurveName = yieldCurveNames[0];
    final String spreadCurveName =  yieldCurveNames.length > 1 ? yieldCurveNames[1] : null;
    final String underlyingDiscountCurveName = yieldCurveNames.length > 2 ? yieldCurveNames[2] : null;
    
    //DayCount dc = new ActualThreeSixtyFive();
    
    // TODO: Time conversions all use TimeCalculator, which uses convention Actual / Actual ISDA
    // TODO: This is also embedded in the toDerivative methods for the annuity classes
    return new CDSDerivative(
      discountCurveName, spreadCurveName, underlyingDiscountCurveName,
      _premium.toDerivative(pricingDate, discountCurveName),
      _payout == null ? null : _payout.toDerivative(pricingDate, discountCurveName),
      getTimeBetween(pricingDate, _startDate),
      getTimeBetween(pricingDate, _maturity),
      _notional, _spread, _recoveryRate, accruedInterest(pricingDate),
      _accrualOnDefault, _payOnDefault, _protectStart
    );
  }
  
  private static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {
    
    return date2.isBefore(date1)
      ? -ACT_365F.getDayCountFraction(date2, date1)
      :  ACT_365F.getDayCountFraction(date1, date2);
  }
  
  public double accruedInterest(final ZonedDateTime pricingDate) {

    final ZonedDateTime stepinDate = pricingDate.plusDays(1); // TODO: Get this value from somewhere
    
    final int nCoupons = _premium.getNumberOfPayments();
    int couponIndex = 0;
    boolean found = false;
    
    for (int i = nCoupons - 1; i >= 0; --i) {
      if (!_premium.getNthPayment(i).getAccrualStartDate().isAfter(stepinDate)) {
        couponIndex = i;
        found = true;
        break;
      }
    }
    
    if (!found) {
      return 0.0;
    }
    
    final CouponFixedDefinition currentPeriod = _premium.getNthPayment(couponIndex);
    final ZonedDateTime previousAccrualDate = currentPeriod.getAccrualStartDate();
    final ZonedDateTime nextAccrualDate = currentPeriod.getAccrualEndDate();
    
    return AccruedInterestCalculator.getAccruedInterest(
      _dayCount, couponIndex, nCoupons, previousAccrualDate, stepinDate, nextAccrualDate, currentPeriod.getRate(), Math.round(1.0 / currentPeriod.getPaymentYearFraction()), /* isEOM */ false)
      * currentPeriod.getNotional();
    
    // TODO: Are settlement days relevant here?
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCDSDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCDSDefinition(this);
  }

}
