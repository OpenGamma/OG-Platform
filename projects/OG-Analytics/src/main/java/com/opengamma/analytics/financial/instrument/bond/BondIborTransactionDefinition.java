/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a transaction on a Ibor coupon bond issue.
 */
public class BondIborTransactionDefinition extends BondTransactionDefinition<PaymentFixedDefinition, CouponIborDefinition>
  implements InstrumentDefinitionWithData<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * Constructor of a Ibor coupon bond transaction from all the transaction details.
   * @param underlyingBond The Ibor coupon bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondIborTransactionDefinition(final BondIborSecurityDefinition underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double price) {
    super(underlyingBond, quantity, settlementDate, price);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    // TODO: review this implementation using the Security toDerivative.
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName };
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot, getUnderlyingBond().getCalendar());
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate(), getUnderlyingBond().getCalendar());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) getUnderlyingBond().getCoupons().toDerivative(date, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final Annuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final Annuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime, discountingCurveName);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime, discountingCurveName);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName };
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot, getUnderlyingBond().getCalendar());
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate(), getUnderlyingBond().getCalendar());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) getUnderlyingBond().getCoupons().toDerivative(date, indexFixingTS, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final Annuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final Annuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime, discountingCurveName);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime, discountingCurveName);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date) {
    // TODO: review this implementation using the Security toDerivative.
    ArgumentChecker.notNull(date, "date");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot, getUnderlyingBond().getCalendar());
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate(), getUnderlyingBond().getCalendar());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) getUnderlyingBond().getCoupons().toDerivative(date);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final Annuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final Annuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot, getUnderlyingBond().getCalendar());
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate(), getUnderlyingBond().getCalendar());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) getUnderlyingBond().getCoupons().toDerivative(date, indexFixingTS);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final Annuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final Annuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborTransactionDefinition(this);
  }

}
