/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class NodeSensitivityCalculatorTest {

  private static final String FUNDING_CURVE_NAME = "funding";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static final NodeSensitivityCalculator NSC = new NodeSensitivityCalculator();
  private static final InterestRateDerivative IRD;
  private static final LinkedHashMap<String, YieldAndDiscountCurve> INTERPOLATED_CURVES;
  private static final YieldAndDiscountCurve FUNDING_CURVE;
  private static final YieldAndDiscountCurve LIBOR_CURVE;

  static {
    final double[] fixedPaymentTimes = new double[] {1, 2, 3, 4, 5};
    final double[] floatingPaymentTimes = new double[] {0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};

    final double[] fundingCurveNodes = new double[] {0.5, 1, 1.5, 2.0, 3.1, 4.1, 5};
    final double[] fundingCurveYields = new double[] {0.03, 0.04, 0.043, 0.046, 0.4, 0.036, 0.03};
    final double[] liborCurveNodes = new double[] {1, 1.5, 1.9, 3., 4.0, 6.0};
    final double[] liborCurveYields = new double[] {0.041, 0.043, 0.048, 0.41, 0.0362, 0.032};

    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

    FUNDING_CURVE = new YieldCurve(InterpolatedDoublesCurve.fromSorted(fundingCurveNodes, fundingCurveYields, extrapolator));
    extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
    LIBOR_CURVE = new YieldCurve(InterpolatedDoublesCurve.fromSorted(liborCurveNodes, liborCurveYields, extrapolator));

    INTERPOLATED_CURVES = new LinkedHashMap<String, YieldAndDiscountCurve>();
    INTERPOLATED_CURVES.put(FUNDING_CURVE_NAME, FUNDING_CURVE);
    INTERPOLATED_CURVES.put(LIBOR_CURVE_NAME, LIBOR_CURVE);

    final double couponRate = 0.07;
    IRD = new FixedFloatSwap(fixedPaymentTimes, floatingPaymentTimes, couponRate, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInstrument() {
    NSC.calculate(null, PresentValueSensitivityCalculator.getInstance(), null, INTERPOLATED_CURVES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    NSC.calculate(IRD, null, null, INTERPOLATED_CURVES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolatedCurves() {
    NSC.calculate(IRD, PresentValueSensitivityCalculator.getInstance(), null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNames() {
    NSC.calculate(IRD, PresentValueSensitivityCalculator.getInstance(), new YieldCurveBundle(INTERPOLATED_CURVES), INTERPOLATED_CURVES);
  }

  @Test
  public void testPresentValue() {
    final InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, null, INTERPOLATED_CURVES);
    final DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, null, INTERPOLATED_CURVES);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  @Test
  public void testParRateValue() {
    final InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = ParRateCalculator.getInstance();
    final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = ParRateCurveSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, null, INTERPOLATED_CURVES);
    final DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, null, INTERPOLATED_CURVES);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  @Test
  public void testWithKnownCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurve = new LinkedHashMap<String, YieldAndDiscountCurve>();
    fittingCurve.put(LIBOR_CURVE_NAME, LIBOR_CURVE);

    final InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, fixedCurve, fittingCurve);
    final DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, fixedCurve, fittingCurve);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  private DoubleMatrix1D finiteDiffNodeSense(final InterestRateDerivative ird, final InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator, final YieldCurveBundle fixedCurves,
      final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves) {

    int nNodes = 0;
    for (final YieldAndDiscountCurve curve : interpolatedCurves.values()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
      nNodes += dataBundle.size();
    }

    final double[] yields = new double[nNodes];
    int index = 0;
    for (final YieldAndDiscountCurve curve : interpolatedCurves.values()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        yields[index++] = y;
      }
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(final DoubleMatrix1D x) {

        final YieldCurveBundle curves = new YieldCurveBundle();
        int index2 = 0;
        for (final String name : interpolatedCurves.keySet()) {
          final YieldAndDiscountCurve curve = interpolatedCurves.get(name);
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();

          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;

          final YieldAndDiscountCurve newCurve = new YieldCurve(InterpolatedDoublesCurve.from(dataBundle.getKeys(), yields1, ((InterpolatedDoublesCurve) curve.getCurve()).getInterpolator()));
          curves.setCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return valueCalculator.visit(ird, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.derivative(f);

    return grad.evaluate(new DoubleMatrix1D(yields));

  }
}
