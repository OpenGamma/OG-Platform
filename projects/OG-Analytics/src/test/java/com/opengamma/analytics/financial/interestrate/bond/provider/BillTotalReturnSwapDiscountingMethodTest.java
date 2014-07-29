/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.bond.BillDataSets;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test related to the bond total return swap pricing methodology by discounting of the cash-flows.
 */
public class BillTotalReturnSwapDiscountingMethodTest {

  private static final Currency EUR = Currency.EUR;

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2014, 6, 25);
  private static final ZonedDateTime TERMINATION_DATE = DateUtils.getUTCDate(2014, 12, 22);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2014, 6, 23); // Before effective date.
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2014, 8, 18); // After effective date.

  private static final double EFFECTIVE_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE);
  private static final double EFFECTIVE_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, EFFECTIVE_DATE);
  private static final double TERMINATION_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE);
  private static final double TERMINATION_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, TERMINATION_DATE);

  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 2, 7), DateUtils.getUTCDate(2014, 6, 23),
    DateUtils.getUTCDate(2014, 7, 18) };
  private static final double[] FIXING_RATES = new double[] {0.0040, 0.0041, 0.0042 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATES, FIXING_RATES);

  private static final BillSecurityDefinition BELDEC14_DEFINITION = BillDataSets.billBel_20141218();
  // Funding: unique fixed coupon in EUR: pay TRS bill, receive funding
  private static final double NOTIONAL_TRS = 123456000;
  private static final double NOTIONAL_BILL = 100000000;
  private static final BillSecurity BELDEC14_1 = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE);
  private static final BillSecurity BELDEC14_2 = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_2, EFFECTIVE_DATE);
  // Funding: unique fixed coupon in EUR: receive TRS bond, pay funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(EUR,
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.50, NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_REC_DEFINITION = new PaymentFixedDefinition(EUR, TERMINATION_DATE, NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION, FUNDING_FIXED_NTL_REC_DEFINITION }, BELDEC14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_1 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_2 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_2);
  private static final BillTotalReturnSwap TRS_PAY_FIXED_REC_1 =
      new BillTotalReturnSwap(EFFECTIVE_TIME_1, TERMINATION_TIME_1, FUNDING_LEG_FIXED_REC_1, BELDEC14_1, -NOTIONAL_BILL);
  private static final BillTotalReturnSwap TRS_PAY_FIXED_REC_2 =
      new BillTotalReturnSwap(EFFECTIVE_TIME_2, TERMINATION_TIME_2, FUNDING_LEG_FIXED_REC_2, BELDEC14_2, -NOTIONAL_BILL);
  // Funding: unique fixed coupon in EUR: pay TRS bond, receive funding
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_PAY_DEFINITION = new CouponFixedDefinition(EUR,
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.50, -NOTIONAL_TRS, RATE);
  private static final PaymentFixedDefinition FUNDING_FIXED_NTL_PAY_DEFINITION = new PaymentFixedDefinition(EUR, TERMINATION_DATE, -NOTIONAL_TRS);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_PAY_DEFINITION =
      new AnnuityDefinition<>(new PaymentDefinition[] {FUNDING_FIXED_CPN_PAY_DEFINITION, FUNDING_FIXED_NTL_PAY_DEFINITION }, BELDEC14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_PAY_1 = FUNDING_LEG_FIXED_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final BillTotalReturnSwap TRS_REC_FIXED_PAY_1 =
      new BillTotalReturnSwap(EFFECTIVE_TIME_1, TERMINATION_TIME_1, FUNDING_LEG_FIXED_PAY_1, BELDEC14_1, NOTIONAL_BILL);
  // Funding: multiple USD Libor coupons
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final double SPREAD = 0.0010;
  private static final IborIndex USDLIBOR1M = IndexIborMaster.getInstance().getIndex("USDLIBOR1M");
  private static final Currency USD = USDLIBOR1M.getCurrency();
  private static final AnnuityDefinition<CouponDefinition> FUNDING_LEG_IBOR_PAY_DEFINITION = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(EFFECTIVE_DATE,
      TERMINATION_DATE, NOTIONAL_TRS, SPREAD, USDLIBOR1M, USDLIBOR1M.getDayCount(), USDLIBOR1M.getBusinessDayConvention(), true, USDLIBOR1M.getTenor(),
      USDLIBOR1M.isEndOfMonth(), NYC, StubType.SHORT_START, 0, false, true);
  private static final Annuity<? extends Payment> FUNDING_LEG_IBOR_PAY_1 = FUNDING_LEG_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);
  private static final BillTotalReturnSwapDefinition TRS_REC_IBOR_PAY_DEFINITION = new BillTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE,
      FUNDING_LEG_IBOR_PAY_DEFINITION, BELDEC14_DEFINITION, NOTIONAL_BILL);
  private static final BillTotalReturnSwap TRS_REC_IBOR_PAY_1_EFF = TRS_REC_IBOR_PAY_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);

  private static final BillTotalReturnSwapDiscountingMethod METHOD_TRS_BND = BillTotalReturnSwapDiscountingMethod.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> PV01C =
      new PV01CurveParametersCalculator<>(PVCSIC);
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final IssuerProviderDiscount ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+1;

  @Test
  public void presentValueFixedSameCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedPay = METHOD_TRS_BND.presentValue(TRS_PAY_FIXED_REC_1, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", 1, pvComputedPay.size()); // Bill and funding in same currency
    assertTrue("BillTRSDiscountingMethod: present value", pvComputedPay.getAmount(EUR) != 0.0);
    MultipleCurrencyAmount pvBillUnit = BELDEC14_1.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_1.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBillUnit.multipliedBy(-NOTIONAL_BILL).plus(pvFunding);
    assertEquals("BillTRSDiscountingMethod: present value", pvExpected.getAmount(EUR), pvComputedPay.getAmount(EUR), TOLERANCE_PV);
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_BND.presentValue(TRS_REC_FIXED_PAY_1, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", -pvComputedPay.getAmount(EUR), pvComputedRec.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueFixedSameCurrencyAfterEffective() {
    MultipleCurrencyAmount pvComputed = METHOD_TRS_BND.presentValue(TRS_PAY_FIXED_REC_2, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", 1, pvComputed.size()); // Bill and funding in same currency
    assertTrue("BillTRSDiscountingMethod: present value", pvComputed.getAmount(EUR) != 0.0);
    MultipleCurrencyAmount pvBillUnit = BELDEC14_2.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_FIXED_REC_2.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBillUnit.multipliedBy(-NOTIONAL_BILL).plus(pvFunding);
    assertEquals("BillTRSDiscountingMethod: present value", pvExpected.getAmount(EUR), pvComputed.getAmount(EUR), TOLERANCE_PV); // Bill and funding in same currency
  }

  @Test
  public void presentValueIborDiffCurrencyBeforeEffective() {
    MultipleCurrencyAmount pvComputedRec = METHOD_TRS_BND.presentValue(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", 2, pvComputedRec.size()); // Bill and funding in different currency
    assertTrue("BillTRSDiscountingMethod: present value", pvComputedRec.getAmount(EUR) != 0.0);
    assertTrue("BillTRSDiscountingMethod: present value", pvComputedRec.getAmount(USD) != 0.0);
    MultipleCurrencyAmount pvBillUnit = BELDEC14_1.accept(PVIC, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvFunding = FUNDING_LEG_IBOR_PAY_1.accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvExpected = pvBillUnit.multipliedBy(NOTIONAL_BILL).plus(pvFunding);
    assertEquals("BillTRSDiscountingMethod: present value", pvExpected.getAmount(EUR), pvComputedRec.getAmount(EUR), TOLERANCE_PV);
    assertEquals("BillTRSDiscountingMethod: present value", pvFunding.getAmount(USD), pvComputedRec.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueLegs() {
    MultipleCurrencyAmount pvBillLegExpected = TRS_REC_IBOR_PAY_1_EFF.getAsset().accept(PVIC, ISSUER_MULTICURVE).multipliedBy(NOTIONAL_BILL);
    MultipleCurrencyAmount pvBillLegComputed = METHOD_TRS_BND.presentValueAssetLeg(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", pvBillLegExpected.getAmount(EUR), pvBillLegComputed.getAmount(EUR), TOLERANCE_PV);
    MultipleCurrencyAmount pvFundingLegExpected = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PVDC, ISSUER_MULTICURVE.getMulticurveProvider());
    MultipleCurrencyAmount pvFundingLegComputed = METHOD_TRS_BND.presentValueFundingLeg(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", pvFundingLegExpected.getAmount(USD), pvFundingLegComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_TRS_BND.presentValue(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvCalculator = TRS_REC_IBOR_PAY_1_EFF.accept(PVIC, ISSUER_MULTICURVE);
    assertEquals("BillTRSDiscountingMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
    assertEquals("BillTRSDiscountingMethod: present value", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivty() {
    MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_TRS_BND.presentValueCurveSensitivity(TRS_REC_IBOR_PAY_1_EFF, ISSUER_MULTICURVE).cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsFundingLeg = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PVCSIC, ISSUER_MULTICURVE).cleaned();
    AssertSensitivityObjects.assertEquals("BillTRSDiscountingMethod: present value curve senstivity",
        pvcsFundingLeg.getSensitivity(USD), pvcsComputed.getSensitivity(USD), TOLERANCE_PV_DELTA);
    MultipleCurrencyMulticurveSensitivity pvcsBillLeg = TRS_REC_IBOR_PAY_1_EFF.getAsset().accept(PVCSIC, ISSUER_MULTICURVE).multipliedBy(NOTIONAL_BILL).cleaned();
    AssertSensitivityObjects.assertEquals("BillTRSDiscountingMethod: present value curve senstivity",
        pvcsBillLeg.getSensitivity(EUR), pvcsComputed.getSensitivity(EUR), TOLERANCE_PV_DELTA);
  }

  @Test
  public void pv01() {
    ReferenceAmount<Pair<String, Currency>> pv01Computed = TRS_REC_IBOR_PAY_1_EFF.accept(PV01C, ISSUER_MULTICURVE);
    ReferenceAmount<Pair<String, Currency>> pv01Funding = TRS_REC_IBOR_PAY_1_EFF.getFundingLeg().accept(PV01C, ISSUER_MULTICURVE);
    ReferenceAmount<Pair<String, Currency>> pv01Bill = TRS_REC_IBOR_PAY_1_EFF.getAsset().accept(PV01C, ISSUER_MULTICURVE);
    pv01Bill = pv01Bill.multiplyBy(NOTIONAL_BILL);
    assertEquals("BillTRSDiscountingMethod: pv01", pv01Computed.getMap().size(), 3); // Dsc, Libor, Govt
    assertEquals("BillTRSDiscountingMethod: pv01", pv01Funding.getMap().size(), 2); // Dsc, Libor
    assertEquals("BillTRSDiscountingMethod: pv01", pv01Bill.getMap().size(), 1); // Govt
    double pv01BillExpected = -4823.349602769501; // Hardcoded value
    assertEquals("BillTRSDiscountingMethod: pv01", pv01Bill.getMap().get(Pairs.of(ISSUER_MULTICURVE.getName(BELDEC14_DEFINITION.getIssuerEntity()), EUR)), pv01BillExpected, TOLERANCE_PV_DELTA);
    assertEquals("BillTRSDiscountingMethod: pv01", pv01Bill.getMap().get(Pairs.of(ISSUER_MULTICURVE.getName(BELDEC14_DEFINITION.getIssuerEntity()), EUR)),
        pv01Computed.getMap().get(Pairs.of(ISSUER_MULTICURVE.getName(BELDEC14_DEFINITION.getIssuerEntity()), EUR)), TOLERANCE_PV_DELTA);
  }

}
