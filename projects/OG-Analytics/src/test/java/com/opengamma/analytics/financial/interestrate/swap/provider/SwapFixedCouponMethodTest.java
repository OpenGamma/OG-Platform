package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueBasisPointCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueBasisPointDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedCouponMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 11, 5);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = INDEX_LIST[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);

  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2013, 9, 9);
  private static final double NOTIONAL = 100000000.0; // 100m
  private static final double RATE = 0.0250; // 2.5%

  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(START_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, true);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(START_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, false);

  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double SHIFT_FD = 1.0E-7;

  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueBasisPointDiscountingCalculator PVBPDC = PresentValueBasisPointDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueBasisPointCurveSensitivityDiscountingCalculator PVBPCSDC = PresentValueBasisPointCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface> PS_PR_C = new SimpleParameterSensitivityParameterCalculator<>(PRCSDC);
  private static final SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface> PS_PVBP_C = new SimpleParameterSensitivityParameterCalculator<>(PVBPCSDC);
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PR_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PRDC, SHIFT_FD);
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PVBP_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVBPDC, SHIFT_FD);

  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0;

  @Test
  /**
   * Tests the par rate.
   */
  public void parRatePayerReceiver() {
    // Payer and receiver have same par rate.
    final double parRateRec = SWAP_RECEIVER.accept(PRDC, MULTICURVES);
    final double parRatePay = SWAP_PAYER.accept(PRDC, MULTICURVES);
    assertEquals("ParRateDiscountingCalculator: swap", parRateRec, parRatePay, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate calculator for the swaps.
   */
  public void parRate() {
    final double ratePayer = SWAP_PAYER.accept(PRDC, MULTICURVES);
    final double rateReceiver = SWAP_RECEIVER.accept(PRDC, MULTICURVES);
    assertEquals("Par Rate swap", ratePayer, rateReceiver, TOLERANCE_RATE);
    final double ratePayer2 = PRDC.visitFixedCouponSwap(SWAP_PAYER, EUR1YEURIBOR6M.getFixedLegDayCount(), MULTICURVES);
    final double rateReceiver2 = PRDC.visitFixedCouponSwap(SWAP_RECEIVER, EUR1YEURIBOR6M.getFixedLegDayCount(), MULTICURVES);
    assertEquals("Par Rate swap", ratePayer2, rateReceiver2, TOLERANCE_RATE);
    assertEquals("Par Rate swap", ratePayer2, rateReceiver, TOLERANCE_RATE);
  }

  @Test
  public void parRateCurveSensitivity() {
    final SimpleParameterSensitivity psComputed = PS_PR_C.calculateSensitivity(SWAP_PAYER, MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity psFD = PS_PR_FDC.calculateSensitivity(SWAP_PAYER, MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", psFD, psComputed, TOLERANCE_RATE_DELTA);
  }

  @Test
  public void presentValueBasisPoint() {
    double pvbpExpected = 0;
    for (int loopcpn = 0; loopcpn < SWAP_PAYER.getFixedLeg().getPayments().length; loopcpn++) {
      pvbpExpected += Math.abs(SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction())
          * MULTICURVES.getDiscountFactor(EUR, SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentTime()) * NOTIONAL;
    }
    final double pvbpComputed = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, MULTICURVES);
    assertEquals("SwapFixedCouponMethod: present value basis point", pvbpExpected, pvbpComputed, TOLERANCE_PV); // one cent out of 100m
  }

  @Test
  public void presentValueBasisPointCurveSensitivity() {
    final SimpleParameterSensitivity psComputed = PS_PVBP_C.calculateSensitivity(SWAP_PAYER, MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity psFD = PS_PVBP_FDC.calculateSensitivity(SWAP_PAYER, MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", psFD, psComputed, TOLERANCE_PV_DELTA);
  }

}
