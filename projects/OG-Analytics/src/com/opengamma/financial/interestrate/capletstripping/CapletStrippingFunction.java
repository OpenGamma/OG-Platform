/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.SABRTermStructureParameters;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.ParameterLimitsTransform;

/**
 * 
 */
public class CapletStrippingFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  private static String ALPHA = "alpha";
  private static String BETA = "beta";
  private static String NU = "nu";
  private static String RHO = "rho";

  private List<CapFloorPricer> _capPricers;

  private final LinkedHashMap<String, double[]> _knotPoints;
  private final LinkedHashMap<String, Interpolator1D> _interpolators;
  private final LinkedHashMap<String, ParameterLimitsTransform> _parameterTransforms;
  private final LinkedHashMap<String, Curve<Double, Double>> _knownParameterTermSturctures;
  private final int _totalNodes;

  public CapletStrippingFunction(final List<CapFloor> caps, final YieldCurveBundle yieldCurves,
      final LinkedHashMap<String, double[]> knotPoints,
      final LinkedHashMap<String, Interpolator1D> interpolators,
      final LinkedHashMap<String, ParameterLimitsTransform> parameterTransforms,
      final LinkedHashMap<String, Curve<Double, Double>> knownParameterTermSturctures) {
    Validate.notNull(caps, "caps null");
    Validate.notNull(knotPoints, "null node points");
    Validate.notNull(interpolators, "null interpolators");
    Validate.isTrue(knotPoints.size() == interpolators.size(), "size mismatch between nodes and interpolators");

    if (knownParameterTermSturctures == null) {
      Validate.isTrue(knotPoints.containsKey(ALPHA) && interpolators.containsKey(ALPHA), "alpha curve not found");
      Validate.isTrue(knotPoints.containsKey(BETA) && interpolators.containsKey(BETA), "beta curve not found");
      Validate.isTrue(knotPoints.containsKey(NU) && interpolators.containsKey(NU), "nu curve not found");
      Validate.isTrue(knotPoints.containsKey(RHO) && interpolators.containsKey(RHO), "rho curve not found");
    } else {
      Validate.isTrue((knotPoints.containsKey(ALPHA) && interpolators.containsKey(ALPHA))
          ^ knownParameterTermSturctures.containsKey(ALPHA), "alpha curve not found");
      Validate.isTrue((knotPoints.containsKey(BETA) && interpolators.containsKey(BETA))
          ^ knownParameterTermSturctures.containsKey(BETA), "beta curve not found");
      Validate.isTrue((knotPoints.containsKey(NU) && interpolators.containsKey(NU))
          ^ knownParameterTermSturctures.containsKey(NU), "nu curve not found");
      Validate.isTrue((knotPoints.containsKey(RHO) && interpolators.containsKey(RHO))
          ^ knownParameterTermSturctures.containsKey(RHO), "rho curve not found");
    }
    _parameterTransforms = parameterTransforms; //TODO all the check for this 


    _capPricers = new ArrayList<CapFloorPricer>(caps.size());
    for (CapFloor cap : caps) {
      _capPricers.add(new CapFloorPricer(cap, yieldCurves));
    }

    int sum = 0;
    for (double[] nodes : knotPoints.values()) {
      sum += nodes.length;
    }
    _totalNodes = sum;
    _knotPoints = knotPoints;
    _interpolators = interpolators;

    _knownParameterTermSturctures = knownParameterTermSturctures;
  }

  @Override
  public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
    Validate.notNull(x);
    final int totalNodes = _totalNodes;

    if (x.getNumberOfElements() != totalNodes) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    Map<String, Curve<Double, Double>> pCurves = new HashMap<String, Curve<Double, Double>>();

    int index = 0;

    for (final String name : _interpolators.keySet()) {
      final Interpolator1D interpolator = _interpolators.get(name);
      final double[] nodes = _knotPoints.get(name);
      final double[] values = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      Curve<Double, Double> curve;
      if (_parameterTransforms == null) {
        curve = InterpolatedDoublesCurve.from(nodes, values, interpolator);
      } else {
        //TODO this logic of applying a transform to a curve should be extracted to somewhere else 
        final ParameterLimitsTransform pt = _parameterTransforms.get(name);
        final Interpolator1DDataBundle dataBundle = interpolator.getDataBundleFromSortedArrays(nodes, values);
        Function1D<Double, Double> temp = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double x) {
            return pt.inverseTransform(interpolator.interpolate(dataBundle, x));
          }
        };
        curve = FunctionalDoublesCurve.from(temp);
      }
      pCurves.put(name, curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownParameterTermSturctures != null) {
      pCurves.putAll(_knownParameterTermSturctures);
    }

    //TODO for now this is tied to SABRTermStructureParameters - what to be able to drop in any volatility model that has a term structure of
    //parameters 
    VolatilityModel1D volModel = new SABRTermStructureParameters(pCurves.get(ALPHA), pCurves.get(BETA), pCurves.get(NU), pCurves.get(RHO));

    double[] res = new double[_capPricers.size()];
    for (int i = 0; i < _capPricers.size(); i++) {
      res[i] = _capPricers.get(i).price(volModel);
    }

    return new DoubleMatrix1D(res);
  }

}
