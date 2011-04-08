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
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final String fundingCurveName = yieldCurveNames[0];
    final double settlementTime;
    final double settlementAmount;
    if (getSettlementDate().isBefore(zonedDate)) {
      settlementTime = 0;
      settlementAmount = 0;
    } else {
      settlementTime = actAct.getDayCountFraction(zonedDate, getSettlementDate());
      settlementAmount = getPaymentAmount();
    }
    PaymentFixed settlement = new PaymentFixed(getUnderlyingBond().getCurrency(), settlementTime, settlementAmount, fundingCurveName);
    AnnuityPaymentFixed nominal = getUnderlyingBond().getNominal().toDerivative(date, yieldCurveNames);
    nominal = nominal.trimBefore(settlementTime);
    @SuppressWarnings("unchecked")
    GenericAnnuity<Payment> coupon = (GenericAnnuity<Payment>) getUnderlyingBond().getCoupon().toDerivative(date, yieldCurveNames);
    BondIborDescription bondPurchase = new BondIborDescription(nominal, coupon, settlementTime);
    BondIborTransaction result = new BondIborTransaction(bondPurchase, getQuantity(), settlement);
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
