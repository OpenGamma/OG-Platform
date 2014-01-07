/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public abstract class NodeSensitivityCalculatorTestBase {

  private static final String DISCOUNTING_CURVE_NAME = "USD Discounting";
  private static final String FORWARD_CURVE_NAME = "USD Forward 3M";
  private static final String[] CURVE_NAMES = new String[] {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  private static final YieldCurveBundle CURVE_BUNDLE_YIELD;
  private static final YieldAndDiscountCurve DISCOUNTING_YIELD_CURVE;
  private static final YieldAndDiscountCurve FORWARD_YIELD_CURVE;

  private static final YieldCurveBundle CURVE_BUNDLE_DISCOUNTFACTOR;
  private static final YieldAndDiscountCurve DISCOUNTING_DF_CURVE;
  private static final YieldAndDiscountCurve FORWARD_DF_CURVE;

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
    final double[] dscCurveNodes = new double[] {0.01, 0.5, 1, 1.5, 2.0, 3.1, 4.1, 5, 6.0 };
    final double[] fwdCurveNodes = new double[] {0.01, 1, 1.5, 1.9, 3., 4.0, 5.0, 6.0 };

    final double[] dscCurveYields = new double[] {0.03, 0.03, 0.04, 0.043, 0.06, 0.03, 0.036, 0.03, 0.03 };
    final double[] fwdCurveYields = new double[] {0.03, 0.05, 0.043, 0.048, 0.031, 0.0362, 0.032, 0.032 };

    DISCOUNTING_YIELD_CURVE = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(dscCurveNodes, dscCurveYields, INTERPOLATOR_DQ));
    FORWARD_YIELD_CURVE = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(fwdCurveNodes, fwdCurveYields, INTERPOLATOR_CS));
    final LinkedHashMap<String, YieldAndDiscountCurve> curvesY = new LinkedHashMap<>();
    curvesY.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_YIELD_CURVE);
    curvesY.put(FORWARD_CURVE_NAME, FORWARD_YIELD_CURVE);
    CURVE_BUNDLE_YIELD = new YieldCurveBundle(curvesY);

    final double[] dscCurveDf = new double[dscCurveNodes.length];
    for (int loopnode = 0; loopnode < dscCurveNodes.length; loopnode++) {
      dscCurveDf[loopnode] = Math.exp(-dscCurveNodes[loopnode] * dscCurveYields[loopnode]);
    }
    final double[] fwdCurveDf = new double[fwdCurveNodes.length];
    for (int loopnode = 0; loopnode < fwdCurveNodes.length; loopnode++) {
      fwdCurveDf[loopnode] = Math.exp(-fwdCurveNodes[loopnode] * fwdCurveYields[loopnode]);
    }

    DISCOUNTING_DF_CURVE = DiscountCurve.from(InterpolatedDoublesCurve.fromSorted(dscCurveNodes, dscCurveDf, INTERPOLATOR_DQ));
    FORWARD_DF_CURVE = DiscountCurve.from(InterpolatedDoublesCurve.fromSorted(fwdCurveNodes, fwdCurveDf, INTERPOLATOR_CS));
    final LinkedHashMap<String, YieldAndDiscountCurve> curvesDF = new LinkedHashMap<>();
    curvesDF.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_DF_CURVE);
    curvesDF.put(FORWARD_CURVE_NAME, FORWARD_DF_CURVE);
    CURVE_BUNDLE_DISCOUNTFACTOR = new YieldCurveBundle(curvesDF);
  }

  protected abstract NodeYieldSensitivityCalculator getCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> getSensitivityCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Double> getValueCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument() {
    getCalculator().calculateSensitivities(null, getSensitivityCalculator(), null, CURVE_BUNDLE_YIELD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    getCalculator().calculateSensitivities(SWAP, null, null, CURVE_BUNDLE_YIELD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolatedCurves() {
    getCalculator().calculateSensitivities(SWAP, getSensitivityCalculator(), null, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNames() {
    getCalculator().calculateSensitivities(SWAP, getSensitivityCalculator(), new YieldCurveBundle(CURVE_BUNDLE_YIELD), CURVE_BUNDLE_YIELD);
  }

  @Test
  public void testWithKnownYieldCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(DISCOUNTING_CURVE_NAME, DISCOUNTING_YIELD_CURVE);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurveMap = new LinkedHashMap<>();
    fittingCurveMap.put(FORWARD_CURVE_NAME, FORWARD_YIELD_CURVE);
    final YieldCurveBundle fittingCurve = new YieldCurveBundle(fittingCurveMap);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = getValueCalculator();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = getSensitivityCalculator();
    final DoubleMatrix1D result = getCalculator().calculateSensitivities(SWAP, sensitivityCalculator, fixedCurve, fittingCurve);
    final DoubleMatrix1D fdResult = finiteDiffNodeSensitivitiesYield(SWAP, valueCalculator, fixedCurve, fittingCurve);
    assertArrayEquals("Sensitivity to rates: YieldCurve", fdResult.getData(), result.getData(), TOLERANCE_SENSI);
  }

  @Test
  public void testWithKnownDiscountingCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(DISCOUNTING_CURVE_NAME, DISCOUNTING_DF_CURVE);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurveMap = new LinkedHashMap<>();
    fittingCurveMap.put(FORWARD_CURVE_NAME, FORWARD_DF_CURVE);
    final YieldCurveBundle fittingCurve = new YieldCurveBundle(fittingCurveMap);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = getValueCalculator();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = getSensitivityCalculator();
    final DoubleMatrix1D result = getCalculator().calculateSensitivities(SWAP, sensitivityCalculator, fixedCurve, fittingCurve);
    final DoubleMatrix1D resultFdDf = finiteDiffNodeSensitivitiesDsc(SWAP, valueCalculator, fixedCurve, fittingCurve); // Sensi with respect to df
    final double[] resultFdY = new double[resultFdDf.getNumberOfElements()];
    for (int loopnode = 0; loopnode < resultFdDf.getNumberOfElements(); loopnode++) {
      resultFdY[loopnode] = -resultFdDf.getEntry(loopnode) * ((DiscountCurve) FORWARD_DF_CURVE).getCurve().getXData()[loopnode]
          * ((DiscountCurve) FORWARD_DF_CURVE).getCurve().getYData()[loopnode];
    }
    assertArrayEquals("Sensitivity to rates: DiscountingCurve", resultFdY, result.getData(), TOLERANCE_SENSI);
  }

  // TODO: make this a method generally available?
  protected DoubleMatrix1D finiteDiffNodeSensitivitiesYield(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {

    int nNodes = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((YieldCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      nNodes += dataBundle.size();
    }

    final double[] yields = new double[nNodes];
    int index = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((YieldCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        yields[index++] = y;
      }
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(final DoubleMatrix1D x) {
        final YieldCurveBundle curves = interpolatedCurves.copy();
        int index2 = 0;
        for (final String name : interpolatedCurves.getAllNames()) {
          final YieldCurve curve = (YieldCurve) interpolatedCurves.getCurve(name);
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();
          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;
          final YieldAndDiscountCurve newCurve = YieldCurve.from(InterpolatedDoublesCurve.from(dataBundle.getKeys(), yields1, ((InterpolatedDoublesCurve) curve.getCurve()).getInterpolator()));
          curves.replaceCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return ird.accept(valueCalculator, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.differentiate(f);

    return grad.evaluate(new DoubleMatrix1D(yields));

  }

  protected DoubleMatrix1D finiteDiffNodeSensitivitiesDsc(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {
    int nNodes = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      ArgumentChecker.isTrue(interpolatedCurves.getCurve(curveName) instanceof DiscountCurve, "Curve should be DiscountCurve");
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((DiscountCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      nNodes += dataBundle.size();
    }
    final double[] df = new double[nNodes];
    int index = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((DiscountCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        df[index++] = y;
      }
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(final DoubleMatrix1D x) {
        final YieldCurveBundle curves = interpolatedCurves.copy();
        int index2 = 0;
        for (final String name : interpolatedCurves.getAllNames()) {
          final DiscountCurve curve = (DiscountCurve) interpolatedCurves.getCurve(name);
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();
          final double[] df1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;
          final YieldAndDiscountCurve newCurve = DiscountCurve.from(InterpolatedDoublesCurve.from(dataBundle.getKeys(), df1, ((InterpolatedDoublesCurve) curve.getCurve()).getInterpolator()));
          curves.replaceCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return ird.accept(valueCalculator, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.differentiate(f);
    return grad.evaluate(new DoubleMatrix1D(df));
  }

  protected InstrumentDerivative getSwap() {
    return SWAP;
  }

  protected YieldCurveBundle getYieldCurve() {
    return CURVE_BUNDLE_YIELD;
  }

  protected YieldCurveBundle getDiscountCurve() {
    return CURVE_BUNDLE_DISCOUNTFACTOR;
  }

  protected double getTolerance() {
    return TOLERANCE_SENSI;
  }
}
