/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveForwardInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the computation of parameter sensitivity from point sensitivity.
 *
 */
@Test(groups = TestGroup.UNIT)
public class ParameterSensitivityProviderCalculatorTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USD6MLIBOR3M.getCurrency();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2012, 10, 29);
  private static final double NOTIONAL = 100000000;

  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, Period.ofYears(2), USD6MLIBOR3M, NOTIONAL, 0.05, false);
  private static final AnnuityCouponFixedDefinition ANNUITY_DEFINITION = SWAP_DEFINITION.getFixedLeg();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 9, 26);
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final AnnuityCouponFixed ANNUITY = ANNUITY_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final GeneratorSwapFixedON USD1YFEDFUND = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON FEDFUND = USD1YFEDFUND.getIndex();
  private static final SwapFixedONDefinition OIS_DEFINITION = SwapFixedONDefinition.from(EFFECTIVE_DATE, Period.ofMonths(6), NOTIONAL, USD1YFEDFUND, 0.02, false);
  private static final SwapFixedCoupon<Coupon> OIS = OIS_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double[] TIME = {0.25, 0.50, 1.0, 2.0, 5.0};
  private static final double[] YIELD = {0.02, 0.025, 0.03, 0.03, 0.028};
  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final String DSC_NAME = "USD Discounting";
  private static final String FWD3_NAME = "USD Forward 3M";
  private static final YieldAndDiscountCurve DSC = new YieldCurve(DSC_NAME, new InterpolatedDoublesCurve(TIME, YIELD, INTERPOLATOR_LINEAR, true));
  private static final YieldAndDiscountCurve FWD3_DSC = new YieldCurve(FWD3_NAME, new InterpolatedDoublesCurve(TIME, YIELD, INTERPOLATOR_LINEAR, true));
  private static final MulticurveProviderDiscount MARKET_DSC = new MulticurveProviderDiscount();
  static {
    MARKET_DSC.setCurve(USD, DSC);
    MARKET_DSC.setCurve(FEDFUND, DSC);
    MARKET_DSC.setCurve(USDLIBOR3M, FWD3_DSC);
  }
  private static final DoublesCurve FWD3_FWD = new InterpolatedDoublesCurve(TIME, YIELD, INTERPOLATOR_LINEAR, true, FWD3_NAME);
  private static final MulticurveProviderForward MARKET_FWD = new MulticurveProviderForward();
  static {
    MARKET_FWD.setCurve(USD, DSC);
    MARKET_FWD.setCurve(FEDFUND, DSC);
    MARKET_FWD.setCurve(USDLIBOR3M, FWD3_FWD);
  }

  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSC);
  // private static final ParameterSensitivityMatrixMarketCalculator PSC_MAT = new ParameterSensitivityMatrixMarketCalculator(PVCSC);
  private static final double SHIFT = 5.0E-7;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVC, SHIFT);
  private static final ParameterSensitivityMulticurveForwardInterpolatedFDCalculator PSC_FWD_FD = new ParameterSensitivityMulticurveForwardInterpolatedFDCalculator(PVC, SHIFT);

  private static final double TOLERANCE_DELTA = 1.0E+2; // 0.01 currency unit for 1bp on 100m

  @Test
  public void parameterSensitivityBlock() {
    final MultipleCurrencyParameterSensitivity pvpsAnnuityExact = PSC.calculateSensitivity(ANNUITY, MARKET_DSC, MARKET_DSC.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsAnnuityFD = PSC_DSC_FD.calculateSensitivity(ANNUITY, MARKET_DSC);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: fixed annuity ", pvpsAnnuityExact, pvpsAnnuityFD, TOLERANCE_DELTA);

    final MultipleCurrencyParameterSensitivity pvps2AnnuityExact = PSC.calculateSensitivity(ANNUITY, MARKET_FWD, MARKET_FWD.getAllNames());
    final MultipleCurrencyParameterSensitivity pvps2AnnuityFD = PSC_FWD_FD.calculateSensitivity(ANNUITY, MARKET_FWD);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: fixed annuity ", pvps2AnnuityExact, pvps2AnnuityFD, TOLERANCE_DELTA);

    final MultipleCurrencyParameterSensitivity pvpsSwapExact = PSC.calculateSensitivity(SWAP, MARKET_DSC, MARKET_DSC.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsSwapFD = PSC_DSC_FD.calculateSensitivity(SWAP, MARKET_DSC);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: swap ", pvpsSwapExact, pvpsSwapFD, TOLERANCE_DELTA);

    final MultipleCurrencyParameterSensitivity pvps2SwapExact = PSC.calculateSensitivity(SWAP, MARKET_FWD, MARKET_FWD.getAllNames());
    final MultipleCurrencyParameterSensitivity pvps2SwapFD = PSC_FWD_FD.calculateSensitivity(SWAP, MARKET_FWD);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: swap", pvps2SwapExact, pvps2SwapFD, TOLERANCE_DELTA);

    final MultipleCurrencyParameterSensitivity pvpsOisExact = PSC.calculateSensitivity(OIS, MARKET_DSC, MARKET_DSC.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsOisFD = PSC_DSC_FD.calculateSensitivity(OIS, MARKET_DSC);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: Ois", pvpsOisExact, pvpsOisFD, TOLERANCE_DELTA);

    final MultipleCurrencyParameterSensitivity pvps2OisExact = PSC.calculateSensitivity(OIS, MARKET_FWD, MARKET_FWD.getAllNames());
    final MultipleCurrencyParameterSensitivity pvps2OisFD = PSC_FWD_FD.calculateSensitivity(OIS, MARKET_FWD);
    AssertSensitivityObjects.assertEquals("ParameterSensitivityMarketBlockCalculator: Ois", pvps2OisExact, pvps2OisFD, TOLERANCE_DELTA);

    final Set<String> required = new TreeSet<>();
    required.add(DSC_NAME);
    final MultipleCurrencyParameterSensitivity pvpsSwapNoFwd = PSC.calculateSensitivity(SWAP, MARKET_DSC, required);
    assertTrue("ParameterSensitivityMarketBlockCalculator: fixed curve ", pvpsSwapNoFwd.getAllNamesCurrency().size() == 1);
    assertArrayEquals("ParameterSensitivityMarketBlockCalculator: fixed curve ", pvpsSwapNoFwd.getSensitivity(DSC_NAME, USD).getData(), pvpsSwapExact.getSensitivity(DSC_NAME, USD).getData(),
        TOLERANCE_DELTA);
  }

}
