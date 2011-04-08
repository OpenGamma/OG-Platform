/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondIborDescription;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * Describes a transaction on a Ibor coupon bond issue.
 */
public class BondIborTransactionDefinition extends BondTransactionDefinition<CouponIborDefinition> {

  /**
   * Constructor of a Ibor coupon bond transaction from all the transaction details.
   * @param underlyingBond The Ibor coupon bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondIborTransactionDefinition(BondIborDescriptionDefinition underlyingBond, double quantity, ZonedDateTime settlementDate, double price) {
    super(underlyingBond, quantity, settlementDate, price);
  }

  @Override
  public BondIborTransaction toDerivative(LocalDate date, String... yieldCurveNames) {
    // First yield curve used for coupon and notional (credit), the second for discounting and the third for forward (Ibor).
    // TODO: Take the ex-coupon days into account.
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final String creditCurveName = yieldCurveNames[0];
    final String discountingCurveName = yieldCurveNames[1];
    final String iborCurveName = yieldCurveNames[2];
    String[] couponCurveName = new String[] {creditCurveName, iborCurveName};
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(zonedDate, getUnderlyingBond().getCalendar(), getUnderlyingBond().getSettlementDays());
    double spotTime = actAct.getDayCountFraction(zonedDate, spot);
    final double settlementTime;
    final double settlementAmount;
    if (getSettlementDate().isBefore(zonedDate)) {
      settlementTime = 0;
      settlementAmount = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(zonedDate, getSettlementDate());
      settlementAmount = getPaymentAmount();
    }
    PaymentFixed settlement = new PaymentFixed(getUnderlyingBond().getCurrency(), settlementTime, settlementAmount, discountingCurveName);
    AnnuityPaymentFixed nominal = getUnderlyingBond().getNominal().toDerivative(date, creditCurveName);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Payment> coupon = (GenericAnnuity<Payment>) getUnderlyingBond().getCoupon().toDerivative(date, couponCurveName);
    AnnuityPaymentFixed nominalPurchase = nominal.trimBefore(settlementTime);
    GenericAnnuity<Payment> couponPurchase = coupon.trimBefore(settlementTime);
    AnnuityPaymentFixed nominalStandard = nominal.trimBefore(spotTime);
    GenericAnnuity<Payment> couponStandard = coupon.trimBefore(spotTime);
    BondIborDescription bondPurchase = new BondIborDescription(nominalPurchase, couponPurchase);
    BondIborDescription bondStandard = new BondIborDescription(nominalStandard, couponStandard);
    int nbCoupon = getUnderlyingBond().getCoupon().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    double notionalStandard = getUnderlyingBond().getCoupon().getNthPayment(couponIndex).getNotional();
    BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), settlement, bondStandard, spotTime, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }
}
