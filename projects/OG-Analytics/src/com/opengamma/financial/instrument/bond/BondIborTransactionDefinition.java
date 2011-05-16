/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.FixedIncomeInstrumentWithDataConverter;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondIborDescription;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Describes a transaction on a Ibor coupon bond issue.
 */
public class BondIborTransactionDefinition extends BondTransactionDefinition<CouponIborDefinition> implements
    FixedIncomeInstrumentWithDataConverter<BondTransaction<? extends Payment>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * Constructor of a Ibor coupon bond transaction from all the transaction details.
   * @param underlyingBond The Ibor coupon bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondIborTransactionDefinition(final BondIborDescriptionDefinition underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double price) {
    super(underlyingBond, quantity, settlementDate, price);
  }

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    // TODO: Take the ex-coupon days into account.
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName};
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getCalendar(), getUnderlyingBond().getSettlementDays());
    final double spotTime = actAct.getDayCountFraction(date, spot);
    final double settlementTime;
    final double settlementAmount;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
      settlementAmount = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate());
      settlementAmount = getPaymentAmount();
    }
    final PaymentFixed settlement = new PaymentFixed(getUnderlyingBond().getCurrency(), settlementTime, settlementAmount, discountingCurveName);
    final AnnuityPaymentFixed nominal = getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Payment> coupon = (GenericAnnuity<Payment>) getUnderlyingBond().getCoupon().toDerivative(date, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final GenericAnnuity<Payment> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final GenericAnnuity<Payment> couponStandard = coupon.trimBefore(spotTime);
    final BondIborDescription bondPurchase = new BondIborDescription(nominalPurchase, couponPurchase);
    final BondIborDescription bondStandard = new BondIborDescription(nominalStandard, couponStandard);
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), settlement, bondStandard, spotTime, notionalStandard);
    return result;
  }

  @Override
  public BondIborTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    // TODO: Take the ex-coupon days into account.
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTS, "index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    final String[] couponCurveName = new String[] {creditCurveName, iborCurveName};
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getCalendar(), getUnderlyingBond().getSettlementDays());
    final double spotTime = actAct.getDayCountFraction(date, spot);
    final double settlementTime;
    final double settlementAmount;
    if (getSettlementDate().isBefore(date)) {
      settlementTime = 0;
      settlementAmount = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(date, getSettlementDate());
      settlementAmount = getPaymentAmount();
    }
    final PaymentFixed settlement = new PaymentFixed(getUnderlyingBond().getCurrency(), settlementTime, settlementAmount, discountingCurveName);
    final AnnuityPaymentFixed nominal = getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    @SuppressWarnings("unchecked")
    final GenericAnnuity<Payment> coupon = (GenericAnnuity<Payment>) getUnderlyingBond().getCoupon().toDerivative(date, indexFixingTS, couponCurveName);
    final AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    final GenericAnnuity<Payment> couponPurchase = coupon.trimBefore(settlementTime);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    final GenericAnnuity<Payment> couponStandard = coupon.trimBefore(spotTime);
    final BondIborDescription bondPurchase = new BondIborDescription(nominalPurchase, couponPurchase);
    final BondIborDescription bondStandard = new BondIborDescription(nominalStandard, couponStandard);
    final int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    final BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), settlement, bondStandard, spotTime, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    throw new NotImplementedException();
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    throw new NotImplementedException();
  }

}
