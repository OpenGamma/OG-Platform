/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class MultiCurvePricingFunctions {

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {
    /** The absolute tolerance used in root-finding by curve functions */
    private double _absoluteTolerance = 1e-9;
    /** The relative tolerance used in root-finding by curve functions */
    private double _relativeTolerance = 1e-9;
    /** The maximum number of iterations used in root-finding by curve functions */
    private int _maxIterations = 1000;

    /**
     * Gets the absolute tolerance.
     * @return The absolute tolerance
     */
    public double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    /**
     * Sets the absolute tolerance.
     * @param absoluteTolerance The absolute tolerance
     */
    public void setAbsoluteTolerance(final double absoluteTolerance) {
      _absoluteTolerance = absoluteTolerance;
    }

    /**
     * Gets the relative tolerance.
     * @return The relative tolerance
     */
    public double getRelativeTolerance() {
      return _relativeTolerance;
    }

    /**
     * Sets the relative tolerance.
     * @param relativeTolerance The relative tolerance.
     */
    public void setRelativeTolerance(final double relativeTolerance) {
      _relativeTolerance = relativeTolerance;
    }

    /**
     * Gets the maximum number of iterations.
     * @return The maximum number of iterations
     */
    public int getMaximumIterations() {
      return _maxIterations;
    }

    /**
     * Sets the maximum number of iterations
     * @param maxIterations The maximum number of iterations
     */
    public void setMaximumIterations(final int maxIterations) {
      _maxIterations = maxIterations;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNegativeOrZero(getAbsoluteTolerance(), "absolute tolerance");
      ArgumentChecker.notNegativeOrZero(getRelativeTolerance(), "relative tolerance");
      ArgumentChecker.notNegativeOrZero(getMaximumIterations(), "maximum iterations");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      addCurveDefaults(functions);
    }

    /**
     * Adds default values for absolute tolerance, relative tolerance and the maximum number of
     * iterations for all curve building functions
     * @param functions The list of function configurations.
     */
    protected void addCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3];
      args[0] = Double.toString(getAbsoluteTolerance());
      args[1] = Double.toString(getRelativeTolerance());
      args[2] = Integer.toString(getMaximumIterations());
      functions.add(functionConfiguration(MultiCurvePricingDefaults.class, args));
    }
  }
}
