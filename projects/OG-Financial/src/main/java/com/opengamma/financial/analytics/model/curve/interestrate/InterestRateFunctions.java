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
    /** The currencies for which these defaults apply */
    private final Set<String> _applicableCurrencies = new HashSet<>();
    /** The absolute tolerance of the root-finding */
    private double _absoluteTolerance = 0.0001;
    /** The relative tolerance of the root-finding */
    private double _relativeTolerance = 0.0001;
    /** The maximum number of iterations used by the root-finder */
    private int _maxIterations = 1000;
    /** The matrix decomposition method used by the root-finder */
    private String _decomposition = DecompositionFactory.SV_COLT_NAME;
    /** True if finite difference is used to calculate derivatives */
    private boolean _useFiniteDifference; /* = false;*/
    /** The interpolator name */
    private String _interpolatorName = Interpolator1DFactory.LINEAR;
    /** The left extrapolator name */
    private String _leftExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    /** The right extrapolator name */
    private String _rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;

    /**
     * Sets the currencies for which these defaults apply.
     * @param applicableCurrencies The applicable currencies
     */
    public void setApplicableCurrencies(final Set<String> applicableCurrencies) {
      _applicableCurrencies.clear();
      _applicableCurrencies.addAll(applicableCurrencies);
    }

    /**
     * Gets the currencies for which these defaults apply.
     * @return The applicable currencies
     */
    public Set<String> getApplicableCurrencies() {
      return _applicableCurrencies;
    }

    /**
     * Gets the absolute tolerance used by the root-finder.
     * @return The absolute tolerance
     */
    public double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    /**
     * Sets the absolute tolerance used by the root-finder.
     * @param absoluteTolerance The absolute tolerance
     */
    public void setAbsoluteTolerance(final double absoluteTolerance) {
      _absoluteTolerance = absoluteTolerance;
    }

    /**
     * Gets the relative tolerance used by the root-finder.
     * @return The relative tolerance
     */
    public double getRelativeTolerance() {
      return _relativeTolerance;
    }

    /**
     * Sets the relative tolerance used by the root-finder.
     * @param relativeTolerance The relative tolerance
     */
    public void setRelativeTolerance(final double relativeTolerance) {
      _relativeTolerance = relativeTolerance;
    }

    /**
     * Gets the maximum number of iterations used by the root-finder.
     * @return The maximum number of iterations
     */
    public int getMaxIterations() {
      return _maxIterations;
    }

    /**
     * Sets the maximum number of iterations used by the root-finder.
     * @param maxIterations The maximum number of iterations
     */
    public void setMaxIterations(final int maxIterations) {
      _maxIterations = maxIterations;
    }

    /**
     * Gets the matrix decomposition used by the root-finder.
     * @return The matrix decomposition
     */
    public String getDecomposition() {
      return _decomposition;
    }

    /**
     * Sets the matrix decomposition used by the root-finder.
     * @param decomposition The matrix decomposition
     */
    public void setDecomposition(final String decomposition) {
      _decomposition = decomposition;
    }

    /**
     * Does the root-finder use finite difference to calculate derivatives.
     * @return True if the root-finder uses finite difference
     */
    public boolean isUseFiniteDifference() {
      return _useFiniteDifference;
    }

    /**
     * Sets the derivative calculation method, finite difference or analytic.
     * @param useFiniteDifference True if the root-finder uses finite difference
     */
    public void setUseFiniteDifference(final boolean useFiniteDifference) {
      _useFiniteDifference = useFiniteDifference;
    }

    /**
     * Gets the interpolator name.
     * @return The interpolator name
     */
    public String getInterpolatorName() {
      return _interpolatorName;
    }

    /**
     * Sets the interpolator name.
     * @param interpolatorName The interpolator name
     */
    public void setInterpolatorName(final String interpolatorName) {
      _interpolatorName = interpolatorName;
    }

    /**
     * Gets the left extrapolator name.
     * @return The left extrapolator name
     */
    public String getLeftExtrapolatorName() {
      return _leftExtrapolatorName;
    }

    /**
     * Sets the left extrapolator name.
     * @param leftExtrapolatorName The left extrapolator name
     */
    public void setLeftExtrapolatorName(final String leftExtrapolatorName) {
      _leftExtrapolatorName = leftExtrapolatorName;
    }

    /**
     * Gets the right extrapolator name.
     * @return The right extrapolator name
     */
    public String getRightExtrapolatorName() {
      return _rightExtrapolatorName;
    }

    /**
     * Sets the right extrapolator name.
     * @param rightExtrapolatorName The right extrapolator name
     */
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

    /**
     * Adds default functions for FX implied curves for all applicable currencies.
     * @param functions A list of function configurations
     */
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

    /**
     * Adds default functions for yield curves calculated using the present value or par rate method.
     * @param functions A list of function configurations
     */
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
      functions.add(functionConfiguration(ImpliedDepositYieldCurveDefaults.class, args));
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
    functions.add(functionConfiguration(FXImpliedYieldCurveSeriesFunction.class));
    functions.add(functionConfiguration(InterpolatedYieldCurveFunction.class));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING));
    functions.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    functions.add(functionConfiguration(MultiYieldCurvePresentValueMethodFunction.class));
    functions.add(functionConfiguration(MultiCurveCalculationConfigFunction.class));
  }

}
