/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Populates the examples database with multi-curve calculation configurations that 
 * produce FX-implied curves.
 */

public class ExampleFXImpliedMultiCurveCalculationConfigPopulator {
  /** Curve names */
  private static final String[] CURVE_NAMES = new String[] {"DEFAULT"};
  /** Exogenous configurations */
  private static final LinkedHashMap<String, String[]> EXOGENOUS_CURVE_CONFIGS = new LinkedHashMap<>();
  
  static {
    EXOGENOUS_CURVE_CONFIGS.put("DefaultTwoCurveUSDConfig", new String[] {"Discounting"});
  }
  
  /**
   * @param configMaster The configuration master, not null
   */
  public ExampleFXImpliedMultiCurveCalculationConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    populateConfigMaster(configMaster);
  }
  
  private static void populateConfigMaster(ConfigMaster configMaster) {
    UnorderedCurrencyPair[] pairs = ExampleViewsPopulator.CURRENCY_PAIRS;
    Set<Currency> uniqueCurrencies = new HashSet<>();
    for (UnorderedCurrencyPair pair : pairs) {
      uniqueCurrencies.add(pair.getFirstCurrency());
      uniqueCurrencies.add(pair.getSecondCurrency());
    }
    for (Currency currency : uniqueCurrencies) {
      final String name = currency.getCode() + "FX";
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(currency);
      final MultiCurveCalculationConfig config = new MultiCurveCalculationConfig(name, CURVE_NAMES, target, FXImpliedYieldCurveFunction.FX_IMPLIED, 
          null, EXOGENOUS_CURVE_CONFIGS);
      ConfigMasterUtils.storeByName(configMaster, makeConfig(config));
    }
  }
  
  private static ConfigItem<MultiCurveCalculationConfig> makeConfig(MultiCurveCalculationConfig curveConfig) {
    final ConfigItem<MultiCurveCalculationConfig> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getCalculationConfigName());
    return config;
  }
}

