/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
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
  private static final LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> INTERPOLATED_CURVES;
  private static final InterpolatedYieldAndDiscountCurve FUNDING_CURVE;
  private static final InterpolatedYieldAndDiscountCurve LIBOR_CURVE;

  static {
    double[] fixedPaymentTimes = new double[] {1, 2, 3, 4, 5};
    double[] floatingPaymentTimes = new double[] {0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};

    double[] fundingCurveNodes = new double[] {0.5, 1, 1.5, 2.0, 3.1, 4.1, 5};
    double[] fundingCurveYields = new double[] {0.03, 0.04, 0.043, 0.046, 0.4, 0.036, 0.03};
    double[] liborCurveNodes = new double[] {1, 1.5, 1.9, 3., 4.0, 6.0};
    double[] liborCurveYields = new double[] {0.041, 0.043, 0.048, 0.41, 0.0362, 0.032};

    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

    FUNDING_CURVE = new InterpolatedYieldCurve(fundingCurveNodes, fundingCurveYields, extrapolator);
    extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
    LIBOR_CURVE = new InterpolatedYieldCurve(liborCurveNodes, liborCurveYields, extrapolator);

    INTERPOLATED_CURVES = new LinkedHashMap<String, InterpolatedYieldAndDiscountCurve>();
    INTERPOLATED_CURVES.put(FUNDING_CURVE_NAME, FUNDING_CURVE);
    INTERPOLATED_CURVES.put(LIBOR_CURVE_NAME, LIBOR_CURVE);

    double couponRate = 0.07;
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
    InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueSensitivityCalculator.getInstance();
    DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, null, INTERPOLATED_CURVES);
    DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, null, INTERPOLATED_CURVES);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  @Test
  public void testParRateValue() {
    InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = ParRateCalculator.getInstance();
    InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = ParRateCurveSensitivityCalculator.getInstance();
    DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, null, INTERPOLATED_CURVES);
    DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, null, INTERPOLATED_CURVES);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  @Test
  public void testWithKnowCurve() {
    YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> fittingCurve = new LinkedHashMap<String, InterpolatedYieldAndDiscountCurve>();
    fittingCurve.put(LIBOR_CURVE_NAME, LIBOR_CURVE);

    InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueSensitivityCalculator.getInstance();
    DoubleMatrix1D result = NSC.calculate(IRD, senseCalculator, fixedCurve, fittingCurve);
    DoubleMatrix1D fdresult = finiteDiffNodeSense(IRD, valueCalculator, fixedCurve, fittingCurve);

    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }

  private DoubleMatrix1D finiteDiffNodeSense(final InterestRateDerivative ird, final InterestRateDerivativeVisitor<YieldCurveBundle, Double> valueCalculator, final YieldCurveBundle fixedCurves,
      final LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> interpolatedCurves) {

    int nNodes = 0;
    for (InterpolatedYieldAndDiscountCurve curve : interpolatedCurves.values()) {
      Interpolator1DDataBundle dataBundle = curve.getDataBundles().values().iterator().next();
      nNodes += dataBundle.size();
    }

    double[] yields = new double[nNodes];
    int index = 0;
    for (InterpolatedYieldAndDiscountCurve curve : interpolatedCurves.values()) {
      Interpolator1DDataBundle dataBundle = curve.getDataBundles().values().iterator().next();
      for (double y : dataBundle.getValues()) {
        yields[index++] = y;
      }
    }

    Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(DoubleMatrix1D x) {

        YieldCurveBundle curves = new YieldCurveBundle();
        int index2 = 0;
        for (final String name : interpolatedCurves.keySet()) {
          final InterpolatedYieldAndDiscountCurve curve = interpolatedCurves.get(name);
          Interpolator1DDataBundle dataBundle = curve.getDataBundles().values().iterator().next();
          int numberOfNodes = dataBundle.size();

          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;

          final InterpolatedYieldAndDiscountCurve newCurve = new InterpolatedYieldCurve(dataBundle.getKeys(), yields1, curve.getInterpolators().values().iterator().next());
          curves.setCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return valueCalculator.getValue(ird, curves);
      }
    };

    ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.derivative(f);

    return grad.evaluate(new DoubleMatrix1D(yields));

  }
}
