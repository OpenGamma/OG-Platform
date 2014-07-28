/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.volatility.SABRTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * Gives the (Black) volatility for a given forward, strike and time-to-expiry based on a SABR term structure model - i.e., alpha, beta, nu
 * and rho are (spline) functions of the time-to-expiry of an option. <b>Note</b> the parameters are not functions of time but are have fixed
 * values for a given option expiry that does not change over the life time of the option. (Approximations do exist for the former case, but we do
 * not deal with them here.)
 */
public class SABRTermStructureModelProvider extends VolatilityModelProvider {

  private static final String ALPHA = "alpha";
  private static final String BETA = "beta";
  private static final String RHO = "rho";
  private static final String NU = "nu";

  private final LinkedHashMap<String, ? extends DoublesCurve> _knownParameterTermStructures;
  private final InterpolatedCurveBuildingFunction _curveBuilder;

  /**
   * General set up for a SABRTermStructureModelProvider
   * @param knotPoints Map between parameter curve names ("alpha", "beta", "rho" and "nu") and the positions of the knot points on each of those curves
   * @param interpolators  Map between parameter curve names ("alpha", "beta", "rho" and "nu") and the interpolator used to describe that curve
   * @param parameterTransforms  Map between parameter curve names ("alpha", "beta", "rho" and "nu") and the parameter transform used for that curve
   * @param knownParameterTermSturctures  Map between known curve names (could be "alpha", "beta", "rho" and "nu") and the known curve(s)
   */
  public SABRTermStructureModelProvider(final LinkedHashMap<String, double[]> knotPoints, final LinkedHashMap<String, Interpolator1D> interpolators,
      final LinkedHashMap<String, ParameterLimitsTransform> parameterTransforms, final LinkedHashMap<String, DoublesCurve> knownParameterTermSturctures) {

    ArgumentChecker.notNull(knotPoints, "null node points");
    ArgumentChecker.notNull(interpolators, "null interpolators");
    ArgumentChecker.isTrue(knotPoints.size() == interpolators.size(), "size mismatch between nodes and interpolators");

    if (knownParameterTermSturctures == null) {
      ArgumentChecker.isTrue(knotPoints.containsKey(ALPHA) && interpolators.containsKey(ALPHA), "alpha curve not found");
      ArgumentChecker.isTrue(knotPoints.containsKey(BETA) && interpolators.containsKey(BETA), "beta curve not found");
      ArgumentChecker.isTrue(knotPoints.containsKey(NU) && interpolators.containsKey(NU), "nu curve not found");
      ArgumentChecker.isTrue(knotPoints.containsKey(RHO) && interpolators.containsKey(RHO), "rho curve not found");
    } else {
      ArgumentChecker.isTrue((knotPoints.containsKey(ALPHA) && interpolators.containsKey(ALPHA)) ^ knownParameterTermSturctures.containsKey(ALPHA), "alpha curve not found");
      ArgumentChecker.isTrue((knotPoints.containsKey(BETA) && interpolators.containsKey(BETA)) ^ knownParameterTermSturctures.containsKey(BETA), "beta curve not found");
      ArgumentChecker.isTrue((knotPoints.containsKey(NU) && interpolators.containsKey(NU)) ^ knownParameterTermSturctures.containsKey(NU), "nu curve not found");
      ArgumentChecker.isTrue((knotPoints.containsKey(RHO) && interpolators.containsKey(RHO)) ^ knownParameterTermSturctures.containsKey(RHO), "rho curve not found");
    }

    final LinkedHashMap<String, Interpolator1D> transInterpolators = new LinkedHashMap<>();
    for (final Map.Entry<String, Interpolator1D> entry : interpolators.entrySet()) {
      final String name = entry.getKey();
      final Interpolator1D temp = new TransformedInterpolator1D(entry.getValue(), parameterTransforms.get(name));
      transInterpolators.put(name, temp);
    }

    _curveBuilder = new InterpolatedCurveBuildingFunction(knotPoints, transInterpolators);

    // _parameterTransforms = parameterTransforms; //TODO all the check for this

    _knownParameterTermStructures = knownParameterTermSturctures;
  }

  /**
   * @param x The concatenated nodes that form the interpolated SABR parameter curves
   * @return a VolatilityModel1D (SABRTermStructureParameters)
   */
  @Override
  public VolatilityModel1D evaluate(final DoubleMatrix1D x) {
    final LinkedHashMap<String, ? extends DoublesCurve> curves = getCurves(x);
    return new SABRTermStructureParameters(curves.get(ALPHA), curves.get(BETA), curves.get(RHO), curves.get(NU));
  }

  protected LinkedHashMap<String, ? extends DoublesCurve> getCurves(final DoubleMatrix1D x) {
    @SuppressWarnings("rawtypes")
    final LinkedHashMap curves = _curveBuilder.evaluate(x);

    // set any known (i.e. fixed) curves
    if (_knownParameterTermStructures != null) {
      curves.putAll(_knownParameterTermStructures);
    }
    return curves;
  }

}
