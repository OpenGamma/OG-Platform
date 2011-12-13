/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Describes a transaction on a Ibor coupon bond issue.
 */
public class BondIborTransactionDefinition extends BondTransactionDefinition<PaymentFixedDefinition, CouponIborDefinition> implements
    InstrumentDefinitionWithData<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>, DoubleTimeSeries<ZonedDateTime>> {

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

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    // TODO: review this implementation using the Security toDerivative.
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName};
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot);
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) getUnderlyingBond().getCoupon().toDerivative(date, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final GenericAnnuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final GenericAnnuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime, discountingCurveName);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime, discountingCurveName);
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTS, "index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName};
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final double spotTime = actAct.getDayCountFraction(date, spot);
    final double settlementTime;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate());
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) getUnderlyingBond().getCoupon().toDerivative(date, indexFixingTS, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final GenericAnnuity<Coupon> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final GenericAnnuity<Coupon> couponStandard = coupon.trimBefore(spotTime);
    final BondIborSecurity bondPurchase = new BondIborSecurity(nominalPurchase, couponPurchase, settlementTime, discountingCurveName);
    final BondIborSecurity bondStandard = new BondIborSecurity(nominalStandard, couponStandard, spotTime, discountingCurveName);
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondIborTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondIborTransactionDefinition(this);
  }

}
