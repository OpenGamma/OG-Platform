/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.List;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.BeanDynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class VolatilityCubeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * @return The configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new VolatilityCubeFunctions().getObjectCreating();
  }

  /**
   * Returns a configuration source populated with volatility cube definition and specification
   * functions that listen to changes.
   * @param configMaster The config master
   * @return A configuration source
   */
  public static FunctionConfigurationSource providers(final ConfigMaster configMaster) {
    return new BeanDynamicFunctionConfigurationSource(ConfigMasterChangeProvider.of(configMaster)) {

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        final Providers providers = new Providers();
        providers.setConfigMaster(configMaster);
        return providers;
      }

      @Override
      protected boolean isPropogateEvent(final ChangeEvent event) {
        return Providers.isMonitoredType(event.getObjectId().getValue());
      }
    };
  }

  /**
   * Function repository configuration source for curve functions based on the items defined in a {@link ConfigMaster}.
   */
  public static class Providers extends VersionedFunctionConfigurationBean {
    /** The configuration master */
    private ConfigMaster _configMaster;

    /**
     * Sets the config master.
     *
     * @param configMaster The config master, not null
     */
    public void setConfigMaster(final ConfigMaster configMaster) {
      ArgumentChecker.notNull(configMaster, "config master");
      _configMaster = configMaster;
    }

    /**
     * Gets the config master.
     *
     * @return The config master
     */
    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<VolatilityCubeDefinition<?, ?, ?>> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setVersionCorrection(getVersionCorrection());
      searchRequest.setType(VolatilityCubeDefinition.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final VolatilityCubeDefinition<?, ?, ?> config = ((ConfigItem<VolatilityCubeDefinition<?, ?, ?>>) configDocument.getConfig()).getValue();
        functions.add(functionConfiguration(VolatilityCubeDefinitionFunction.class, config.getName()));
      }

      searchRequest.setType(VolatilityCubeSpecification.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final VolatilityCubeSpecification config = ((ConfigItem<VolatilityCubeSpecification>) configDocument.getConfig()).getValue();
        functions.add(functionConfiguration(VolatilityCubeSpecificationFunction.class, config.getName()));
      }
    }

    /**
     * Returns true if the type is {@link VolatilityCubeDefinition} or {@link VolatilityCubeSpecification}.
     * @param type
     * @return
     */
    /* package */ static boolean isMonitoredType(final String type) {
      return VolatilityCubeDefinition.class.getName().equals(type) || VolatilityCubeSpecification.class.getName().equals(type);
    }
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(RawVolatilityCubeDataFunction.class));
    functions.add(functionConfiguration(RelativeStrikeLognormalVolatilityCubeConverterFunction.class));
    functions.add(functionConfiguration(MoneynessLognormalVolatilityCubeConverterFunction.class));
  }
}
