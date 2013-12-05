/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterUnderlyingSensitivityCalculator;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality
 */
@Test(groups = TestGroup.UNIT)
@Deprecated
public abstract class ParameterUnderlyingSensitivityCalculatorTest {

  private static final String DISCOUNTING_CURVE_NAME = "USD Discounting";
  private static final String FORWARD_CURVE_NAME = "USD Forward 3M";
  private static final String[] CURVE_NAMES = new String[] {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  private static final YieldCurveBundle CURVE_BUNDLE_YIELD;
  private static final YieldAndDiscountCurve DISCOUNTING_CURVE_YIELD;
  private static final YieldAndDiscountCurve FORWARD_CURVE_YIELD;

  private static final YieldCurveBundle CURVE_BUNDLE_SPREAD;
  private static final YieldAndDiscountCurve DISCOUNTING_CURVE_SPREAD;
  private static final YieldAndDiscountCurve FORWARD_CURVE_SPREAD;

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR6M = USD6MLIBOR3M.getIborIndex();
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final double SWAP_RATE = 0.05;
  private static final double SWAP_NOTIONAL = 1.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 6, 29);
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, USDLIBOR6M.getSpotLag(), NYC);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLE_DATE, SWAP_TENOR, USD6MLIBOR3M, SWAP_NOTIONAL, SWAP_RATE, true);
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR,
      FLAT_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_CS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, LINEAR_EXTRAPOLATOR,
      FLAT_EXTRAPOLATOR);

  private static final double TOLERANCE_SENSI = 1.0E-6;

  static {
    final double[] dscCurveNodes = new double[] {0.01, 0.5, 1, 1.5, 2.0, 3.1, 4.1, 5, 6.0};
    final double[] fwdCurveNodes = new double[] {0.01, 1, 1.5, 1.9, 3., 4.0, 5.0, 6.0};

    final double[] dscCurveYields = new double[] {0.03, 0.03, 0.04, 0.043, 0.06, 0.03, 0.036, 0.03, 0.03};
    final double[] fwdCurveYields = new double[] {0.03, 0.05, 0.043, 0.048, 0.031, 0.0362, 0.032, 0.032};

    DISCOUNTING_CURVE_YIELD = new YieldCurve(DISCOUNTING_CURVE_NAME, InterpolatedDoublesCurve.fromSorted(dscCurveNodes, dscCurveYields, INTERPOLATOR_DQ));
    FORWARD_CURVE_YIELD = new YieldCurve(FORWARD_CURVE_NAME, InterpolatedDoublesCurve.fromSorted(fwdCurveNodes, fwdCurveYields, INTERPOLATOR_CS));
    final LinkedHashMap<String, YieldAndDiscountCurve> curvesY = new LinkedHashMap<>();
    curvesY.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_YIELD);
    curvesY.put(FORWARD_CURVE_NAME, FORWARD_CURVE_YIELD);
    CURVE_BUNDLE_YIELD = new YieldCurveBundle(curvesY);

    final double spread = 0.01;
    final YieldAndDiscountCurve spreadCurve = YieldCurve.from(new ConstantDoublesCurve(spread));
    DISCOUNTING_CURVE_SPREAD = new YieldCurve(DISCOUNTING_CURVE_NAME, InterpolatedDoublesCurve.fromSorted(dscCurveNodes, dscCurveYields, INTERPOLATOR_DQ));
    FORWARD_CURVE_SPREAD = new YieldAndDiscountAddZeroSpreadCurve("Fwd+Spread", false, DISCOUNTING_CURVE_SPREAD, spreadCurve);
    final LinkedHashMap<String, YieldAndDiscountCurve> curvesS = new LinkedHashMap<>();
    curvesS.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_SPREAD);
    curvesS.put(FORWARD_CURVE_NAME, FORWARD_CURVE_SPREAD);
    CURVE_BUNDLE_SPREAD = new YieldCurveBundle(curvesS);
  }

  protected abstract ParameterUnderlyingSensitivityCalculator getCalculator();

  protected abstract ParameterSensitivityCalculator getNoUnderlyingCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Double> getValueCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument() {
    getCalculator().calculateSensitivity(null, null, CURVE_BUNDLE_YIELD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolatedCurves() {
    getCalculator().calculateSensitivity(SWAP, null, (YieldCurveBundle) null);
  }

  @Test
  /**
   * Tests when the sensitivity to only one curve is computed and not to the underlying (the underlying is fixed).
   */
  public void testWithNoUnderlying() {
    final Set<String> fixedCurve = new HashSet<>();
    fixedCurve.add(DISCOUNTING_CURVE_NAME);
    final DoubleMatrix1D resultU = getCalculator().calculateSensitivity(SWAP, fixedCurve, CURVE_BUNDLE_SPREAD);
    final DoubleMatrix1D resultNoU = getNoUnderlyingCalculator().calculateSensitivity(SWAP, fixedCurve, CURVE_BUNDLE_SPREAD);
    assertEquals("Sensitivity to rates: YieldCurve", resultNoU.getData()[resultNoU.getData().length - 1], resultU.getData()[0], TOLERANCE_SENSI);
    // Implementation note: the sensitivity to the spread parameter is the last one (given by the order of construction).
  }

  protected InstrumentDerivative getSwap() {
    return SWAP;
  }

  protected YieldCurveBundle getCurveBundleSpread() {
    return CURVE_BUNDLE_SPREAD;
  }

  protected double getTolerance() {
    return TOLERANCE_SENSI;
  }
}
