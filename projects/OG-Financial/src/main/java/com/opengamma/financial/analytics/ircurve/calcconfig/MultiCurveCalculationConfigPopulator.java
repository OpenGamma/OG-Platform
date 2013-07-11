/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class MultiCurveCalculationConfigPopulator {

  public MultiCurveCalculationConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "config master");
    populateConfigMaster(configMaster);
  }

  private static void populateConfigMaster(final ConfigMaster configMaster) {
    final String discountingCurveName = "Discounting";
    final String forward3MCurveName = "Forward3M";
    final String forward6MCurveName = "Forward6M";
    final String forward3MFutCurveName = "Forward3MFut";
    final MultiCurveCalculationConfig defaultUSDConfig = new MultiCurveCalculationConfig("DefaultTwoCurveUSDConfig", new String[] {discountingCurveName, forward3MCurveName},
        ComputationTargetSpecification.of(Currency.USD), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getTwoCurveUSDInstrumentConfig(discountingCurveName, forward3MCurveName));
    final MultiCurveCalculationConfig defaultGBPConfig = new MultiCurveCalculationConfig("DefaultTwoCurveGBPConfig", new String[] {discountingCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.GBP), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getTwoCurveGBPInstrumentConfig(discountingCurveName, forward6MCurveName));
    final MultiCurveCalculationConfig defaultEURConfig = new MultiCurveCalculationConfig("DefaultTwoCurveEURConfig", new String[] {discountingCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.EUR), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getTwoCurveEURInstrumentConfig(discountingCurveName, forward6MCurveName));
    final MultiCurveCalculationConfig defaultJPYConfig = new MultiCurveCalculationConfig("DefaultTwoCurveJPYConfig", new String[] {discountingCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.JPY), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getTwoCurveJPYInstrumentConfig(discountingCurveName, forward6MCurveName));
    final MultiCurveCalculationConfig defaultCHFConfig = new MultiCurveCalculationConfig("DefaultTwoCurveCHFConfig", new String[] {discountingCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.CHF), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getTwoCurveCHFInstrumentConfig(discountingCurveName, forward6MCurveName));
    final MultiCurveCalculationConfig eurOIS3M6M = new MultiCurveCalculationConfig("EUR-OIS-3M-6M", new String[] {discountingCurveName, forward3MCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.EUR), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getEUROISInstrumentConfig1(discountingCurveName, forward3MCurveName, forward6MCurveName));
    final MultiCurveCalculationConfig eurOIS3MFut6M = new MultiCurveCalculationConfig("EUR-OIS-3MFut-6M", new String[] {discountingCurveName, forward3MFutCurveName, forward6MCurveName},
        ComputationTargetSpecification.of(Currency.EUR), MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING, getEUROISFutInstrumentConfig1(discountingCurveName, forward3MFutCurveName, forward6MCurveName));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(defaultUSDConfig));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(defaultGBPConfig));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(defaultEURConfig));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(defaultJPYConfig));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(defaultCHFConfig));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(getAUDThreeCurveConfig()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(getAUDDiscountingCurveConfig()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(getAUDForwardCurvesConfig()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(getSingleAUDCurveConfig()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(eurOIS3M6M));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(eurOIS3MFut6M));
  }

  private static ConfigItem<MultiCurveCalculationConfig> makeConfig(final MultiCurveCalculationConfig curveConfig) {
    final ConfigItem<MultiCurveCalculationConfig> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getCalculationConfigName());
    return config;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getTwoCurveUSDInstrumentConfig(final String discountingCurveName, final String forward3MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward3MOnly = new String[] {forward3MCurveName};
    final String[] discountingForward3M = new String[] {discountingCurveName, forward3MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<>();
    forward3MConfig.put(StripInstrumentType.LIBOR, forward3MOnly);
    forward3MConfig.put(StripInstrumentType.FRA_3M, discountingForward3M);
    forward3MConfig.put(StripInstrumentType.SWAP_3M, discountingForward3M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward3MCurveName, new CurveInstrumentConfig(forward3MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getEUROISInstrumentConfig1(final String discountingCurveName, final String forward3MCurveName,
      final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward3MOnly = new String[] {forward3MCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward3M = new String[] {discountingCurveName, forward3MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<>();
    forward3MConfig.put(StripInstrumentType.EURIBOR, forward3MOnly);
    forward3MConfig.put(StripInstrumentType.FRA_3M, discountingForward3M);
    forward3MConfig.put(StripInstrumentType.SWAP_3M, discountingForward3M);
    final Map<StripInstrumentType, String[]> forward6MConfig = new HashMap<>();
    forward6MConfig.put(StripInstrumentType.EURIBOR, forward6MOnly);
    forward6MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward6MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward3MCurveName, new CurveInstrumentConfig(forward3MConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward6MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getEUROISFutInstrumentConfig1(final String discountingCurveName, final String forward3MCurveName,
      final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward3MOnly = new String[] {forward3MCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward3M = new String[] {discountingCurveName, forward3MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<>();
    forward3MConfig.put(StripInstrumentType.EURIBOR, forward3MOnly);
    forward3MConfig.put(StripInstrumentType.FRA_3M, discountingForward3M);
    forward3MConfig.put(StripInstrumentType.FUTURE, discountingForward3M);
    forward3MConfig.put(StripInstrumentType.SWAP_3M, discountingForward3M);
    final Map<StripInstrumentType, String[]> forward6MConfig = new HashMap<>();
    forward6MConfig.put(StripInstrumentType.EURIBOR, forward6MOnly);
    forward6MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward6MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward3MCurveName, new CurveInstrumentConfig(forward3MConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward6MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getTwoCurveGBPInstrumentConfig(final String discountingCurveName, final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward6MConfig = new HashMap<>();
    forward6MConfig.put(StripInstrumentType.LIBOR, forward6MOnly);
    forward6MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward6MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward6MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getTwoCurveEURInstrumentConfig(final String discountingCurveName, final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward6MConfig = new HashMap<>();
    forward6MConfig.put(StripInstrumentType.EURIBOR, forward6MOnly);
    forward6MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward6MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward6MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getTwoCurveJPYInstrumentConfig(final String discountingCurveName, final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<>();
    forward3MConfig.put(StripInstrumentType.LIBOR, forward6MOnly);
    forward3MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward3MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward3MConfig));
    return result;
  }

  private static LinkedHashMap<String, CurveInstrumentConfig> getTwoCurveCHFInstrumentConfig(final String discountingCurveName, final String forward6MCurveName) {
    final String[] discountingOnly = new String[] {discountingCurveName};
    final String[] forward6MOnly = new String[] {forward6MCurveName};
    final String[] discountingForward6M = new String[] {discountingCurveName, forward6MCurveName};
    final String[] discountingDiscounting = new String[] {discountingCurveName, discountingCurveName};
    final LinkedHashMap<String, CurveInstrumentConfig> result = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, discountingOnly);
    discountingConfig.put(StripInstrumentType.OIS_SWAP, discountingDiscounting);
    final Map<StripInstrumentType, String[]> forward3MConfig = new HashMap<>();
    forward3MConfig.put(StripInstrumentType.LIBOR, forward6MOnly);
    forward3MConfig.put(StripInstrumentType.FRA_6M, discountingForward6M);
    forward3MConfig.put(StripInstrumentType.SWAP_6M, discountingForward6M);
    result.put(discountingCurveName, new CurveInstrumentConfig(discountingConfig));
    result.put(forward6MCurveName, new CurveInstrumentConfig(forward3MConfig));
    return result;
  }

  private static MultiCurveCalculationConfig getAUDThreeCurveConfig() {
    final String[] yieldCurveNames = new String[] {"Discounting", "ForwardBasis3M", "ForwardBasis6M"};
    final Currency target = Currency.AUD;
    final String calculationMethod = MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
    final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, new String[] {"Discounting"});
    discountingConfig.put(StripInstrumentType.OIS_SWAP, new String[] {"Discounting", "Discounting"});
    final Map<StripInstrumentType, String[]> forwardBasis3MConfig = new HashMap<>();
    forwardBasis3MConfig.put(StripInstrumentType.BASIS_SWAP, new String[] {"Discounting", "ForwardBasis3M", "ForwardBasis6M"});
    forwardBasis3MConfig.put(StripInstrumentType.SWAP_3M, new String[] {"Discounting", "ForwardBasis3M"});
    forwardBasis3MConfig.put(StripInstrumentType.LIBOR, new String[] {"ForwardBasis3M"});
    final Map<StripInstrumentType, String[]> forwardBasis6MConfig = new HashMap<>();
    forwardBasis6MConfig.put(StripInstrumentType.BASIS_SWAP, new String[] {"Discounting", "ForwardBasis3M", "ForwardBasis6M"});
    forwardBasis6MConfig.put(StripInstrumentType.SWAP_6M, new String[] {"Discounting", "ForwardBasis6M"});
    forwardBasis6MConfig.put(StripInstrumentType.LIBOR, new String[] {"ForwardBasis6M"});
    curveExposuresForInstruments.put("Discounting", new CurveInstrumentConfig(discountingConfig));
    curveExposuresForInstruments.put("ForwardBasis3M", new CurveInstrumentConfig(forwardBasis3MConfig));
    curveExposuresForInstruments.put("ForwardBasis6M", new CurveInstrumentConfig(forwardBasis6MConfig));
    return new MultiCurveCalculationConfig("DefaultThreeCurveAUDConfig", yieldCurveNames, ComputationTargetSpecification.of(target), calculationMethod, curveExposuresForInstruments);
  }

  private static MultiCurveCalculationConfig getAUDDiscountingCurveConfig() {
    final String[] yieldCurveNames = new String[] {"Discounting"};
    final Currency target = Currency.AUD;
    final String calculationMethod = MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
    final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> discountingConfig = new HashMap<>();
    discountingConfig.put(StripInstrumentType.CASH, new String[] {"Discounting"});
    discountingConfig.put(StripInstrumentType.OIS_SWAP, new String[] {"Discounting", "Discounting"});
    curveExposuresForInstruments.put("Discounting", new CurveInstrumentConfig(discountingConfig));
    return new MultiCurveCalculationConfig("DiscountingAUDConfig", yieldCurveNames, ComputationTargetSpecification.of(target), calculationMethod, curveExposuresForInstruments);
  }

  private static MultiCurveCalculationConfig getAUDForwardCurvesConfig() {
    final String[] yieldCurveNames = new String[] {"ForwardBasis3M", "ForwardBasis6M"};
    final Currency target = Currency.AUD;
    final String calculationMethod = MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
    final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> forwardBasis3MConfig = new HashMap<>();
    forwardBasis3MConfig.put(StripInstrumentType.BASIS_SWAP, new String[] {"Discounting", "ForwardBasis3M", "ForwardBasis6M"});
    forwardBasis3MConfig.put(StripInstrumentType.SWAP_3M, new String[] {"Discounting", "ForwardBasis3M"});
    forwardBasis3MConfig.put(StripInstrumentType.LIBOR, new String[] {"ForwardBasis3M"});
    final Map<StripInstrumentType, String[]> forwardBasis6MConfig = new HashMap<>();
    forwardBasis6MConfig.put(StripInstrumentType.BASIS_SWAP, new String[] {"Discounting", "ForwardBasis3M", "ForwardBasis6M"});
    forwardBasis6MConfig.put(StripInstrumentType.SWAP_6M, new String[] {"Discounting", "ForwardBasis6M"});
    forwardBasis6MConfig.put(StripInstrumentType.LIBOR, new String[] {"ForwardBasis6M"});
    curveExposuresForInstruments.put("ForwardBasis3M", new CurveInstrumentConfig(forwardBasis3MConfig));
    curveExposuresForInstruments.put("ForwardBasis6M", new CurveInstrumentConfig(forwardBasis6MConfig));
    final LinkedHashMap<String, String[]> exogenousConfigAndCurveNames = new LinkedHashMap<>();
    exogenousConfigAndCurveNames.put("DiscountingAUDConfig", new String[] {"Discounting"});
    return new MultiCurveCalculationConfig("ForwardFromDiscountingAUDConfig", yieldCurveNames, ComputationTargetSpecification.of(target), calculationMethod, curveExposuresForInstruments,
        exogenousConfigAndCurveNames);
  }

  private static MultiCurveCalculationConfig getSingleAUDCurveConfig() {
    final String[] yieldCurveNames = new String[] {"Single"};
    final String[] twoCurveNames = new String[] {"Single", "Single"};
    final Currency target = Currency.AUD;
    final String calculationMethod = MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
    final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments = new LinkedHashMap<>();
    final Map<StripInstrumentType, String[]> singleConfig = new HashMap<>();
    singleConfig.put(StripInstrumentType.FUTURE, twoCurveNames);
    singleConfig.put(StripInstrumentType.CASH, yieldCurveNames);
    singleConfig.put(StripInstrumentType.SWAP_3M, twoCurveNames);
    singleConfig.put(StripInstrumentType.SWAP_6M, twoCurveNames);
    curveExposuresForInstruments.put("Single", new CurveInstrumentConfig(singleConfig));
    return new MultiCurveCalculationConfig("SingleAUDConfig", yieldCurveNames, ComputationTargetSpecification.of(target), calculationMethod, curveExposuresForInstruments);
  }
}
