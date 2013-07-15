/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import java.util.List;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackFunctions;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class SurfaceFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new SurfaceFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private String _xInterpolatorName = Interpolator1DFactory.LINEAR;
    private String _yInterpolatorName = Interpolator1DFactory.LINEAR;
    private String _leftXExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    private String _rightXExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    private String _leftYExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    private String _rightYExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    private boolean _useFixedAlpha; /* = false;*/
    private boolean _useFixedBeta = true;
    private boolean _useFixedRho; /* = false;*/
    private boolean _useFixedNu; /* = false;*/
    private double _alpha = 0.05;
    private double _beta = 1.0;
    private double _rho = 0.07;
    private double _nu; /* = 0.0;*/
    private double _error = 0.001;

    public void setXInterpolatorName(final String xInterpolatorName) {
      _xInterpolatorName = xInterpolatorName;
    }

    public String getXInterpolatorName() {
      return _xInterpolatorName;
    }

    public void setYInterpolatorName(final String yInterpolatorName) {
      _yInterpolatorName = yInterpolatorName;
    }

    public String getYInterpolatorName() {
      return _yInterpolatorName;
    }

    public void setLeftXExtrapolatorName(final String leftXExtrapolatorName) {
      _leftXExtrapolatorName = leftXExtrapolatorName;
    }

    public String getLeftXExtrapolatorName() {
      return _leftXExtrapolatorName;
    }

    public void setRightXExtrapolatorName(final String rightXExtrapolatorName) {
      _rightXExtrapolatorName = rightXExtrapolatorName;
    }

    public String getRightXExtrapolatorName() {
      return _rightXExtrapolatorName;
    }

    public void setLeftYExtrapolatorName(final String leftYExtrapolatorName) {
      _leftYExtrapolatorName = leftYExtrapolatorName;
    }

    public String getLeftYExtrapolatorName() {
      return _leftYExtrapolatorName;
    }

    public void setRightYExtrapolatorName(final String rightYExtrapolatorName) {
      _rightYExtrapolatorName = rightYExtrapolatorName;
    }

    public String getRightYExtrapolatorName() {
      return _rightYExtrapolatorName;
    }

    public void setUseFixedAlpha(final boolean useFixedAlpha) {
      _useFixedAlpha = useFixedAlpha;
    }

    public boolean isUseFixedAlpha() {
      return _useFixedAlpha;
    }

    public void setUseFixedBeta(final boolean useFixedBeta) {
      _useFixedBeta = useFixedBeta;
    }

    public boolean isUseFixedBeta() {
      return _useFixedBeta;
    }

    public void setUseFixedRho(final boolean useFixedRho) {
      _useFixedRho = useFixedRho;
    }

    public boolean isUseFixedRho() {
      return _useFixedRho;
    }

    public void setUseFixedNu(final boolean useFixedNu) {
      _useFixedNu = useFixedNu;
    }

    public boolean isUseFixedNu() {
      return _useFixedNu;
    }

    public void setAlpha(final double alpha) {
      _alpha = alpha;
    }

    public double getAlpha() {
      return _alpha;
    }

    public void setBeta(final double beta) {
      _beta = beta;
    }

    public double getBeta() {
      return _beta;
    }

    public void setRho(final double rho) {
      _rho = rho;
    }

    public double getRho() {
      return _rho;
    }

    public void setNu(final double nu) {
      _nu = nu;
    }

    public double getNu() {
      return _nu;
    }

    public void setError(final double error) {
      _error = error;
    }

    public double getError() {
      return _error;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getXInterpolatorName(), "xInterpolatorName");
      ArgumentChecker.notNullInjected(getYInterpolatorName(), "yInterpolatorName");
      ArgumentChecker.notNullInjected(getLeftXExtrapolatorName(), "leftXExtrapolatorName");
      ArgumentChecker.notNullInjected(getRightXExtrapolatorName(), "rightXExtrapolatorName");
      ArgumentChecker.notNullInjected(getLeftYExtrapolatorName(), "leftYExtrapolatorName");
      ArgumentChecker.notNullInjected(getRightYExtrapolatorName(), "rightYExtrapolatorName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(SABRIRFutureOptionNLSSDefaults.class,
          getXInterpolatorName(), getYInterpolatorName(), getLeftXExtrapolatorName(), getRightXExtrapolatorName(), getLeftYExtrapolatorName(), getRightYExtrapolatorName(),
          Boolean.toString(isUseFixedAlpha()), Boolean.toString(isUseFixedBeta()), Boolean.toString(isUseFixedRho()), Boolean.toString(isUseFixedNu()), Double.toString(getAlpha()),
          Double.toString(getBeta()), Double.toString(getRho()), Double.toString(getNu()), Double.toString(getError())));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackScholesMertonImpliedVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(HestonFourierIRFutureSurfaceFittingFunction.class));
    functions.add(functionConfiguration(SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction.class));
    functions.add(functionConfiguration(ForexStrangleRiskReversalVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(ForexCallDeltaVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(ForexPutDeltaVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(ForexFlatWithTermStructureVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(ForexStrangleRiskReversalVolatilitySurfaceFunction.class));
    functions.add(functionConfiguration(InterpolatedVolatilitySurfaceFunction.class));
  }

  protected FunctionConfigurationSource blackFunctionConfiguration() {
    return BlackFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), blackFunctionConfiguration());
  }

}
