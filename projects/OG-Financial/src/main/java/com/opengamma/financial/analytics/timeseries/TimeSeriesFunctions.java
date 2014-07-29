/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.BeanDynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.ImpliedDepositCurveFunction;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class TimeSeriesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new TimeSeriesFunctions().getObjectCreating();
  }

  /**
   * Returns a factory that populates the repository with functions that produce {@link ValueRequirementNames#YIELD_CURVE_HISTORICAL_TIME_SERIES} for all curve types <b>except</b>
   * {@link ImpliedDepositCurveFunction#IMPLIED_DEPOSIT}
   * 
   * @param configMaster The configuration master
   * @return A function configuration source
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
      protected boolean isPropogateEvent(ChangeEvent event) {
        return Providers.isMonitoredType(event.getObjectId().getValue());
      }

    };
  }

  /**
   * Function repository configuration source for yield curve functions based on the items defined in a Config Master.
   */
  public static class Providers extends VersionedFunctionConfigurationBean {
    /** The configuration master */
    private ConfigMaster _configMaster;

    /**
     * Sets the configuration master.
     * 
     * @param configMaster The config master
     */
    public void setConfigMaster(final ConfigMaster configMaster) {
      _configMaster = configMaster;
    }

    /**
     * Gets the configuration master.
     * 
     * @return The configuration master.
     */
    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      // search for the names of implied deposit curves and exclude from historical time series function
      final List<String> excludedCurves = new ArrayList<>();
      final ConfigSearchRequest<YieldCurveDefinition> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setType(MultiCurveCalculationConfig.class);
      searchRequest.setVersionCorrection(getVersionCorrection());
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(_configMaster, searchRequest)) {
        final String documentName = configDocument.getName();
        final MultiCurveCalculationConfig config = ((ConfigItem<MultiCurveCalculationConfig>) configDocument.getConfig()).getValue();
        if (config.getCalculationMethod().equals(ImpliedDepositCurveFunction.IMPLIED_DEPOSIT)) {
          excludedCurves.addAll(Arrays.asList(config.getYieldCurveNames()));
        }
      }
      if (excludedCurves.isEmpty()) {
        functions.add(functionConfiguration(YieldCurveHistoricalTimeSeriesFunction.class));
        functions.add(functionConfiguration(YieldCurveConversionSeriesFunction.class));
      } else {
        final String[] excludedCurvesArray = excludedCurves.toArray(new String[excludedCurves.size()]);
        functions.add(functionConfiguration(YieldCurveHistoricalTimeSeriesFunction.class, excludedCurvesArray));
        functions.add(functionConfiguration(YieldCurveConversionSeriesFunction.class, excludedCurvesArray));
      }
    }

    public static boolean isMonitoredType(final String type) {
      return MultiCurveCalculationConfig.class.getName().equals(type);
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CreditSpreadCurveHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(CurveConfigurationHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(CurveHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(DefaultHistoricalTimeSeriesShiftFunction.class));
    functions.add(functionConfiguration(FXForwardCurveHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveNodeReturnSeriesFunction.class));
    functions.add(functionConfiguration(FXReturnSeriesFunction.class));
    functions.add(functionConfiguration(FXVolatilitySurfaceHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesSecurityFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestPositionProviderIdValueFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestSecurityValueFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestValueFunction.class));
    functions.add(functionConfiguration(HistoricalValuationFunction.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunction.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunctionDeprecated.class));
    functions.add(functionConfiguration(VolatilityWeightedFXForwardCurveNodeReturnSeriesFunction.class));
    functions.add(functionConfiguration(VolatilityWeightedFXReturnSeriesFunction.class));
    functions.add(functionConfiguration(VolatilityWeightedYieldCurveNodeReturnSeriesFunction.class));
    functions.add(functionConfiguration(YieldCurveNodeReturnSeriesFunction.class));
  }

}
