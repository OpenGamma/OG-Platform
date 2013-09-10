/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationFunction;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.model.curve.forward.InstantaneousForwardCurveFunction;
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
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {
    private double _absoluteTolerance = 0.0001;
    private double _relativeTolerance = 0.0001;
    private int _maxIterations = 1000;

    public double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    public void setAbsoluteTolerance(final double absoluteTolerance) {
      _absoluteTolerance = absoluteTolerance;
    }

    public double getRelativeTolerance() {
      return _relativeTolerance;
    }

    public void setRelativeTolerance(final double relativeTolerance) {
      _relativeTolerance = relativeTolerance;
    }

    public int getMaximumIterations() {
      return _maxIterations;
    }

    public void setMaximumIterations(final int maxIterations) {
      _maxIterations = maxIterations;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNegativeOrZero(getAbsoluteTolerance(), "absolute tolerance");
      ArgumentChecker.notNegativeOrZero(getRelativeTolerance(), "relative tolerance");
      ArgumentChecker.notNegativeOrZero(getMaximumIterations(), "maximum iterations");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      addCurveDefaults(functions);
    }

    protected void addCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3];
      args[0] = Double.toString(getAbsoluteTolerance());
      args[1] = Double.toString(getRelativeTolerance());
      args[2] = Integer.toString(getMaximumIterations());
      functions.add(functionConfiguration(CurveDefaults.class, args));
    }
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

    protected void addInterpolatedCurveBuildingFunctions(final List<FunctionConfiguration> functions,
                                                         final Set<Class<? extends CurveTypeConfiguration>> curveTypeConfigClasses,
                                                         final String curveConfigName) {
      if (curveTypeConfigClasses.contains(InflationCurveTypeConfiguration.class)) {
        functions.add(functionConfiguration(InflationProviderDiscountingFunction.class, curveConfigName));
      } else if (curveTypeConfigClasses.contains(IssuerCurveTypeConfiguration.class)) {
        functions.add(functionConfiguration(IssuerProviderDiscountingFunction.class, curveConfigName));
      } else {
        functions.add(functionConfiguration(MultiCurveDiscountingFunction.class, curveConfigName));
        functions.add(functionConfiguration(HullWhiteOneFactorDiscountingCurveFunction.class, curveConfigName));
      }
      functions.add(functionConfiguration(FXMatrixFunction.class, curveConfigName));
      functions.add(functionConfiguration(CurveConstructionConfigurationFunction.class, curveConfigName));
    }

    protected void addCurveBuildingFunctions(final List<FunctionConfiguration> functions,
                                                         final String curveConfigName) {
      functions.add(functionConfiguration(ISDACompliantCurveFunction.class, curveConfigName));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<CurveDefinition> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setType(CurveConstructionConfiguration.class);
      final Class<?>[] curveConstructionConfigurationClasses = new Class[] {CurveConstructionConfiguration.class};
      for (final Class<?> klass : curveConstructionConfigurationClasses) {
        searchRequest.setType(klass);
        for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
          final String documentName = configDocument.getName();
          CurveConstructionConfiguration config = ((ConfigItem<CurveConstructionConfiguration>) configDocument.getConfig()).getValue();
          
          /*
           * We need the CurveTypeConfigurations of the curves contained within the CurveConstructionConfiguration to
           * decided whether we want to add the curve building function for this CurveConstructionConfiguration.
           */
          Set<Class<? extends CurveTypeConfiguration>> allCurveTypeConfigs = extractCurveTypeConfigurationClasses(config);
          
          addInterpolatedCurveBuildingFunctions(functions, allCurveTypeConfigs, documentName);
        }
      }

      searchRequest.setType(CurveDefinition.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final String documentName = configDocument.getName();
        addCurveBuildingFunctions(functions, documentName);
      }
    }

    /**
     * Extracts the CurveTypeConfiguration classes from a given CurveConstructionConfiguration.
     * <p>
     * This allows us to decide whether we want a function to be supported based on the contained CurveTypeConfigurations.
     * 
     * @param config the CurveConstructionConfiguration to retrieve the curve types from.
     * @return a Set of CurveTypeConfigurations.
     */
    private Set<Class<? extends CurveTypeConfiguration>> extractCurveTypeConfigurationClasses(CurveConstructionConfiguration config) {
      Set<Class<? extends CurveTypeConfiguration>> allCurveTypeConfigs = new HashSet<>();
      for (CurveGroupConfiguration group: config.getCurveGroups()) {
        for (List<CurveTypeConfiguration> curveTypeConfigs: group.getTypesForCurves().values()) {
          for (CurveTypeConfiguration curveTypeConfig: curveTypeConfigs) {
            allCurveTypeConfigs.add(curveTypeConfig.getClass());
          }
        }
      }
      return allCurveTypeConfigs;
    }
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(HullWhiteOneFactorParametersFunction.class));
    functions.add(functionConfiguration(G2ppParametersFunction.class));
    functions.add(functionConfiguration(InstantaneousForwardCurveFunction.class));
  }
}
