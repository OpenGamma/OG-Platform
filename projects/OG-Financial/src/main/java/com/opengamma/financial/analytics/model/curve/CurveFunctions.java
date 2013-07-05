/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationFunction;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.future.FutureFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class CurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource providers(final ConfigMaster configMaster) {
    final Providers factory = new Providers();
    factory.setConfigMaster(configMaster);
    return factory.getObjectCreating();
  }

  /**
   * Function repository configuration source for curve functions based on the items defined in a {@link ConfigMaster}.
   */
  public static class Providers extends AbstractFunctionConfigurationBean {
    private ConfigMaster _configMaster;

    public void setConfigMaster(final ConfigMaster configMaster) {
      ArgumentChecker.notNull(configMaster, "config master");
      _configMaster = configMaster;
    }

    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    protected void addCurveBuildingFunctions(final List<FunctionConfiguration> functions, final String curveConfigName) {
      functions.add(functionConfiguration(FXMatrixFunction.class, curveConfigName));
      functions.add(functionConfiguration(CurveConstructionConfigurationFunction.class, curveConfigName));
      functions.add(functionConfiguration(MulticurveProviderDiscountingFunction.class, curveConfigName));
      functions.add(functionConfiguration(InflationProviderDiscountingFunction.class, curveConfigName));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<CurveDefinition> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setType(CurveConstructionConfiguration.class);
      final Class[] curveConstructionConfigurationClasses = new Class[] {CurveConstructionConfiguration.class};
      for (final Class klass : curveConstructionConfigurationClasses) {
        searchRequest.setType(klass);
        for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
          final String documentName = configDocument.getName();
          addCurveBuildingFunctions(functions, documentName);
        }
      }
    }
  }

  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource interestRateFunctionConfiguration() {
    return InterestRateFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), forwardFunctionConfiguration(), futureFunctionConfiguration(), interestRateFunctionConfiguration());
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }
}
