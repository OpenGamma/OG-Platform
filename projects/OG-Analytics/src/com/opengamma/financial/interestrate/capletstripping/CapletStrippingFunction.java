/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.SABRTermStructureParameters;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.TransformedInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.ParameterLimitsTransform;

/**
 * 
 */
public class CapletStrippingFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  private static final String ALPHA = "alpha";
  private static final String BETA = "beta";
  private static final String RHO = "rho";
  private static final String NU = "nu";

  private final List<CapFloorPricer> _capPricers;

  // private final LinkedHashMap<String, double[]> _knotPoints;
  // private final LinkedHashMap<String, Interpolator1D> _interpolators;
  // private final LinkedHashMap<String, ParameterLimitsTransform> _parameterTransforms;
  private final LinkedHashMap<String, InterpolatedDoublesCurve> _knownParameterTermStructures;

  private final InterpolatedCurveBuildingFunction _curveBuilder;

  // private final int _totalNodes;

  public CapletStrippingFunction(final List<CapFloor> caps, final YieldCurveBundle yieldCurves,
      final LinkedHashMap<String, double[]> knotPoints,
      final LinkedHashMap<String, Interpolator1D> interpolators,
      final LinkedHashMap<String, ParameterLimitsTransform> parameterTransforms,
      final LinkedHashMap<String, InterpolatedDoublesCurve> knownParameterTermSturctures) {
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

    final LinkedHashMap<String, Interpolator1D> transInterpolators = new LinkedHashMap<String, Interpolator1D>();
    final Set<String> names = interpolators.keySet();
    for (final String name : names) {
      final Interpolator1D temp = new TransformedInterpolator1D(interpolators.get(name), parameterTransforms.get(name));
      transInterpolators.put(name, temp);
    }

    _curveBuilder = new InterpolatedCurveBuildingFunction(knotPoints, transInterpolators);

    //  _parameterTransforms = parameterTransforms; //TODO all the check for this

    _capPricers = new ArrayList<CapFloorPricer>(caps.size());
    for (final CapFloor cap : caps) {
      _capPricers.add(new CapFloorPricer(cap, yieldCurves));
    }
    _knownParameterTermStructures = knownParameterTermSturctures;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

    final LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);

    // set any known (i.e. fixed) curves
    if (_knownParameterTermStructures != null) {
      curves.putAll(_knownParameterTermStructures);
    }

    //TODO for now this is tied to SABRTermStructureParameters - what to be able to drop in any volatility model that has a term structure of
    //parameters
    final VolatilityModel1D volModel = new SABRTermStructureParameters(curves.get(ALPHA), curves.get(BETA), curves.get(RHO), curves.get(NU));

    final double[] res = new double[_capPricers.size()];
    for (int i = 0; i < _capPricers.size(); i++) {
      res[i] = _capPricers.get(i).impliedVol(volModel);
    }

    return new DoubleMatrix1D(res);
  }

}
