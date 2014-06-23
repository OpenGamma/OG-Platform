/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the description of a bill total return swap with an underlying bill and a funding leg.
 */
public class BillTotalReturnSwapDefinitionTest {

  private static final Currency EUR = Currency.EUR;

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2014, 6, 25);
  private static final ZonedDateTime TERMINATION_DATE = DateUtils.getUTCDate(2014, 12, 22);

  private static final BillSecurityDefinition BELDEC14_DEFINITION = BillDataSets.billBel_20141218();
  // Funding: unique fixed coupon in EUR: pay TRS bill, receive funding
  private static final double NOTIONAL_TRS = 123456000;
  private static final double NOTIONAL_BILL = 100000000;
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(EUR,
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.50, NOTIONAL_TRS, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION }, BELDEC14_DEFINITION.getCalendar());
  private static final BillTotalReturnSwapDefinition TRS_PAY_FIXED_REC_DEFINITION =
      new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_FIXED_REC_DEFINITION, BELDEC14_DEFINITION, -NOTIONAL_BILL);
  // Funding: unique fixed coupon in GBP: receive TRS bond, pay funding
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_PAY_DEFINITION = new CouponFixedDefinition(EUR,
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.50, -NOTIONAL_TRS, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_PAY_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_PAY_DEFINITION }, BELDEC14_DEFINITION.getCalendar());
  private static final BillTotalReturnSwapDefinition TRS_REC_FIXED_PAY_DEFINITION =
      new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_FIXED_PAY_DEFINITION, BELDEC14_DEFINITION, NOTIONAL_BILL);
  // Funding: multiple USD Libor coupons
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double SPREAD = 0.0010;
  private static final IborIndex USDLIBOR1M = IndexIborMaster.getInstance().getIndex("USDLIBOR1M");
  private static final AnnuityDefinition<CouponDefinition> FUNDING_LEG_IBOR_PAY_DEFINITION = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(EFFECTIVE_DATE,
      TERMINATION_DATE, NOTIONAL_TRS, SPREAD, USDLIBOR1M, USDLIBOR1M.getDayCount(), USDLIBOR1M.getBusinessDayConvention(), true, USDLIBOR1M.getTenor(),
      USDLIBOR1M.isEndOfMonth(), NYC, StubType.SHORT_START, 0, false, true);
  private static final BillTotalReturnSwapDefinition TRS_REC_IBOR_PAY_DEFINITION =
      new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_IBOR_PAY_DEFINITION, BELDEC14_DEFINITION, NOTIONAL_BILL);

  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 2, 7), DateUtils.getUTCDate(2014, 6, 23),
    DateUtils.getUTCDate(2014, 7, 18) };
  private static final double[] FIXING_RATES = new double[] {0.0040, 0.0041, 0.0042 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES, FIXING_RATES);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2014, 6, 23); // Before effective date.
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2014, 8, 18); // After effective date.

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEffectiveDate() {
    new BillTotalReturnSwapDefinition(null, TERMINATION_DATE, FUNDING_LEG_FIXED_PAY_DEFINITION, BELDEC14_DEFINITION, NOTIONAL_BILL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTerminationDate() {
    new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, null, FUNDING_LEG_FIXED_PAY_DEFINITION, BELDEC14_DEFINITION, NOTIONAL_BILL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFundingLeg() {
    new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, null, BELDEC14_DEFINITION, NOTIONAL_BILL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlyingBond() {
    new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_FIXED_PAY_DEFINITION, null, NOTIONAL_BILL);
  }

  @Test
  public void getter() {
    assertEquals("BondTotalReturnSwapDefinition: getter", EFFECTIVE_DATE, TRS_REC_FIXED_PAY_DEFINITION.getEffectiveDate());
    assertEquals("BondTotalReturnSwapDefinition: getter", TERMINATION_DATE, TRS_REC_FIXED_PAY_DEFINITION.getTerminationDate());
    assertEquals("BondTotalReturnSwapDefinition: getter", FUNDING_LEG_FIXED_PAY_DEFINITION, TRS_REC_FIXED_PAY_DEFINITION.getFundingLeg());
    assertEquals("BondTotalReturnSwapDefinition: getter", BELDEC14_DEFINITION, TRS_REC_FIXED_PAY_DEFINITION.getAsset());
    assertEquals("BondTotalReturnSwapDefinition: getter", NOTIONAL_BILL, TRS_REC_FIXED_PAY_DEFINITION.getQuantity());
  }

  @Test
  public void toDerivativeFixedRecBeforeEffectiveDate() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1);
    BillSecurity bond = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE);
    BillTotalReturnSwap trsExpected = new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, NOTIONAL_BILL);
    BillTotalReturnSwap trsConverted = TRS_REC_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1);
    assertEquals("BillTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

  @Test
  public void toDerivativeFixedPayBeforeEffectiveDate() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
    BillSecurity bond = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE);
    BillTotalReturnSwap trsExpected = new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, -NOTIONAL_BILL);
    BillTotalReturnSwap trsConverted = TRS_PAY_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
    assertEquals("BondTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

  @Test
  public void toDerivativeFixedAfterEffectiveDate() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_2);
    BillSecurity bond = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_2, EFFECTIVE_DATE);
    BillTotalReturnSwap trsExpected = new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, NOTIONAL_BILL);
    BillTotalReturnSwap trsConverted = TRS_REC_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_2);
    assertEquals("BondTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

  @Test
  public void toDerivativeIborBeforeEffectiveDate() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);
    BillSecurity bond = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE);
    BillTotalReturnSwap trsExpected = new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, NOTIONAL_BILL);
    BillTotalReturnSwap trsConverted = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);
    assertEquals("BondTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

  @Test
  public void toDerivativeIborAfterEffectiveDate() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_2, FIXING_TS);
    BillSecurity bond = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_2, EFFECTIVE_DATE);
    BillTotalReturnSwap trsExpected = new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, NOTIONAL_BILL);
    BillTotalReturnSwap trsConverted = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_2, FIXING_TS);
    assertEquals("BondTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

}
