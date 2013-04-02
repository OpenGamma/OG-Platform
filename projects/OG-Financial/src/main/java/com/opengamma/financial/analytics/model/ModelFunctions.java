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
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.cds.CDSFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.horizon.HorizonFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleInstrumentFunctions;
import com.opengamma.financial.analytics.model.swaption.SwaptionFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class ModelFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new ModelFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected RepositoryConfigurationSource bondFunctionConfiguration() {
    return BondFunctions.instance();
  }

  protected RepositoryConfigurationSource bondFutureOptionFunctionConfiguration() {
    return BondFutureOptionFunctions.instance();
  }

  protected RepositoryConfigurationSource cdsFunctionConfiguration() {
    return CDSFunctions.instance();
  }

  protected RepositoryConfigurationSource creditFunctionConfiguration() {
    return CreditFunctions.instance();
  }

  protected RepositoryConfigurationSource curveFunctionConfiguration() {
    return CurveFunctions.instance();
  }

  protected RepositoryConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.instance();
  }

  protected RepositoryConfigurationSource fixedIncomeFunctionConfiguration() {
    return FixedIncomeFunctions.instance();
  }

  protected RepositoryConfigurationSource forexFunctionConfiguration() {
    return ForexFunctions.instance();
  }

  protected RepositoryConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected RepositoryConfigurationSource futureOptionFunctionConfiguration() {
    return FutureOptionFunctions.instance();
  }

  protected RepositoryConfigurationSource horizonFunctionConfiguration() {
    return HorizonFunctions.instance();
  }

  protected RepositoryConfigurationSource irFutureOptionFunctionConfiguration() {
    return IRFutureOptionFunctions.instance();
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected RepositoryConfigurationSource pnlFunctionConfiguration() {
    return PNLFunctions.instance();
  }

  protected RepositoryConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource sabrCubeFunctionConfiguration() {
    return SABRCubeFunctions.instance();
  }

  protected RepositoryConfigurationSource sensitivitiesFunctionConfiguration() {
    return SensitivitiesFunctions.instance();
  }

  protected RepositoryConfigurationSource simpleInstrumentFunctionConfiguration() {
    return SimpleInstrumentFunctions.instance();
  }

  protected RepositoryConfigurationSource swaptionFunctionConfiguration() {
    return SwaptionFunctions.instance();
  }

  protected RepositoryConfigurationSource varFunctionConfiguration() {
    return VaRFunctions.instance();
  }

  protected RepositoryConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), bondFunctionConfiguration(), bondFutureOptionFunctionConfiguration(), cdsFunctionConfiguration(),
        creditFunctionConfiguration(), curveFunctionConfiguration(), equityFunctionConfiguration(), fixedIncomeFunctionConfiguration(), forexFunctionConfiguration(),
        futureFunctionConfiguration(), futureOptionFunctionConfiguration(), horizonFunctionConfiguration(), irFutureOptionFunctionConfiguration(), optionFunctionConfiguration(),
        pnlFunctionConfiguration(), riskFactorFunctionConfiguration(), sabrCubeFunctionConfiguration(), sensitivitiesFunctionConfiguration(), simpleInstrumentFunctionConfiguration(),
        swaptionFunctionConfiguration(), varFunctionConfiguration(), volatilityFunctionConfiguration());
  }

}
