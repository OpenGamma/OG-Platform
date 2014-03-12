/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

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
public class SurfaceFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * @return The configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new SurfaceFunctions().getObjectCreating();
  }

  /**
   * Returns a configuration source populated with surface definition and specification
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
      final ConfigSearchRequest<SurfaceDefinition<?, ?>> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setVersionCorrection(getVersionCorrection());
      searchRequest.setType(SurfaceDefinition.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final SurfaceDefinition<?, ?> config = ((ConfigItem<SurfaceDefinition<?, ?>>) configDocument.getConfig()).getValue();
        functions.add(functionConfiguration(SurfaceDefinitionFunction.class, config.getName()));
      }

      searchRequest.setType(SurfaceSpecification.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final SurfaceSpecification config = ((ConfigItem<SurfaceSpecification>) configDocument.getConfig()).getValue();
        functions.add(functionConfiguration(SurfaceSpecificationFunction.class, config.getName()));
      }
    }

    /**
     * Returns true if the type is {@link SurfaceDefinition} or {@link SurfaceSpecification}.
     * @param type
     * @return
     */
    /* package */ static boolean isMonitoredType(final String type) {
      return SurfaceDefinition.class.getName().equals(type) || SurfaceSpecification.class.getName().equals(type);
    }
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }
}
