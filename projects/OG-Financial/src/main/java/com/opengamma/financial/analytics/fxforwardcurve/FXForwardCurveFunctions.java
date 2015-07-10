/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import java.util.List;

import com.opengamma.core.change.ChangeEvent;
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

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FXForwardCurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new FXForwardCurveFunctions().getObjectCreating();
  }

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
   * Function repository configuration source for FX Forward curve functions based on the items defined in a Config Master.
   */
  public static class Providers extends VersionedFunctionConfigurationBean {

    private ConfigMaster _configMaster;

    public void setConfigMaster(final ConfigMaster configMaster) {
      _configMaster = configMaster;
    }

    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    protected void addFXForwardCurveFunctions(List<FunctionConfiguration> functions, String ccy1, String ccy2, String curveName) {
      functions.add(functionConfiguration(FXForwardCurveDefinitionFunction.class, ccy1, ccy2, curveName));
      functions.add(functionConfiguration(FXForwardCurveSpecificationFunction.class, ccy1, ccy2, curveName));
    }

    @Override
    protected void addAllConfigurations(List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<FXForwardCurveDefinition> searchRequest = new ConfigSearchRequest<FXForwardCurveDefinition>();
      searchRequest.setType(FXForwardCurveDefinition.class);
      searchRequest.setVersionCorrection(getVersionCorrection());
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        String documentName = configDocument.getName();
        if (!documentName.endsWith("FX_FORWARD")) {
          continue;
        }
        documentName = documentName.substring(0, documentName.length() - 11);
        final int underscore = documentName.lastIndexOf('_');
        if (underscore <= 0) {
          continue;
        }
        String curveName = documentName.substring(0, underscore);
        String currencies = documentName.substring(underscore + 1);
        if (currencies.length() != 6) {
          continue;
        }
        String ccy1 = currencies.substring(0, 3);
        String ccy2 = currencies.substring(3);
        addFXForwardCurveFunctions(functions, ccy1, ccy2, curveName);
      }
    }

    private static boolean isMonitoredType(final String type) {
      return FXForwardCurveDefinition.class.getName().equals(type);
    }

  }

  @Override
  protected void addAllConfigurations(List<FunctionConfiguration> functions) {
  }

}
