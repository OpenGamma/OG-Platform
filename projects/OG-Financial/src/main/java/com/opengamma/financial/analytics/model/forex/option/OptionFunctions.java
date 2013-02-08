/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.equity.option.EquityVanillaBarrierOptionDistanceFunction;
import com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions;
import com.opengamma.financial.analytics.model.forex.option.localvol.LocalVolFunctions;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceDefaults;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class OptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  /**
   * Gets the default values for calculations
   * @return The repository with equity option defaults set
   */
  public static RepositoryConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * @param barrierFormat the barrier output display format
   * @return The repository with equity barrier option defaults set
   */
  public static RepositoryConfigurationSource defaults(final String barrierFormat) {
    final Defaults factory = new Defaults();
    factory.setBarrierDistanceFormat(barrierFormat);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    private String _barrierFormat = EquityVanillaBarrierOptionDistanceFunction.BARRIER_ABS;

    public void setBarrierDistanceFormat(final String format) {
      _barrierFormat = format;
    }

    public String getBarrierDistanceFormat() {
      return _barrierFormat;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(BarrierOptionDistanceDefaults.class, getBarrierDistanceFormat()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BloombergFXSpotRatePercentageChangeFunction.class));
    functions.add(functionConfiguration(BloombergFXOptionSpotRateFunction.class));
    functions.add(functionConfiguration(FXBarrierOptionDistanceFunction.class));
  }

  protected RepositoryConfigurationSource blackFunctionConfiguration() {
    return BlackFunctions.instance();
  }

  protected RepositoryConfigurationSource callSpreadBlackFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource localVolFunctionConfiguration() {
    return LocalVolFunctions.instance();
  }

  protected RepositoryConfigurationSource vannaVolgaFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), blackFunctionConfiguration(), callSpreadBlackFunctionConfiguration(), localVolFunctionConfiguration(),
        vannaVolgaFunctionConfiguration());
  }

}
