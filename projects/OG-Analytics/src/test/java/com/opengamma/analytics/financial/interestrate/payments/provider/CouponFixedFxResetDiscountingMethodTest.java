/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedFxResetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the pricing method for fixed coupon with FX reset notional.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedFxResetDiscountingMethodTest {
  
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2010, 1, 6);
  
  /** Single coupon. */
  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime FX_DELIVERY_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.04;
  private static final CouponFixedFxResetDefinition CPN_DEFINITION = 
      new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, 
          NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);
  private static final CouponFixedFxReset CPN = CPN_DEFINITION.toDerivative(VALUATION_DATE);
  
  
  private static final MulticurveProviderDiscount MULTICURVE = 
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();  
  
  /** Methods and calculators. */
  private static final CouponFixedFxResetDiscountingMethod METHOD_CPN_FIXED_FX = 
      CouponFixedFxResetDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final CurrencyExposureDiscountingCalculator CEDC = CurrencyExposureDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = 
      new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0;
  
  @Test
  public void presentValue() {
    double fxToday = MULTICURVE.getFxRate(CUR_REF, CUR_PAY);
    double amount = CPN.paymentAmount(fxToday);
    double dfXTp = MULTICURVE.getDiscountFactor(CUR_PAY, CPN.getPaymentTime());
    double dfYT0 = MULTICURVE.getDiscountFactor(CUR_REF, CPN.getFxDeliveryTime());
    double dfXT0 = MULTICURVE.getDiscountFactor(CUR_PAY, CPN.getFxDeliveryTime());
    double pvExpected = amount * dfXTp * dfYT0 / dfXT0;
    MultipleCurrencyAmount pvComputed = METHOD_CPN_FIXED_FX.presentValue(CPN, MULTICURVE);
    assertTrue("CouponFixedFxResetDiscountingMethod: present value", pvComputed.size() == 1);
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        pvExpected, pvComputed.getAmount(CUR_PAY), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueCalculatorVsMethod() {
    MultipleCurrencyAmount pvMethod = METHOD_CPN_FIXED_FX.presentValue(CPN, MULTICURVE);
    MultipleCurrencyAmount pvCalculator = CPN.accept(PVDC, MULTICURVE);
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        pvMethod.getAmount(CUR_PAY), pvCalculator.getAmount(CUR_PAY), TOLERANCE_PV);
  }
  
  @Test
  public void currencyExposure() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_FIXED_FX.presentValue(CPN, MULTICURVE);
    MultipleCurrencyAmount ceComputed = METHOD_CPN_FIXED_FX.currencyExposure(CPN, MULTICURVE);
    assertEquals("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        MULTICURVE.getFxRates().convert(ceComputed, CUR_PAY).getAmount(),
        MULTICURVE.getFxRates().convert(pvComputed, CUR_PAY).getAmount(), TOLERANCE_PV);
    assertTrue("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        Math.abs(ceComputed.getAmount(CUR_REF)) > TOLERANCE_PV);
    double amount = NOTIONAL * ACCRUAL_FACTOR * RATE;
    double dfXTp = MULTICURVE.getDiscountFactor(CUR_PAY, CPN.getPaymentTime());
    double dfYT0 = MULTICURVE.getDiscountFactor(CUR_REF, CPN.getFxDeliveryTime());
    double dfXT0 = MULTICURVE.getDiscountFactor(CUR_PAY, CPN.getFxDeliveryTime());
    double ceExpected = amount * dfXTp * dfYT0 / dfXT0;
    assertEquals("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        ceExpected, ceComputed.getAmount(CUR_REF), TOLERANCE_PV);
  }
  
  @Test
  public void currencyExposureCalculatorVsMethod() {
    MultipleCurrencyAmount ceMethod = METHOD_CPN_FIXED_FX.currencyExposure(CPN, MULTICURVE);
    MultipleCurrencyAmount ceCalculator = CPN.accept(CEDC, MULTICURVE);
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        ceMethod.getAmount(CUR_REF), ceCalculator.getAmount(CUR_REF), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyParameterSensitivity senseCalc1 = PSC.calculateSensitivity(CPN, MULTICURVE);
    MultipleCurrencyParameterSensitivity senseFd1 = PSC_DSC_FD.calculateSensitivity(CPN, MULTICURVE);
    AssertSensitivityObjects.assertEquals("CouponFixedFxResetDiscountingMethod: curve sensitivity", 
         senseCalc1, senseFd1, TOLERANCE_PV_DELTA);    
  }
  
  @Test
  public void presentValueCurveSensitivityCalculatorVsMethod() {
    MultipleCurrencyMulticurveSensitivity pvcsMethod = 
        METHOD_CPN_FIXED_FX.presentValueCurveSensitivity(CPN, MULTICURVE).cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsCalculator = 
        CPN.accept(PVCSDC, MULTICURVE).cleaned();
    AssertSensitivityObjects.assertEquals("CouponFixedFxResetDiscountingMethod: curve sensitivity", 
        pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  /** Swap with FX reset. EUR P3M v USD FX reset P3M*/ 
  private static final Calendar CAL = new CalendarUSD("CAL");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = 
      GENERATOR_IRS_MASTER.getGenerator(GeneratorSwapFixedIborMaster.EUR1YEURIBOR3M, CAL);
  private static final AdjustedDateParameters ADJUSTED_DATE_IBOR = 
      new AdjustedDateParameters(CAL, EUR1YEURIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_IBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, CAL, EUR1YEURIBOR3M.getBusinessDayConvention());
  private static final IborIndex EUREURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2017, 7, 18);
  private static final double FIXED_RATE_1 = 0.0200;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };
  // Ibor leg EUR with exchange notional
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_1_DEFINITION = 
      (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(PAYER_1).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).index(EUREURIBOR3M).accrualPeriodFrequency(EUREURIBOR3M.getTenor()).
      rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_IBOR).accrualPeriodParameters(ADJUSTED_DATE_IBOR).
      dayCount(EUREURIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_IBOR).
      currency(EUREURIBOR3M.getCurrency()).exchangeInitialNotional(true).exchangeFinalNotional(true).
      startDateAdjustmentParameters(ADJUSTED_DATE_IBOR).endDateAdjustmentParameters(ADJUSTED_DATE_IBOR).build();
  // Fixed Leg USD with FX reset EUR with exchange notional
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_FXRESET_FIXED_1_DEFINITION;
  static {
    double sign = PAYER_1 ? 1.0d : -1.0d;
    int nbCpn1 = IBOR_LEG_1_DEFINITION.getNumberOfPayments() - 2; // Remove notional
    CouponDefinition[] cpnFxReset = new CouponDefinition[3 * nbCpn1];
    for(int loopcpn = 0; loopcpn<nbCpn1; loopcpn++) {
      CouponIborDefinition cpnLoop = (CouponIborDefinition) IBOR_LEG_1_DEFINITION.getNthPayment(loopcpn+1);
      cpnFxReset[3 * loopcpn] = new CouponFixedFxResetDefinition(CUR_PAY, cpnLoop.getAccrualStartDate(), 
          cpnLoop.getAccrualStartDate(), cpnLoop.getAccrualStartDate(), 1.0d, -sign * NOTIONAL_1, 1.0d, CUR_REF, 
          cpnLoop.getFixingDate(), cpnLoop.getAccrualStartDate());
      cpnFxReset[1 + 3 * loopcpn] = new CouponFixedFxResetDefinition(CUR_PAY, cpnLoop.getAccrualEndDate(), 
          cpnLoop.getAccrualStartDate(), cpnLoop.getAccrualEndDate(), cpnLoop.getPaymentYearFraction(), 
          sign * NOTIONAL_1, FIXED_RATE_1, CUR_REF, cpnLoop.getFixingDate(), cpnLoop.getAccrualStartDate());
      cpnFxReset[2 + 3 * loopcpn] = new CouponFixedFxResetDefinition(CUR_PAY, cpnLoop.getAccrualEndDate(), 
          cpnLoop.getAccrualEndDate(), cpnLoop.getAccrualEndDate(), 1.0d, sign * NOTIONAL_1, 1.0d, CUR_REF, 
          cpnLoop.getFixingDate(), cpnLoop.getAccrualStartDate());
    }    
    LEG_FXRESET_FIXED_1_DEFINITION = new AnnuityDefinition<>(cpnFxReset, CAL);
  }
  private static final SwapDefinition SWAP_1_DEFINITION = 
      new SwapDefinition(IBOR_LEG_1_DEFINITION, LEG_FXRESET_FIXED_1_DEFINITION);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_1 = SWAP_1_DEFINITION.toDerivative(VALUATION_DATE);

  public void presentValueSwap() {
    MultipleCurrencyAmount pvComputed = SWAP_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvExpected = SWAP_1.getFirstLeg().accept(PVDC, MULTICURVE);
    Annuity<? extends Payment> legFxRest = SWAP_1.getSecondLeg();
    int nbCpn = legFxRest.getNumberOfPayments();
    for(int loopcpn=0; loopcpn<nbCpn; loopcpn++) {
      pvExpected = pvExpected.plus(legFxRest.getNthPayment(loopcpn).accept(PVDC, MULTICURVE));
    }
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        pvExpected.getAmount(CUR_PAY), pvComputed.getAmount(CUR_PAY), TOLERANCE_PV);
  }
  
  public void currencyExposureSwap() {
    MultipleCurrencyAmount ceComputed = SWAP_1.accept(CEDC, MULTICURVE);
    MultipleCurrencyAmount ceExpected = SWAP_1.getFirstLeg().accept(PVDC, MULTICURVE);
    Annuity<? extends Payment> legFxRest = SWAP_1.getSecondLeg();
    int nbCpn = legFxRest.getNumberOfPayments();
    for(int loopcpn=0; loopcpn<nbCpn; loopcpn++) {
      ceExpected = ceExpected.plus(legFxRest.getNthPayment(loopcpn).accept(CEDC, MULTICURVE));
    }
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        ceExpected.getAmount(CUR_REF), ceComputed.getAmount(CUR_REF), TOLERANCE_PV);
  } 
  
  
}
