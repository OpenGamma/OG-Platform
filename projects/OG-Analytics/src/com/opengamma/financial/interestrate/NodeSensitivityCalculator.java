/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculatorFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class NodeSensitivityCalculator {

  public DoubleMatrix1D presentValueCalculate(final InterestRateDerivative ird, final YieldCurveBundle fixedCurves, LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> interpolatedCurves) {
    return calculate(ird, PresentValueSensitivityCalculator.getInstance(), fixedCurves, interpolatedCurves);
  }

  public DoubleMatrix1D parRateCalculate(final InterestRateDerivative ird, final YieldCurveBundle fixedCurves, LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> interpolatedCurves) {
    return calculate(ird, ParRateCurveSensitivityCalculator.getInstance(), fixedCurves, interpolatedCurves);
  }

  @SuppressWarnings("unchecked")
  public DoubleMatrix1D calculate(final InterestRateDerivative ird, final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> calculator,
      final YieldCurveBundle fixedCurves, LinkedHashMap<String, InterpolatedYieldAndDiscountCurve> interpolatedCurves) {

    Validate.notNull(ird, "null InterestRtaeDerivative");
    Validate.notNull(calculator, "null calculator");
    Validate.notNull(interpolatedCurves, "interpolated curves");

    YieldCurveBundle allCurves = new YieldCurveBundle(interpolatedCurves);

    if (fixedCurves != null) {
      for (String name : interpolatedCurves.keySet()) {
        Validate.isTrue(!fixedCurves.containsName(name), "fixed curves contain a name that is also is interpolated curves");
      }
      allCurves.addAll(fixedCurves);
    }

    final Map<String, List<DoublesPair>> senseMap = calculator.getValue(ird, allCurves);

    final List<Double> res = new ArrayList<Double>();

    for (final String name : interpolatedCurves.keySet()) { // loop over all curves (by name)

      InterpolatedYieldAndDiscountCurve curve = interpolatedCurves.get(name);
      Interpolator1D<? extends Interpolator1DDataBundle> interpolator = curve.getInterpolators().values().iterator().next();
      Interpolator1DDataBundle data = curve.getDataBundles().values().iterator().next();
      Interpolator1DNodeSensitivityCalculator sensitivityCalculator;
      // TODO move this logic into a factory
      if (interpolator instanceof CombinedInterpolatorExtrapolator) {
        CombinedInterpolatorExtrapolator combined = (CombinedInterpolatorExtrapolator) interpolator;
        String interpolatorName = Interpolator1DFactory.getInterpolatorName(combined.getInterpolator());
        String leftExtrapolatorName = Interpolator1DFactory.getInterpolatorName(combined.getLeftExtrapolator());
        String rightExtrapolatorName = Interpolator1DFactory.getInterpolatorName(combined.getRightExtrapolator());
        sensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName, false);
      } else {
        String interpolatorName = Interpolator1DFactory.getInterpolatorName(interpolator);
        sensitivityCalculator = Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator(interpolatorName, false);
      }
      final List<DoublesPair> senseList = senseMap.get(name);
      final double[][] sensitivity = new double[senseList.size()][];
      int k = 0;
      for (final DoublesPair timeAndDF : senseList) {
        sensitivity[k++] = sensitivityCalculator.calculate(data, timeAndDF.getFirst());
      }
      for (int j = 0; j < sensitivity[0].length; j++) {
        double temp = 0.0;
        k = 0;
        for (final DoublesPair timeAndDF : senseList) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j];
        }
        res.add(temp);
      }
    }

    return new DoubleMatrix1D(res.toArray(new Double[0]));

  }
}
