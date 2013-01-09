/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForwardFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new ForwardFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final String interpolator, final String leftExtrapolator, final String rightExtrapolator) {
    final Defaults factory = new Defaults();
    factory.setInterpolator(interpolator);
    factory.setLeftExtrapolator(leftExtrapolator);
    factory.setRightExtrapolator(rightExtrapolator);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    private String _interpolator = "DoubleQuadratic";
    private String _leftExtrapolator = "LinearExtrapolator";
    private String _rightExtrapolator = "FlatExtrapolator";

    public void setInterpolator(final String interpolator) {
      _interpolator = interpolator;
    }

    public String getInterpolator() {
      return _interpolator;
    }

    public void setLeftExtrapolator(final String leftExtrapolator) {
      _leftExtrapolator = leftExtrapolator;
    }

    public String getLeftExtrapolator() {
      return _leftExtrapolator;
    }

    public void setRightExtrapolator(final String rightExtrapolator) {
      _rightExtrapolator = rightExtrapolator;
    }

    public String getRightExtrapolator() {
      return _rightExtrapolator;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getInterpolator(), "interpolator");
      ArgumentChecker.notNullInjected(getLeftExtrapolator(), "leftExtrapolator");
      ArgumentChecker.notNullInjected(getRightExtrapolator(), "rightExtrapolator");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesDefaults.class, getInterpolator(), getLeftExtrapolator(), getRightExtrapolator()));
      functions.add(functionConfiguration(InterpolatedForwardCurveDefaults.class, getInterpolator(), getLeftExtrapolator(), getRightExtrapolator()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForwardSwapCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(ForwardSwapCurveMarketDataFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveMarketDataFunction.class));
  }
}
