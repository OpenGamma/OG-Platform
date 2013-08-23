/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class InterestRateFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new InterestRateFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private final Set<String> _applicableCurrencies = new HashSet<String>();
    private double _absoluteTolerance = 0.0001;
    private double _relativeTolerance = 0.0001;
    private int _maxIterations = 1000;
    private String _decomposition = DecompositionFactory.SV_COLT_NAME;
    private boolean _useFiniteDifference; /* = false;*/
    private String _interpolatorName = Interpolator1DFactory.LINEAR;
    private String _leftExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    private String _rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;

    public void setApplicableCurrencies(final Set<String> applicableCurrencies) {
      _applicableCurrencies.clear();
      _applicableCurrencies.addAll(applicableCurrencies);
    }

    public Set<String> getApplicableCurrencies() {
      return _applicableCurrencies;
    }

    public double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    public void setAbsoluteTolerance(final double absoluteTolerance) {
      _absoluteTolerance = absoluteTolerance;
    }

    public double getRelativeTolerance() {
      return _relativeTolerance;
    }

    public void setRelativeTolerance(final double relativeTolerance) {
      _relativeTolerance = relativeTolerance;
    }

    public int getMaxIterations() {
      return _maxIterations;
    }

    public void setMaxIterations(final int maxIterations) {
      _maxIterations = maxIterations;
    }

    public String getDecomposition() {
      return _decomposition;
    }

    public void setDecomposition(final String decomposition) {
      _decomposition = decomposition;
    }

    public boolean isUseFiniteDifference() {
      return _useFiniteDifference;
    }

    public void setUseFiniteDifference(final boolean useFiniteDifference) {
      _useFiniteDifference = useFiniteDifference;
    }

    public String getInterpolatorName() {
      return _interpolatorName;
    }

    public void setInterpolatorName(final String interpolatorName) {
      _interpolatorName = interpolatorName;
    }

    public String getLeftExtrapolatorName() {
      return _leftExtrapolatorName;
    }

    public void setLeftExtrapolatorName(final String leftExtrapolatorName) {
      _leftExtrapolatorName = leftExtrapolatorName;
    }

    public String getRightExtrapolatorName() {
      return _rightExtrapolatorName;
    }

    public void setRightExtrapolatorName(final String rightExtrapolatorName) {
      _rightExtrapolatorName = rightExtrapolatorName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getDecomposition(), "decomposition");
      ArgumentChecker.notNullInjected(getInterpolatorName(), "interpolatorName");
      ArgumentChecker.notNullInjected(getLeftExtrapolatorName(), "leftExtrapolatorName");
      ArgumentChecker.notNullInjected(getRightExtrapolatorName(), "rightExtrapolatorName");
      super.afterPropertiesSet();
    }

    protected void addFXImpliedYieldCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[8 + getApplicableCurrencies().size()];
      int i = 0;
      args[i++] = Double.toString(getAbsoluteTolerance());
      args[i++] = Double.toString(getRelativeTolerance());
      args[i++] = Integer.toString(getMaxIterations());
      args[i++] = getDecomposition();
      args[i++] = Boolean.toString(isUseFiniteDifference());
      args[i++] = getInterpolatorName();
      args[i++] = getLeftExtrapolatorName();
      args[i++] = getRightExtrapolatorName();
      for (final String currency : getApplicableCurrencies()) {
        args[i++] = currency;
      }
      functions.add(functionConfiguration(FXImpliedYieldCurveDefaults.class, args));
    }

    protected void addYieldCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[5 + getApplicableCurrencies().size()];
      int i = 0;
      args[i++] = Double.toString(getAbsoluteTolerance());
      args[i++] = Double.toString(getRelativeTolerance());
      args[i++] = Integer.toString(getMaxIterations());
      args[i++] = getDecomposition();
      args[i++] = Boolean.toString(isUseFiniteDifference());
      for (final String currency : getApplicableCurrencies()) {
        args[i++] = currency;
      }
      functions.add(functionConfiguration(YieldCurveDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getApplicableCurrencies().isEmpty()) {
        addFXImpliedYieldCurveDefaults(functions);
        addYieldCurveDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXImpliedYieldCurveFunction.class));
    functions.add(functionConfiguration(InterpolatedYieldCurveFunction.class));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    functions.add(functionConfiguration(MultiYieldCurveParRateMethodFunction.class));
    functions.add(functionConfiguration(MultiYieldCurvePresentValueMethodFunction.class));
    functions.add(functionConfiguration(MultiCurveCalculationConfigFunction.class));
  }

}
