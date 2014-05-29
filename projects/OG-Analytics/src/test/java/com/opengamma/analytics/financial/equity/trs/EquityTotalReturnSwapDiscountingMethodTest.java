/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.equity.PresentValueCurveSensitivityEquityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.equity.PresentValueEquityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the equity total return swap pricing methodology by discounting of funding leg the cash-flows.
 */
public class EquityTotalReturnSwapDiscountingMethodTest {

  private static final ZonedDateTime EFFECTIVE_DATE_1 = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime EFFECTIVE_DATE_2 = DateUtils.getUTCDate(2012, 3, 9);
  private static final ZonedDateTime TERMINATION_DATE_1 = DateUtils.getUTCDate(2012, 5, 9);
  private static final ZonedDateTime TERMINATION_DATE_2 = DateUtils.getUTCDate(2012, 12, 9);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 2, 2); // Before effective date.
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 2, 16); // After effective date 1.
  private static final ZonedDateTime REFERENCE_DATE_3 = DateUtils.getUTCDate(2012, 3, 16); // After effective date 2.

  private static final double EFFECTIVE_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  private static final double EFFECTIVE_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE_1);
  private static final double TERMINATION_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_1);
  private static final double TERMINATION_TIME_2_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE_1);

  private static final ZonedDateTime[] FIXING_DATES_USDLIBOR = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 2, 7), DateUtils.getUTCDate(2012, 2, 8),
    DateUtils.getUTCDate(2012, 2, 9), DateUtils.getUTCDate(2012, 3, 7) };
  private static final double[] FIXING_RATES_USDLIBOR = new double[] {0.0040, 0.0041, 0.0042, 0.0043 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_USDLIBOR = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES_USDLIBOR, FIXING_RATES_USDLIBOR);

  private static final ZonedDateTime[] FIXING_DATES_GBPSONIA = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 2, 7), DateUtils.getUTCDate(2012, 2, 8),
    DateUtils.getUTCDate(2012, 2, 9), DateUtils.getUTCDate(2012, 2, 12), DateUtils.getUTCDate(2012, 2, 13), DateUtils.getUTCDate(2012, 2, 14), DateUtils.getUTCDate(2012, 2, 15),
    DateUtils.getUTCDate(2012, 2, 16) };
  private static final double[] FIXING_RATES_GBPSONIA = new double[] {0.0010, 0.0011, 0.0012, 0.0013, 0.0010, 0.0011, 0.0012, 0.0013 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_GBPSONIA = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES_GBPSONIA, FIXING_RATES_GBPSONIA);

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

  private static final double NOTIONAL_TRS_GBP = 123456000;
  // Funding: unique ON with spread coupon in GBP: receive TRS equity, pay funding
  private static final IndexON GBPSONIA = IndexONMaster.getInstance().getIndex("SONIA");
  private static final double SPREAD_GBP = 0.0010;
  private static final CouponONSpreadDefinition FUNDING_ON_CPN_REC_DEFINITION = new CouponONSpreadDefinition(GBP, TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1,
      GBPSONIA.getDayCount().getDayCountFraction(EFFECTIVE_DATE_1, TERMINATION_DATE_1), NOTIONAL_TRS_GBP, GBPSONIA, EFFECTIVE_DATE_1, TERMINATION_DATE_1, LON, SPREAD_GBP);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_ON_REC_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_ON_CPN_REC_DEFINITION }, LON);
  private static final Annuity<? extends Payment> FUNDING_LEG_ON_REC_1 = FUNDING_LEG_ON_REC_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS_GBPSONIA);
  private static final Annuity<? extends Payment> FUNDING_LEG_ON_REC_2 = FUNDING_LEG_ON_REC_DEFINITION.toDerivative(REFERENCE_DATE_2, FIXING_TS_GBPSONIA);
  private static final EquityTotalReturnSwap TRS_PAY_ON_REC_1 = new EquityTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_ON_REC_1, EQUITY_PAY,
      -NOTIONAL_TRS_GBP, GBP, DIVIDEND_RATIO);
  private static final EquityTotalReturnSwap TRS_PAY_ON_REC_2 = new EquityTotalReturnSwap(EFFECTIVE_TIME_2_1, TERMINATION_TIME_2_1, FUNDING_LEG_ON_REC_2, EQUITY_PAY,
      -NOTIONAL_TRS_GBP, GBP, DIVIDEND_RATIO);
  // Funding: unique ON coupon in GBP: pay TRS bond, receive funding
  private static final CouponONSpreadDefinition FUNDING_ON_CPN_PAY_DEFINITION = new CouponONSpreadDefinition(GBP, TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1,
      GBPSONIA.getDayCount().getDayCountFraction(EFFECTIVE_DATE_1, TERMINATION_DATE_1), -NOTIONAL_TRS_GBP, GBPSONIA, EFFECTIVE_DATE_1, TERMINATION_DATE_1, LON, SPREAD_GBP);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_ON_PAY_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_ON_CPN_PAY_DEFINITION }, LON);
  private static final Annuity<? extends Payment> FUNDING_LEG_ON_PAY_1 = FUNDING_LEG_ON_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS_GBPSONIA);
  private static final EquityTotalReturnSwap TRS_REC_ON_PAY_1 = new EquityTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_ON_PAY_1, EQUITY_REC,
      NOTIONAL_TRS_GBP, GBP, DIVIDEND_RATIO);
  // Funding: multiple USD Libor coupons
  private static final double NOTIONAL_TRS_USD = 199000000;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double SPREAD = 0.0010;
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AnnuityDefinition<CouponIborSpreadDefinition> FUNDING_LEG_IBOR_PAY_DEFINITION = AnnuityDefinitionBuilder.couponIborSpread(EFFECTIVE_DATE_2,
      TERMINATION_DATE_2, USDLIBOR3M.getTenor(), NOTIONAL_TRS_USD, SPREAD, USDLIBOR3M, true, USDLIBOR3M.getDayCount(), USDLIBOR3M.getBusinessDayConvention(), true, NYC, StubType.SHORT_START, 0);
  private static final Annuity<? extends Payment> FUNDING_LEG_IBOR_PAY_1 = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS_USDLIBOR);
  private static final Annuity<? extends Payment> FUNDING_LEG_IBOR_PAY_2 = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_3, FIXING_TS_USDLIBOR);
  private static final EquityTotalReturnSwapDefinition TRS_REC_IBOR_PAY_DEFINITION = new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_2, TERMINATION_DATE_2,
      FUNDING_LEG_IBOR_PAY_DEFINITION, EQUITY_DEFINITION_REC, NOTIONAL_TRS_USD, USD, DIVIDEND_RATIO);
  private static final EquityTotalReturnSwap TRS_REC_IBOR_PAY_1 = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS_USDLIBOR);
  private static final EquityTotalReturnSwap TRS_REC_IBOR_PAY_2 = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_3, FIXING_TS_USDLIBOR);

  private static final EquityTotalReturnSwapDiscountingMethod METHOD_TRS_EQT = EquityTotalReturnSwapDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueEquityDiscountingCalculator PVEDC = PresentValueEquityDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityEquityDiscountingCalculator PVCSEDC = PresentValueCurveSensitivityEquityDiscountingCalculator.getInstance();

  private static final MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveGbpUsd();
  private static final double EQUITY_PRICE = 123.4;
  private static final EquityTrsDataBundle EQUITY_MULTICURVE = new EquityTrsDataBundle(EQUITY_PRICE, MULTICURVE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dividednRatio() {
    EquityTotalReturnSwap wrongRatio = new EquityTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_ON_REC_1, EQUITY_PAY, -NOTIONAL_TRS_GBP, GBP, 0.5);
    METHOD_TRS_EQT.presentValue(wrongRatio, EQUITY_MULTICURVE);
  }

  @Test
  /** Test present value and currency exposure for an example where the funding and equity are in the same currency. 
   * The TRS is paid or received. There is only one ON coupon in the funding. The valuation date is before the TRS effective date  */
  public void presentValueONSameCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedPay = METHOD_TRS_EQT.presentValue(TRS_PAY_ON_REC_1, EQUITY_MULTICURVE);
    assertEquals("EquityTotalReturnSwapDiscountingMethod: present value", 1, pvComputedPay.size()); // Equity and funding in same currency
    assertTrue("EquityTotalReturnSwapDiscountingMethod: present value", pvComputedPay.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(GBP, -NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvPreviousFixing = MultipleCurrencyAmount.of(GBP, NOTIONAL_TRS_GBP * MULTICURVE.getDiscountFactor(GBP, TRS_PAY_ON_REC_1.getFundingLeg().getNthPayment(0).getPaymentTime()));
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_ON_REC_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvEquity.plus(pvPreviousFixing).plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputedPay.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_EQT.presentValue(TRS_REC_ON_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", -pvComputedPay.getAmount(GBP), pvComputedRec.getAmount(GBP), TOLERANCE_PV);
    MultipleCurrencyAmount pvAssetLeg = METHOD_TRS_EQT.presentValueAssetLeg(TRS_PAY_ON_REC_1, EQUITY_MULTICURVE);
    MultipleCurrencyAmount pvFundingLeg = METHOD_TRS_EQT.presentValueFundingLeg(TRS_PAY_ON_REC_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvFundingLeg.plus(pvAssetLeg).getAmount(GBP), TOLERANCE_PV);
    // Currency exposure
    MultipleCurrencyAmount ceComputedPay = METHOD_TRS_EQT.currencyExposure(TRS_PAY_ON_REC_1, EQUITY_MULTICURVE);
    MultipleCurrencyAmount ceComputedRec = METHOD_TRS_EQT.currencyExposure(TRS_REC_ON_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: currency exposure", pvComputedRec.getAmount(GBP), ceComputedRec.getAmount(GBP), TOLERANCE_PV);
    assertEquals("BondTRSDiscountingMethod: currency exposure", pvComputedPay.getAmount(GBP), ceComputedPay.getAmount(GBP), TOLERANCE_PV);
    // Method vs Calculator
    MultipleCurrencyAmount pvCalculatorPay = TRS_PAY_ON_REC_1.accept(PVEDC, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvCalculatorPay.getAmount(GBP), pvComputedPay.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  /** Test present value and currency exposure for an example where the funding and equity are in the same currency. 
   * The TRS is paid or received. The valuation date is after the TRS effective date  */
  public void presentValueONSameCurrencyAfterEffective() {
    MultipleCurrencyAmount pvComputed = METHOD_TRS_EQT.presentValue(TRS_PAY_ON_REC_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputed.size()); // Bond and funding in same currency
    assertTrue("BondTRSDiscountingMethod: present value", pvComputed.getAmount(GBP) != 0.0);
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(GBP, -NB_SHARES * EQUITY_PRICE);
    MultipleCurrencyAmount pvPreviousFixing = MultipleCurrencyAmount.of(GBP, NOTIONAL_TRS_GBP * MULTICURVE.getDiscountFactor(GBP, TRS_PAY_ON_REC_2.getFundingLeg().getNthPayment(0).getPaymentTime()));
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_ON_REC_2.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvEquity.plus(pvPreviousFixing).plus(pvFunding);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(GBP), pvComputed.getAmount(GBP), TOLERANCE_PV); // Equity and funding in same currency
    // Currency exposure
    MultipleCurrencyAmount ceComputedRec = METHOD_TRS_EQT.currencyExposure(TRS_PAY_ON_REC_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: currency exposure", pvComputed.getAmount(GBP), ceComputedRec.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  /** Test present value and currency exposure for an example where the funding and equity are in different currencies. 
   * The TRS is paid. There are 3 Libor coupons in the funding. The valuation date is before the TRS effective date  */
  public void presentValueIborDiffCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_EQT.presentValue(TRS_REC_IBOR_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputedRec.size()); // Converted in funding: USD
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(USD) != 0.0);
    MultipleCurrencyAmount pvEquityGbp = MultipleCurrencyAmount.of(GBP, NB_SHARES * EQUITY_PRICE);
    CurrencyAmount pvEquityUsd = MULTICURVE.getFxRates().convert(pvEquityGbp, USD);
    MultipleCurrencyAmount pvPreviousFixing = MultipleCurrencyAmount.of(USD,
        -NOTIONAL_TRS_USD * MULTICURVE.getDiscountFactor(USD, TRS_REC_IBOR_PAY_1.getFundingLeg().getNthPayment(0).getPaymentTime()));
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_IBOR_PAY_1.getNthPayment(0).accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvPreviousFixing.plus(pvFunding).plus(pvEquityUsd);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pvAssetLeg = METHOD_TRS_EQT.presentValueAssetLeg(TRS_REC_IBOR_PAY_1, EQUITY_MULTICURVE);
    MultipleCurrencyAmount pvFundingLeg = METHOD_TRS_EQT.presentValueFundingLeg(TRS_REC_IBOR_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(USD), pvFundingLeg.plus(pvAssetLeg).getAmount(USD), TOLERANCE_PV);
    // Currency exposure
    MultipleCurrencyAmount ceComputedRec = METHOD_TRS_EQT.currencyExposure(TRS_REC_IBOR_PAY_1, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: currency exposure", 2, ceComputedRec.size()); // GBP equity and USD funding (and notional currency)
    assertEquals("BondTRSDiscountingMethod: currency exposure", MULTICURVE.getFxRates().convert(ceComputedRec, GBP).getAmount(),
        MULTICURVE.getFxRates().convert(pvComputedRec, GBP).getAmount(), TOLERANCE_PV); // CE and PV total should be the same; only conversion is different
    assertEquals("BondTRSDiscountingMethod: currency exposure", pvEquityGbp.getAmount(GBP), ceComputedRec.getAmount(GBP), TOLERANCE_PV);
    // Method vs Calculator
    MultipleCurrencyAmount pvCalculatorRec = TRS_REC_IBOR_PAY_1.accept(PVEDC, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvCalculatorRec.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Test present value and currency exposure for an example where the funding and equity are in different currencies. 
   * The TRS is paid. There are 3 Libor coupons in the funding. The valuation date is after the TRS effective date, in the first funding period.  */
  public void presentValueIborDiffCurrencyAfterEffective() {
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_EQT.presentValue(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", 1, pvComputedRec.size()); // Converted in funding: USD
    assertTrue("BondTRSDiscountingMethod: present value", pvComputedRec.getAmount(USD) != 0.0);
    MultipleCurrencyAmount pvEquityGbp = MultipleCurrencyAmount.of(GBP, NB_SHARES * EQUITY_PRICE);
    CurrencyAmount pvEquityUsd = MULTICURVE.getFxRates().convert(pvEquityGbp, USD);
    MultipleCurrencyAmount pvPreviousFixing = MultipleCurrencyAmount.of(USD,
        -NOTIONAL_TRS_USD * MULTICURVE.getDiscountFactor(USD, TRS_REC_IBOR_PAY_2.getFundingLeg().getNthPayment(0).getPaymentTime()));
    MultipleCurrencyAmount pvFunding0 = FUNDING_LEG_IBOR_PAY_2.getNthPayment(0).accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = pvPreviousFixing.plus(pvFunding0).plus(pvEquityUsd);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pvAssetLeg = METHOD_TRS_EQT.presentValueAssetLeg(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE);
    MultipleCurrencyAmount pvFundingLeg = METHOD_TRS_EQT.presentValueFundingLeg(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: present value", pvExpected.getAmount(USD), pvFundingLeg.plus(pvAssetLeg).getAmount(USD), TOLERANCE_PV);
    // Currency exposure
    MultipleCurrencyAmount ceComputedRec = METHOD_TRS_EQT.currencyExposure(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE);
    assertEquals("BondTRSDiscountingMethod: currency exposure", 2, ceComputedRec.size()); // GBP equity and USD funding (and notional currency)
    assertEquals("BondTRSDiscountingMethod: currency exposure", MULTICURVE.getFxRates().convert(ceComputedRec, GBP).getAmount(),
        MULTICURVE.getFxRates().convert(pvComputedRec, GBP).getAmount(), TOLERANCE_PV); // CE and PV total should be the same; only conversion is different
    assertEquals("BondTRSDiscountingMethod: currency exposure", pvEquityGbp.getAmount(GBP), ceComputedRec.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  /** Test present value curve sensitivity for an example where the funding and equity are in different currencies. 
   * The TRS is paid. There are 3 Libor coupons in the funding. The valuation date is after the TRS effective date, in the first funding period.  */
  public void presentValueCurveSensitivityIborDiffCurrencyAfterEffective() {
    MultipleCurrencyMulticurveSensitivity pvcsComputedRec = METHOD_TRS_EQT.presentValueCurveSensitivity(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE).cleaned();
    double amount = -NOTIONAL_TRS_USD * MULTICURVE.getDiscountFactor(USD, TRS_REC_IBOR_PAY_2.getFundingLeg().getNthPayment(0).getPaymentTime());
    double time = TRS_REC_IBOR_PAY_2.getFundingLeg().getNthPayment(0).getPaymentTime();
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final DoublesPair s = DoublesPair.of(time, -time * amount);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    mapDsc.put(MULTICURVE.getName(USD), list);
    MultipleCurrencyMulticurveSensitivity pvcsPreviousFixing = new MultipleCurrencyMulticurveSensitivity();
    pvcsPreviousFixing = pvcsPreviousFixing.plus(USD, MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    MultipleCurrencyMulticurveSensitivity pvcsFunding0 = FUNDING_LEG_IBOR_PAY_2.getNthPayment(0).accept(PVCSDC, MULTICURVE);
    MultipleCurrencyMulticurveSensitivity pvcsExpected = pvcsPreviousFixing.plus(pvcsFunding0).cleaned();
    AssertSensitivityObjects.assertEquals("", pvcsExpected, pvcsComputedRec, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    MultipleCurrencyMulticurveSensitivity pvcsComputedRec = METHOD_TRS_EQT.presentValueCurveSensitivity(TRS_REC_IBOR_PAY_2, EQUITY_MULTICURVE).cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsCalculator = TRS_REC_IBOR_PAY_2.accept(PVCSEDC, EQUITY_MULTICURVE).cleaned();
    AssertSensitivityObjects.assertEquals("", pvcsComputedRec, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}
