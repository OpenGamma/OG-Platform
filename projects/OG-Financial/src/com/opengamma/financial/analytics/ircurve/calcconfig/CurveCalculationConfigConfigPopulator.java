/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveCalculationConfigConfigPopulator {

  public CurveCalculationConfigConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "config master");
    populateConfigMaster(configMaster);
  }

  private static void populateConfigMaster(final ConfigMaster configMaster) {
    final MultiCurveCalculationConfig defaultUSDConfig = new MultiCurveCalculationConfig("Default", new String[] {"FUNDING", "FORWARD_3M"},
        new UniqueIdentifiable[] {Currency.USD, Currency.USD}, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(defaultUSDConfig));
  }

  private static ConfigDocument<MultiCurveCalculationConfig> makeConfigDocument(final MultiCurveCalculationConfig curveConfig) {
    final ConfigDocument<MultiCurveCalculationConfig> configDocument = new ConfigDocument<MultiCurveCalculationConfig>(MultiCurveCalculationConfig.class);
    configDocument.setName(curveConfig.getCalculationConfigName());
    configDocument.setValue(curveConfig);
    return configDocument;
  }
}
