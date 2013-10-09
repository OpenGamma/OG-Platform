/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.FXOptionsCalculationMethodDefaults;
import com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.LocalVolFunctions;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceDefaults;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceFunction;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class OptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  /**
   * Gets the default values for calculations
   * @return The repository with equity option defaults set
   */
  public static FunctionConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * @param barrierFormat the barrier output display format
   * @return The repository with equity barrier option defaults set
   */
  public static FunctionConfigurationSource defaults(final String barrierFormat) {
    final Defaults factory = new Defaults();
    factory.setBarrierDistanceFormat(barrierFormat);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private String _barrierFormat = BarrierOptionDistanceFunction.BARRIER_ABS;

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
    functions.add(functionConfiguration(FXSpotRatePercentageChangeFunction.class));
    functions.add(functionConfiguration(FXOptionSpotRateFunction.class));
    functions.add(functionConfiguration(FXBarrierOptionDistanceFunction.class));
    functions.add(functionConfiguration(FXOptionsCalculationMethodDefaults.class, CalculationPropertyNamesAndValues.BLACK_METHOD,
        FXDigitalCallSpreadBlackFunction.CALL_SPREAD_BLACK_METHOD));
  }

  /**
   * Adds Black functions for FX options to the repository
   * @return A function configuration source populated with FX option Black functions
   * @deprecated The functions that are added are deprecated
   */
  @Deprecated
  protected FunctionConfigurationSource blackFunctionConfiguration() {
    return BlackFunctions.instance();
  }

  protected FunctionConfigurationSource callSpreadBlackFunctionConfiguration() {
    return CallSpreadBlackFunctions.instance();
  }

  protected FunctionConfigurationSource localVolFunctionConfiguration() {
    return LocalVolFunctions.instance();
  }

  protected FunctionConfigurationSource vannaVolgaFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), blackFunctionConfiguration(), callSpreadBlackFunctionConfiguration(), localVolFunctionConfiguration(),
        vannaVolgaFunctionConfiguration());
  }

}
