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
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.TransformedInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundleBuilderFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.ParameterLimitsTransform;

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

  public CapletStrippingJacobian(final List<CapFloor> caps, final YieldCurveBundle yieldCurves,
      final LinkedHashMap<String, double[]> knotPoints,
      final LinkedHashMap<String, Interpolator1D> interpolators,
      final LinkedHashMap<String, ParameterLimitsTransform> parameterTransforms,
      final LinkedHashMap<String, InterpolatedDoublesCurve> knownParameterTermSturctures) {
    Validate.notNull(caps, "caps null");
    Validate.notNull(knotPoints, "null node points");
    Validate.notNull(interpolators, "null interpolators");
    Validate.isTrue(knotPoints.size() == interpolators.size(), "size mismatch between nodes and interpolators");

    _knownParameterTermSturctures = knownParameterTermSturctures;

    LinkedHashMap<String, Interpolator1D> transInterpolators = new LinkedHashMap<String, Interpolator1D>();
    Set<String> names = interpolators.keySet();
    _parameterNames = names;
    for (String name : names) {
      Interpolator1D temp = new TransformedInterpolator1D(interpolators.get(name), parameterTransforms.get(name));
      transInterpolators.put(name, temp);
    }

    _capPricers = new ArrayList<CapFloorPricer>(caps.size());
    for (CapFloor cap : caps) {
      _capPricers.add(new CapFloorPricer(cap, yieldCurves));
    }
    _interpolators = transInterpolators;
    _curveBuilder = new InterpolatedCurveBuildingFunction(knotPoints, transInterpolators);
    _dataBundleBuilder = new Interpolator1DDataBundleBuilderFunction(knotPoints, transInterpolators);

  }

  @Override
  public DoubleMatrix2D evaluate(DoubleMatrix1D x) {

    LinkedHashMap<String, Interpolator1DDataBundle> db = _dataBundleBuilder.evaluate(x); //TODO merge these - they do the same work!
    LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);

    // set any known (i.e. fixed) curves
    if (_knownParameterTermSturctures != null) {
      curves.putAll(_knownParameterTermSturctures);
    }

    //TODO make this general - not SABR specific
    Curve<Double, Double> cAlpha = curves.get(ALPHA);
    Curve<Double, Double> cBeta = curves.get(BETA);
    Curve<Double, Double> cRho = curves.get(RHO);
    Curve<Double, Double> cNu = curves.get(NU);
    VolatilityModel1D volModel = new SABRTermStructureParameters(cAlpha, cBeta, cRho, cNu);

    final int nCaps = _capPricers.size();
    final int m = x.getNumberOfElements();
    double[][] jac = new double[nCaps][m];
    double f, k, t;

    for (int i = 0; i < nCaps; i++) {//outer loop over caps

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
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        //TODO again this is SABR specific
        SABRFormulaData data = new SABRFormulaData(cAlpha.getYValue(t), cBeta.getYValue(t), cRho.getYValue(t), cNu.getYValue(t));
        greeks[tIndex] = SABR.getVolatilityAdjoint(option, f, data); //2nd and 3rd entries are forward & strike sensitivity which we don't use
        capletVega[tIndex] = capletDF[tIndex] * BlackFormulaRepository.vega(f, k, t, greeks[tIndex][0]);

        int parmIndex = 0;
        for (String name : _parameterNames) {
          Interpolator1D itrp = _interpolators.get(name);
          nodeSens[parmIndex++][tIndex] = itrp.getNodeSensitivitiesForValue(db.get(name), t);
        }
      }

      double[] res = new double[x.getNumberOfElements()];
      for (int tIndex = 0; tIndex < nCaplets; tIndex++) {
        int index = 0;
        for (int parmIndex = 0; parmIndex < _parameterNames.size(); parmIndex++) {
          double temp = capletVega[tIndex] * greeks[tIndex][parmIndex + 3]; //1st 3 are vol, dForward & dStrike
          double[] ns = nodeSens[parmIndex][tIndex];
          for (int j = 0; j < ns.length; j++) {
            res[index] += ns[j] * temp;
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
