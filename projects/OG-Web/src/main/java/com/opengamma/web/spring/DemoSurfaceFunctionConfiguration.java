/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.analytics.model.volatility.surface.ForexCallDeltaVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.surface.ForexFlatWithTermStructureVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.surface.ForexStrangleRiskReversalVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.surface.InterpolatedVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.volatility.VolatilitySurfaceSpecificationFunction;
import com.opengamma.financial.analytics.volatility.surface.BondFutureOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.financial.analytics.volatility.surface.IRFutureOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.InterpolatedVolatilitySurfaceDefaultPropertiesFunction;
import com.opengamma.financial.analytics.volatility.surface.RawBondFutureOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawEquityOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawFXVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawIRFutureOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawSoybeanFutureOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawSwaptionATMVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.SwaptionATMVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates function repository configuration for surface supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the surfaces and currencies available.
 */
public class DemoSurfaceFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  private ConfigMaster _configMaster;
  private ConfigDBVolatilitySurfaceDefinitionSource _volSurfaceDefinitionSource;
  private ConfigDBVolatilitySurfaceSpecificationSource _volSurfaceSpecificationSource;
  private ConfigDBFuturePriceCurveDefinitionSource _priceCurveDefinitionSource;
  private ConfigDBFuturePriceCurveSpecificationSource _priceCurveSpecificationSource;

  public void setConfigMaster(final ConfigMaster configMaster) {
    _configMaster = configMaster;
    // I've injected the master so we can do the more complex querying when we're ready.
    final ConfigSource configSource = new MasterConfigSource(_configMaster);
    _volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _volSurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _priceCurveDefinitionSource = new ConfigDBFuturePriceCurveDefinitionSource(configSource);
    _priceCurveSpecificationSource = new ConfigDBFuturePriceCurveSpecificationSource(configSource);
  }

  public RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    addConfigFor(configs, VolatilitySurfaceSpecificationFunction.class.getName());
    addConfigFor(configs, RawIRFutureOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, RawBondFutureOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, RawFXVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, RawSwaptionATMVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, RawEquityOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, RawSoybeanFutureOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, IRFutureOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, BondFutureOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, EquityOptionVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, ForexStrangleRiskReversalVolatilitySurfaceFunction.class.getName());
    addConfigFor(configs, ForexCallDeltaVolatilitySurfaceFunction.class.getName());
    addConfigFor(configs, ForexFlatWithTermStructureVolatilitySurfaceFunction.class.getName());
    addConfigFor(configs, SwaptionATMVolatilitySurfaceDataFunction.class.getName());
    addConfigFor(configs, InterpolatedVolatilitySurfaceFunction.class.getName());
    configs.add(new ParameterizedFunctionConfiguration(InterpolatedVolatilitySurfaceDefaultPropertiesFunction.class.getName(),
        Arrays.asList("FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear")));
    return new RepositoryConfiguration(configs);
  }

  private void addConfigFor(final List<FunctionConfiguration> configurations, final String className) {
    configurations.add(new StaticFunctionConfiguration(className));
  }

  public boolean checkForDefinitionAndSpecification(final String definitionName, final String specificationName, final String volSurfaceInstrumentType, final String priceCurveInstrumentType) {
    if (checkForDefinitionAndSpecification(definitionName, volSurfaceInstrumentType, specificationName)) {
      final FuturePriceCurveDefinition<?> futurePriceCurveDefinition = _priceCurveDefinitionSource.getDefinition(definitionName, priceCurveInstrumentType);
      if (futurePriceCurveDefinition != null) {
        final FuturePriceCurveSpecification futurePriceCurveSpecification = _priceCurveSpecificationSource.getSpecification(specificationName, priceCurveInstrumentType);
        if (futurePriceCurveSpecification != null) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkForDefinitionAndSpecification(final String definitionName, final String type, final String specificationName) {
    final VolatilitySurfaceDefinition<?, ?> definition = _volSurfaceDefinitionSource.getDefinition(definitionName, type);
    if (definition != null) {
      final VolatilitySurfaceSpecification specification = _volSurfaceSpecificationSource.getSpecification(specificationName, type);
      if (specification != null) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  public RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
