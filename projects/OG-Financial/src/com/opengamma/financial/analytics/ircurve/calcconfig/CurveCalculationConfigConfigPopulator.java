/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
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
    final String usdFundingCurveName = "Funding";
    final String usdForward3MCurveName = "Forward3M";
    final MultiCurveCalculationConfig defaultUSDConfig = new MultiCurveCalculationConfig("Default", new String[] {usdFundingCurveName, usdForward3MCurveName },
        new UniqueIdentifiable[] {Currency.USD, Currency.USD }, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING,
        getUSDCurveInstrumentConfig(usdFundingCurveName, usdForward3MCurveName));
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(defaultUSDConfig));
  }

  private static ConfigDocument<MultiCurveCalculationConfig> makeConfigDocument(final MultiCurveCalculationConfig curveConfig) {
    final ConfigDocument<MultiCurveCalculationConfig> configDocument = new ConfigDocument<MultiCurveCalculationConfig>(MultiCurveCalculationConfig.class);
    configDocument.setName(curveConfig.getCalculationConfigName());
    configDocument.setValue(curveConfig);
    return configDocument;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getUSDCurveInstrumentConfig(final String fundingCurveName, final String forward3MCurveName) {
    final String[] fundingOnly = new String[] {fundingCurveName };
    final String[] forward3MOnly = new String[] {forward3MCurveName };
    final String[] fundingForward3M = new String[] {fundingCurveName, forward3MCurveName };
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<String, CurveInstrumentConfig>();
    final Map<StripInstrumentType, String[]> fundingConfig = new HashMap<StripInstrumentType, String[]>();
    fundingConfig.put(StripInstrumentType.CASH, fundingOnly);
    fundingConfig.put(StripInstrumentType.OIS_SWAP, new String[] {fundingCurveName, fundingCurveName });
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<StripInstrumentType, String[]>();
    forward3MConfig.put(StripInstrumentType.LIBOR, forward3MOnly);
    forward3MConfig.put(StripInstrumentType.FUTURE, fundingForward3M);
    forward3MConfig.put(StripInstrumentType.FRA_3M, fundingForward3M);
    forward3MConfig.put(StripInstrumentType.SWAP_6M, fundingForward3M);
    result.put(fundingCurveName, new CurveInstrumentConfig(fundingConfig));
    result.put(forward3MCurveName, new CurveInstrumentConfig(forward3MConfig));
    return result;
  }
}
