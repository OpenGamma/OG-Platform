/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.bond.BondDataSets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the description of a bond total return swap pricing methodology by discounting of the cash-flows.
 */
public class BondTRSDiscountingMethodTest {

  private static final ZonedDateTime EFFECTIVE_DATE_1 = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime EFFECTIVE_DATE_2 = DateUtils.getUTCDate(2012, 3, 9);
  private static final ZonedDateTime TERMINATION_DATE_1 = DateUtils.getUTCDate(2012, 5, 9);
  private static final ZonedDateTime TERMINATION_DATE_2 = DateUtils.getUTCDate(2012, 9, 9);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 2, 2); // Before effective date.
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 2, 16); // After effective date 1.

  private static final double EFFECTIVE_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  private static final double EFFECTIVE_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE_1);
  private static final double EFFECTIVE_TIME_1_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_2);
  private static final double TERMINATION_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_1);
  private static final double TERMINATION_TIME_1_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_2);
  private static final double TERMINATION_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE_1);

  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 2, 7), DateUtils.getUTCDate(2012, 2, 8),
    DateUtils.getUTCDate(2012, 2, 9), DateUtils.getUTCDate(2012, 3, 7) };
  private static final double[] FIXING_RATES = new double[] {0.0040, 0.0041, 0.0042, 0.0043 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES, FIXING_RATES);

  private static final double NOTIONAL_TRS = 123456000;
  // Bond (UKT)
  private static final double NOTIONAL_BND = 100000000;
  private static final BondFixedSecurityDefinition UKT14_DEFINITION = BondDataSets.bondUKT5_20140907();
  private static final BondFixedSecurity UKT14_1_1 = UKT14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  private static final BondFixedSecurity UKT14_1_2 = UKT14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE_2);
  private static final BondFixedSecurity UKT14_2_1 = UKT14_DEFINITION.toDerivative(REFERENCE_DATE_2, EFFECTIVE_DATE_1);
  private static final Currency GBP = UKT14_DEFINITION.getCurrency();
  // Funding: unique fixed coupon in GBP: receive TRS bond, pay funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(UKT14_DEFINITION.getCurrency(),
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_REC_DEFINITION = new PaymentFixedDefinition(GBP, TERMINATION_DATE_1, NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION, FUNDING_FIXED_NTL_REC_DEFINITION }, UKT14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_1 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_2 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_2);
  private static final BondTotalReturnSwap TRS_PAY_FIXED_REC_1 = new BondTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, UKT14_1_1, -NOTIONAL_BND);
  private static final BondTotalReturnSwap TRS_PAY_FIXED_REC_2 = new BondTotalReturnSwap(EFFECTIVE_TIME_2_1, TERMINATION_TIME_2_1, FUNDING_LEG_FIXED_REC_2, UKT14_2_1, -NOTIONAL_BND);
  // Funding: unique fixed coupon in GBP: pay TRS bond, receive funding
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_PAY_DEFINITION = new CouponFixedDefinition(UKT14_DEFINITION.getCurrency(),
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, -NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_PAY_DEFINITION = new PaymentFixedDefinition(GBP, TERMINATION_DATE_1, -NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_PAY_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_PAY_DEFINITION, FUNDING_FIXED_NTL_PAY_DEFINITION }, UKT14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_PAY_1 = FUNDING_LEG_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final BondTotalReturnSwap TRS_REC_FIXED_PAY_1 = new BondTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_PAY_1, UKT14_1_1, NOTIONAL_BND);
  // Funding: multiple USD Libor coupons
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double SPREAD = 0.0010;
  private static final IborIndex USDLIBOR1M = IndexIborMaster.getInstance().getIndex("USDLIBOR1M");
  private static final Currency USD = USDLIBOR1M.getCurrency();
  private static final AnnuityDefinition<CouponDefinition> FUNDING_LEG_IBOR_PAY_DEFINITION = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(EFFECTIVE_DATE_2,
      TERMINATION_DATE_2, NOTIONAL_TRS, SPREAD, USDLIBOR1M, USDLIBOR1M.getDayCount(), USDLIBOR1M.getBusinessDayConvention(), true, USDLIBOR1M.getTenor(),
      USDLIBOR1M.isEndOfMonth(), NYC, StubType.SHORT_START, 0, false, true);
  private static final Annuity<? extends Payment> FUNDING_LEG_IBOR_PAY_1 = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);
  private static final BondTotalReturnSwapDefinition TRS_REC_IBOR_PAY_DEFINITION = new BondTotalReturnSwapDefinition(EFFECTIVE_DATE_2, TERMINATION_DATE_2,
      FUNDING_LEG_IBOR_PAY_DEFINITION, UKT14_DEFINITION, NOTIONAL_BND);
  private static final BondTotalReturnSwap TRS_REC_IBOR_PAY_1_STD = new BondTotalReturnSwap(EFFECTIVE_TIME_1_2, TERMINATION_TIME_1_2, FUNDING_LEG_IBOR_PAY_1, UKT14_1_1, NOTIONAL_BND);
  private static final BondTotalReturnSwap TRS_REC_IBOR_PAY_1_EFF = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);

  private static final BondTRSDiscountingMethod METHOD_TRS_BND = BondTRSDiscountingMethod.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final IssuerProviderDiscount ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValueFixedSameCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedPay = METHOD_TRS_BND.presentValue(TRS_PAY_FIXED_REC_1, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputedPay.size()); // Bond and funding in same currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedPay.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvBondUnit = UKT14_1_1.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_1.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBondUnit.multipliedBy(-NOTIONAL_BND).plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputedPay.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_BND.presentValue(TRS_REC_FIXED_PAY_1, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", -pvComputedPay.getAmount(GBP), pvComputedRec.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  public void presentValueFixedSameCurrencyAfterEffective() {
    MultipleCurrencyAmount pvComputed = METHOD_TRS_BND.presentValue(TRS_PAY_FIXED_REC_2, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputed.size()); // Bond and funding in same currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputed.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvBondUnit = UKT14_2_1.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_2.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBondUnit.multipliedBy(-NOTIONAL_BND).plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputed.getAmount(GBP), TOLERANCE_PV); // Bond and funding in same currency
  }

  @Test
  public void presentValueIborDiffCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_BND.presentValue(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 2, pvComputedRec.size()); // Bond and funding in different currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(GBP) != 0.0);
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(USD) != 0.0);
    MultipleCurrencyAmount pvBondUnit = UKT14_1_2.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_IBOR_PAY_1.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBondUnit.multipliedBy(NOTIONAL_BND).plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputedRec.getAmount(GBP), TOLERANCE_PV);
    assertEquals("BondTRSDiscountingMethod: present value", pvFunding.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
    // Check that the coupon not in the effective period is not taken into account
    MultipleCurrencyAmount pvComputedRecStd = METHOD_TRS_BND.presentValue(TRS_REC_IBOR_PAY_1_STD, ISSUER_MULTICURVE);
    assertFalse("", Math.abs(pvComputedRec.getAmount(GBP) - pvComputedRecStd.getAmount(GBP)) < TOLERANCE_PV);
  }

  @Test
  public void presentValueIborDiffLeg() {
    MultipleCurrencyAmount pvBondLegExpected = TRS_REC_IBOR_PAY_1_EFF.getAsset().accept(PVIC, ISSUER_MULTICURVE).multipliedBy(NOTIONAL_BND);
    MultipleCurrencyAmount pvBondLegComputed = METHOD_TRS_BND.presentValueAssetLeg(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvBondLegExpected.getAmount(GBP), pvBondLegComputed.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvFundingLegExpected = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvFundingLegComputed = METHOD_TRS_BND.presentValueFundingLeg(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvFundingLegExpected.getAmount(USD), pvFundingLegComputed.getAmount(USD), TOLERANCE_PV);
  }

}
