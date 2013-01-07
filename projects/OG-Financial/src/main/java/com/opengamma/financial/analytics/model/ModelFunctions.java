/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.swaption.SwaptionFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class ModelFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new ModelFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected RepositoryConfigurationSource bondFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource bondFutureOptionFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource cdsFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource curveFunctionConfiguration() {
    return CurveFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource fixedIncomeFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource forexFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource futureFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource horizonFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource irFutureOptionFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource pnlFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource sabrCubeFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource sensitivitiesFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource simpleInstrumentFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource swaptionFunctionConfiguration() {
    return SwaptionFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource varFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.DEFAULT;
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), bondFunctionConfiguration(), bondFutureOptionFunctionConfiguration(), cdsFunctionConfiguration(),
        curveFunctionConfiguration(), equityFunctionConfiguration(), fixedIncomeFunctionConfiguration(), forexFunctionConfiguration(), futureFunctionConfiguration(),
        horizonFunctionConfiguration(), irFutureOptionFunctionConfiguration(), optionFunctionConfiguration(), pnlFunctionConfiguration(), riskFactorFunctionConfiguration(),
        sabrCubeFunctionConfiguration(), sensitivitiesFunctionConfiguration(), simpleInstrumentFunctionConfiguration(), swaptionFunctionConfiguration(), varFunctionConfiguration(),
        volatilityFunctionConfiguration());
  }

}
