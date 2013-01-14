/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.List;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class DemoStandardFunctionConfiguration extends StandardFunctionConfiguration {

  public static RepositoryConfigurationSource instance() {
    return new DemoStandardFunctionConfiguration().getObjectCreating();
  }

  public DemoStandardFunctionConfiguration() {
    setMark2MarketField("PX_LAST");
    setCostOfCarryField("COST_OF_CARRY");
  }

  @Override
  protected CurrencyInfo audCurrencyInfo() {
    final CurrencyInfo i = super.audCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveAUDConfig");
    i.setCurveName(null, "Discounting");
    i.setCubeName(null, "BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo brlCurrencyInfo() {
    final CurrencyInfo i = super.brlCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveBRLConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo cadCurrencyInfo() {
    final CurrencyInfo i = super.cadCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveCADConfig");
    i.setCurveName(null, "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveCHFConfig");
    i.setCurveName(null, "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveEURConfig");
    i.setCurveName(null, "Discounting");
    i.setSurfaceName("model/irfutureoption", "DEFAULT_PRICE");
    i.setSurfaceName("model/swaption", "DEFAULT");
    i.setCubeName(null, "BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveGBPConfig");
    i.setCurveName(null, "Discounting");
    i.setCubeName(null, "BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo hkdCurrencyInfo() {
    final CurrencyInfo i = super.hkdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveHKDConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo hufCurrencyInfo() {
    final CurrencyInfo i = super.hufCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveHUFConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveJPYConfig");
    i.setCurveName(null, "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo krwCurrencyInfo() {
    final CurrencyInfo i = super.krwCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveKRWConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo mxnCurrencyInfo() {
    final CurrencyInfo i = super.mxnCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveMXNConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo nzdCurrencyInfo() {
    final CurrencyInfo i = super.nzdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveNZDConfig");
    i.setCurveName(null, "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo rubCurrencyInfo() {
    final CurrencyInfo i = super.rubCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultCashCurveRUBConfig");
    i.setCurveName(null, "Cash");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveUSDConfig");
    i.setCurveName(null, "Discounting");
    i.setSurfaceName("model/bondfutureoption", "BBG");
    i.setSurfaceName("model/futureoption", "BBG");
    i.setSurfaceName("model/irfutureoption", "DEFAULT_PRICE");
    i.setCubeName(null, "BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    final CurrencyPairInfo i = super.eurChfCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.eurJpyCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdAudCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdBrlCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdBrlCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdCadCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdCadCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdChfCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdEurCurrencyPairInfo();
    i.setCurveName(null, "DiscountingImplied");
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdGbpCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdHkdCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdHkdCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdHufCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdHufCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdKrwCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdKrwCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdMxnCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdMxnCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdNzdCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdNzdCurrencyPairInfo();
    i.setSurfaceName(null, "TULLETT");
    return i;
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  @Override
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    super.setPNLFunctionDefaults(defaults);
    defaults.setCurveName("FUNDING");
    defaults.setPayCurveName("FUNDING");
    defaults.setReceiveCurveName("FUNDING");
  }

}
