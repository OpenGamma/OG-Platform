/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SABRTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundleBuilderFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;

/**
 * 
 */
public class CapletStrippingJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  private static final String ALPHA = "alpha";
  private static final String BETA = "beta";
  private static final String NU = "nu";
  private static final String RHO = "rho";

  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();

  private final LinkedHashMap<String, Interpolator1D> _interpolators;

  private final Set<String> _parameterNames;
  private final List<CapFloorPricer> _capPricers;
  private final LinkedHashMap<String, InterpolatedDoublesCurve> _knownParameterTermSturctures;
  private final InterpolatedCurveBuildingFunction _curveBuilder;
  private final Interpolator1DDataBundleBuilderFunction _dataBundleBuilder;

  public CapletStrippingJacobian(final List<CapFloor> caps, final MulticurveProviderInterface curves,
      final LinkedHashMap<String, double[]> knotPoints,
      final LinkedHashMap<String, Interpolator1D> interpolators,
      final LinkedHashMap<String, ParameterLimitsTransform> parameterTransforms,
      final LinkedHashMap<String, InterpolatedDoublesCurve> knownParameterTermSturctures) {
    Validate.notNull(caps, "caps null");
    Validate.notNull(knotPoints, "null node points");
    Validate.notNull(interpolators, "null interpolators");
    Validate.isTrue(knotPoints.size() == interpolators.size(), "size mismatch between nodes and interpolators");

    _knownParameterTermSturctures = knownParameterTermSturctures;

    final LinkedHashMap<String, Interpolator1D> transInterpolators = new LinkedHashMap<>();
    final Set<String> names = interpolators.keySet();
    _parameterNames = names;
    for (final String name : names) {
      final Interpolator1D temp = new TransformedInterpolator1D(interpolators.get(name), parameterTransforms.get(name));
      transInterpolators.put(name, temp);
    }

    _capPricers = new ArrayList<>(caps.size());
    for (final CapFloor cap : caps) {
      _capPricers.add(new CapFloorPricer(cap, curves));
    }
    _interpolators = transInterpolators;
    _curveBuilder = new InterpolatedCurveBuildingFunction(knotPoints, transInterpolators);
    _dataBundleBuilder = new Interpolator1DDataBundleBuilderFunction(knotPoints, transInterpolators);

  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {

    final LinkedHashMap<String, Interpolator1DDataBundle> db = _dataBundleBuilder.evaluate(x); //TODO merge these - they do the same work!
    final LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);

    // set any known (i.e. fixed) curves
    if (_knownParameterTermSturctures != null) {
      curves.putAll(_knownParameterTermSturctures);
    }

    //TODO make this general - not SABR specific
    final Curve<Double, Double> cAlpha = curves.get(ALPHA);
    final Curve<Double, Double> cBeta = curves.get(BETA);
    final Curve<Double, Double> cRho = curves.get(RHO);
    final Curve<Double, Double> cNu = curves.get(NU);
    final VolatilityModel1D volModel = new SABRTermStructureParameters(cAlpha, cBeta, cRho, cNu);

    final int nCaps = _capPricers.size();
    final int m = x.getNumberOfElements();
    final double[][] jac = new double[nCaps][m];
    double f, k, t;

    for (int i = 0; i < nCaps; i++) { //outer loop over caps

      final CapFloorPricer capPricer = _capPricers.get(i);
      final double vega = capPricer.vega(volModel);
      final int nCaplets = capPricer.getNumberCaplets();
      final double[][] greeks = new double[nCaplets][]; //the sensitivity of vol to the model parameters
      final double[] capletVega = new double[nCaplets];
      final double[] capletExpiries = capPricer.getExpiries();
      final double[] capletFwds = capPricer.getForwards();
      final double[] capletDF = capPricer.getDiscountFactors();
      final double[][][] nodeSens = new double[_parameterNames.size()][nCaplets][]; //Sensitivity to the nodes of a particular curve at a particular time
      k = capPricer.getStrike();

      //TODO There will be much repeated calculations here, as many of the caplets are shared across caps
      for (int tIndex = 0; tIndex < nCaplets; tIndex++) {
        f = capletFwds[tIndex];
        t = capletExpiries[tIndex];
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        //TODO again this is SABR specific
        final SABRFormulaData data = new SABRFormulaData(cAlpha.getYValue(t), cBeta.getYValue(t), cRho.getYValue(t), cNu.getYValue(t));
        greeks[tIndex] = SABR.getVolatilityAdjoint(option, f, data); //2nd and 3rd entries are forward & strike sensitivity which we don't use
        capletVega[tIndex] = capletDF[tIndex] * BlackFormulaRepository.vega(f, k, t, greeks[tIndex][0]);

        int parmIndex = 0;
        for (final String name : _parameterNames) {
          final Interpolator1D itrp = _interpolators.get(name);
          nodeSens[parmIndex++][tIndex] = itrp.getNodeSensitivitiesForValue(db.get(name), t);
        }
      }

      final double[] res = new double[x.getNumberOfElements()];
      for (int tIndex = 0; tIndex < nCaplets; tIndex++) {
        int index = 0;
        for (int parmIndex = 0; parmIndex < _parameterNames.size(); parmIndex++) {
          final double temp = capletVega[tIndex] * greeks[tIndex][parmIndex + 3]; //1st 3 are vol, dForward & dStrike
          final double[] ns = nodeSens[parmIndex][tIndex];
          for (final double element : ns) {
            res[index] += element * temp;
            index++;
          }
        }
      }
      for (int j = 0; j < res.length; j++) {
        jac[i][j] = res[j] / vega;
      }

    }
    return new DoubleMatrix2D(jac);

  }

}
