/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.ForexVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.financial.analytics.volatility.surface.Grid2DInterpolatedVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates function repository configuration for surface supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the surfaces and currencies available. 
 */
public class DemoSurfaceFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {
  private static final Logger s_logger = LoggerFactory.getLogger(DemoSurfaceFunctionConfiguration.class);
  
  private ConfigMaster _configMaster;
  private ConfigDBVolatilitySurfaceDefinitionSource _volSurfaceDefinitionSource;
  private ConfigDBVolatilitySurfaceSpecificationSource _volSurfaceSpecificationSource;
  private ConfigDBFuturePriceCurveDefinitionSource _priceCurveDefinitionSource;
  private ConfigDBFuturePriceCurveSpecificationSource _priceCurveSpecificationSource;

  public void setConfigMaster(final ConfigMaster configMaster) {
    _configMaster = configMaster;
    // I've injected the master so we can do the more complex querying when we're ready.
    ConfigSource configSource = new MasterConfigSource(_configMaster);
    _volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _volSurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _priceCurveDefinitionSource = new ConfigDBFuturePriceCurveDefinitionSource(configSource);
    _priceCurveSpecificationSource = new ConfigDBFuturePriceCurveSpecificationSource(configSource);
  }
  
  public RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    addConfigFor(configs, RawVolatilitySurfaceDataFunction.class.getName(), new String[] {"DEFAULT", "SWAPTION", "DEFAULT"});
    addConfigFor(configs, IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction.class.getName(), new String[] {"DEFAULT", "DEFAULT", "IR_FUTURE_OPTION", "IR_FUTURE_PRICE"});
    addConfigFor(configs, RawVolatilitySurfaceDataFunction.class.getName(), new String[] {"DEFAULT", "IR_FUTURE_OPTION", "DEFAULT"});
    addConfigFor(configs, RawVolatilitySurfaceDataFunction.class.getName(), new String[] {"DEFAULT", "FX_VANILLA_OPTION", "DEFAULT"});
    addConfigFor(configs, EquityOptionVolatilitySurfaceDataFunction.class.getName(), new String[] {"DEFAULT", "EQUITY_OPTION", "DEFAULT"});
    addConfigFor(configs, Grid2DInterpolatedVolatilitySurfaceFunction.class.getName(), new String[] {"DEFAULT", "EQUITY_OPTION", "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator", 
      "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator"});
    configs.add(new ParameterizedFunctionConfiguration(ForexVolatilitySurfaceFunction.class.getName(), Arrays.asList("DEFAULT", "DEFAULT")));
    return new RepositoryConfiguration(configs);
  }
  
  private void addConfigFor(List<FunctionConfiguration> configurations, String className, String[] params) {
    if (className.equals(Grid2DInterpolatedVolatilitySurfaceFunction.class.getName())) {
      if (params.length != 8) {
        s_logger.error("Not enough parameters for " + className);
        s_logger.error(Arrays.asList(params).toString());
        throw new OpenGammaRuntimeException("Not enough parameters for " + className);        
      }
      configurations.add(new ParameterizedFunctionConfiguration(className, Arrays.asList(params)));
      return;
    }
    if (className.equals(IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction.class.getName())) {
      if (params.length != 4) {
        s_logger.error("Not enough parameters for " + className);
        s_logger.error(Arrays.asList(params).toString());
        throw new OpenGammaRuntimeException("Not enough parameters for " + className);
      }
      if (checkForDefinitionAndSpecification(params[0], params[1], params[2], params[3])) {
        configurations.add(new ParameterizedFunctionConfiguration(className, Arrays.asList(params)));
        return;
      }
    } else {
      if (params.length != 3) {
        s_logger.error("Not enough parameters for " + className);
        s_logger.error(Arrays.asList(params).toString());
        throw new OpenGammaRuntimeException("Not enough parameters for " + className);
      }
      if (checkForDefinitionAndSpecification(params[0], params[1], params[2])) {
        configurations.add(new ParameterizedFunctionConfiguration(className, Arrays.asList(params)));
        return;
      }
    }
    // Handle if it doesn't work and check system run mode so we don't bark warnings if not necessary.
    RunMode runMode = RunMode.valueOf(System.getProperty(PlatformConfigUtils.RUN_MODE_PROPERTY_NAME).toUpperCase());
    switch (runMode) {
      case EXAMPLE:
        s_logger.debug("Not adding function for " + className + " with parameters " + Arrays.asList(params));
        break;
      default:
        s_logger.warn("Not adding function for " + className + " with parameters " + Arrays.asList(params));
        break;
    }
  }
  
  public boolean checkForDefinitionAndSpecification(String definitionName, String specificationName, String volSurfaceInstrumentType, String priceCurveInstrumentType) {
    if (checkForDefinitionAndSpecification(definitionName, volSurfaceInstrumentType, specificationName)) {
      FuturePriceCurveDefinition<?> futurePriceCurveDefinition = _priceCurveDefinitionSource.getDefinition(definitionName, priceCurveInstrumentType);
      if (futurePriceCurveDefinition != null) {
        FuturePriceCurveSpecification futurePriceCurveSpecification = _priceCurveSpecificationSource.getSpecification(specificationName, priceCurveInstrumentType);
        if (futurePriceCurveSpecification != null) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean checkForDefinitionAndSpecification(String definitionName, String type, String specificationName) {
    VolatilitySurfaceDefinition<?, ?> definition = _volSurfaceDefinitionSource.getDefinition(definitionName, type);
    if (definition != null) {
      VolatilitySurfaceSpecification specification = _volSurfaceSpecificationSource.getSpecification(specificationName, type);
      if (specification != null) {
        return true;
      }
    }
    return false;
  }
  

  public RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new RepositoryConfigurationSource() {
      private final RepositoryConfiguration _config = constructRepositoryConfiguration();

      @Override
      public RepositoryConfiguration getRepositoryConfiguration() {
        return _config;
      }
    };
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
