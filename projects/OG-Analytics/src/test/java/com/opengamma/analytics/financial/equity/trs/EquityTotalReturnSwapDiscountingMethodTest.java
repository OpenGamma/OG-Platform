/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the equity total return swap pricing methodology by discounting of funding leg the cash-flows.
 */
public class EquityTotalReturnSwapDiscountingMethodTest {

  private static final ZonedDateTime EFFECTIVE_DATE_1 = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime EFFECTIVE_DATE_2 = DateUtils.getUTCDate(2012, 3, 9);
  private static final ZonedDateTime TERMINATION_DATE_1 = DateUtils.getUTCDate(2012, 5, 9);
  private static final ZonedDateTime TERMINATION_DATE_2 = DateUtils.getUTCDate(2012, 9, 9);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 2, 2); // Before effective date.
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 2, 16); // After effective date 1.

  private static final double EFFECTIVE_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  private static final double EFFECTIVE_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE_1);
  private static final double TERMINATION_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_1);
  private static final double TERMINATION_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE_1);

  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 2, 7), DateUtils.getUTCDate(2012, 2, 8),
    DateUtils.getUTCDate(2012, 2, 9), DateUtils.getUTCDate(2012, 3, 7) };
  private static final double[] FIXING_RATES = new double[] {0.0040, 0.0041, 0.0042, 0.0043 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES, FIXING_RATES);

  private static final Currency GBP = Currency.GBP;
  private static final Calendar LON = new CalendarGBP("LON");

  // Equity
  private static final double NB_SHARES = 1000000;
  private static final String OG_NAME = "OpenGamma Ltd";
  private static final String OG_TICKER = "OG";
  private static final LegalEntity OG_LEGAL_ENTITY = new LegalEntity(OG_TICKER, OG_NAME, Sets.newHashSet(CreditRating.of("AAA", "ABC", true)), Sector.of("Technology"),
      Region.of("UK", Country.GB, Currency.GBP));
  private static final EquityDefinition EQUITY_DEFINITION_REC = new EquityDefinition(OG_LEGAL_ENTITY, GBP, NB_SHARES);
  private static final Equity EQUITY_REC = new Equity(OG_LEGAL_ENTITY, GBP, NB_SHARES);
  private static final Equity EQUITY_PAY = new Equity(OG_LEGAL_ENTITY, GBP, -NB_SHARES);
  private static final double DIVIDEND_RATIO = 1.0;

  private static final double NOTIONAL_TRS = 123456000;
  // Funding: unique fixed coupon in GBP: receive TRS equity, pay funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(GBP,
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_REC_DEFINITION = new PaymentFixedDefinition(GBP, TERMINATION_DATE_1, NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION, FUNDING_FIXED_NTL_REC_DEFINITION }, LON);
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_1 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_2 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_2);
  private static final EquityTotalReturnSwap TRS_PAY_FIXED_REC_1 = new EquityTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, EQUITY_PAY,
      NOTIONAL_TRS, GBP, DIVIDEND_RATIO);
  private static final EquityTotalReturnSwap TRS_PAY_FIXED_REC_2 = new EquityTotalReturnSwap(EFFECTIVE_TIME_2_1, TERMINATION_TIME_2_1, FUNDING_LEG_FIXED_REC_2, EQUITY_PAY,
      NOTIONAL_TRS, GBP, DIVIDEND_RATIO);
  // Funding: unique fixed coupon in GBP: pay TRS bond, receive funding
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_PAY_DEFINITION = new CouponFixedDefinition(GBP,
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, -NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_PAY_DEFINITION = new PaymentFixedDefinition(GBP, TERMINATION_DATE_1, -NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_PAY_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_PAY_DEFINITION, FUNDING_FIXED_NTL_PAY_DEFINITION }, LON);
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_PAY_1 = FUNDING_LEG_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final EquityTotalReturnSwap TRS_REC_FIXED_PAY_1 = new EquityTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_PAY_1, EQUITY_REC,
      NOTIONAL_TRS, GBP, DIVIDEND_RATIO);
  // Funding: multiple USD Libor coupons
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double SPREAD = 0.0010;
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AnnuityDefinition<CouponDefinition> FUNDING_LEG_IBOR_PAY_DEFINITION = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(EFFECTIVE_DATE_2,
      TERMINATION_DATE_2, NOTIONAL_TRS, SPREAD, USDLIBOR3M, USDLIBOR3M.getDayCount(), USDLIBOR3M.getBusinessDayConvention(), true, USDLIBOR3M.getTenor(),
      USDLIBOR3M.isEndOfMonth(), NYC, StubType.SHORT_START, 0, false, true);
  private static final Annuity<? extends Payment> FUNDING_LEG_IBOR_PAY_1 = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);
  private static final EquityTotalReturnSwapDefinition TRS_REC_IBOR_PAY_DEFINITION = new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_2, TERMINATION_DATE_2,
      FUNDING_LEG_IBOR_PAY_DEFINITION, EQUITY_DEFINITION_REC, NOTIONAL_TRS, GBP, DIVIDEND_RATIO);
  private static final EquityTotalReturnSwap TRS_REC_IBOR_PAY_1_EFF = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);

  private static final EquityTotalReturnSwapDiscountingMethod METHOD_TRS_EQT = EquityTotalReturnSwapDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveGbpUsd();
  private static final double EQUITY_PRICE = 1.234;
  private static final EquityTrsDataBundle EQUITY_MULTICURVE = new EquityTrsDataBundle(EQUITY_PRICE, MULTICURVE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  public void presentValueFixedSameCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedPay = METHOD_TRS_EQT.presentValue(TRS_PAY_FIXED_REC_1, EQUITY_MULTICURVE);
    assertEquals("EquityTotalReturnSwapDiscountingMethod: present value", 1, pvComputedPay.size()); // Bond and funding in same currency
    assertTrue("EquityTotalReturnSwapDiscountingMethod: present value", pvComputedPay.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(GBP, -NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvEquity.plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputedPay.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_EQT.presentValue(TRS_REC_FIXED_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", -pvComputedPay.getAmount(GBP), pvComputedRec.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  public void presentValueFixedSameCurrencyAfterEffective() {
    MultipleCurrencyAmount pvComputed = METHOD_TRS_EQT.presentValue(TRS_PAY_FIXED_REC_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputed.size()); // Bond and funding in same currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputed.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(GBP, -NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_2.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvEquity.plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputed.getAmount(GBP), TOLERANCE_PV); // Bond and funding in same currency
  }

  @Test
  public void presentValueIborDiffCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_EQT.presentValue(TRS_REC_IBOR_PAY_1_EFF, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 2, pvComputedRec.size()); // Bond and funding in different currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(GBP) != 0.0);
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(USD) != 0.0);
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(GBP, NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_IBOR_PAY_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvEquity.plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputedRec.getAmount(GBP), TOLERANCE_PV);
    assertEquals("BondTRSDiscountingMethod: present value", pvFunding.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueLegs() {
    MultipleCurrencyAmount pvEquityLegExpected = MultipleCurrencyAmount.of(GBP, NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvEquityLegComputed = METHOD_TRS_EQT.presentValueAssetLeg(TRS_REC_IBOR_PAY_1_EFF, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvEquityLegExpected.getAmount(GBP), pvEquityLegComputed.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvFundingLegExpected = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvFundingLegComputed = METHOD_TRS_EQT.presentValueFundingLeg(TRS_REC_IBOR_PAY_1_EFF, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvFundingLegExpected.getAmount(USD), pvFundingLegComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivty() {
    MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_TRS_EQT.presentValueCurveSensitivity(TRS_REC_IBOR_PAY_1_EFF, EQUITY_MULTICURVE).cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsFundingLeg = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PVCSDC, MULTICURVE).cleaned();
    AssertSensitivityObjects.assertEquals("BondTRSDiscountingMethod: present value curve senstivity",
        pvcsFundingLeg.getSensitivity(USD), pvcsComputed.getSensitivity(USD), TOLERANCE_PV_DELTA);
  }

}
