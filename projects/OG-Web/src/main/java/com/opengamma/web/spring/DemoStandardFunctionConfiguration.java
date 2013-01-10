/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.List;

import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
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
    i.setDefaultCurveConfiguration("DefaultTwoCurveAUDConfig");
    i.setDefaultCurve("Discounting");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo brlCurrencyInfo() {
    final CurrencyInfo i = super.brlCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveBRLConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo cadCurrencyInfo() {
    final CurrencyInfo i = super.cadCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveCADConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveCHFConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveEURConfig");
    i.setDefaultCurve("Discounting");
    i.setIRFutureOptionSurface("DEFAULT_PRICE");
    i.setDefaultCube("BLOOMBERG");
    i.setSwaptionSurface("DEFAULT");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveGBPConfig");
    i.setDefaultCurve("Discounting");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo hkdCurrencyInfo() {
    final CurrencyInfo i = super.hkdCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveHKDConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo hufCurrencyInfo() {
    final CurrencyInfo i = super.hufCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveHUFConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveJPYConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo krwCurrencyInfo() {
    final CurrencyInfo i = super.krwCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveKRWConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo mxnCurrencyInfo() {
    final CurrencyInfo i = super.mxnCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveMXNConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo nzdCurrencyInfo() {
    final CurrencyInfo i = super.nzdCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveNZDConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo rubCurrencyInfo() {
    final CurrencyInfo i = super.rubCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveRUBConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveUSDConfig");
    i.setDefaultCurve("Discounting");
    i.setISDACurveConfiguration("ISDA");
    i.setISDACurve("ISDA");
    i.setBondFutureOptionSurface("BBG");
    i.setFutureOptionSurface("BBG_S ");
    i.setIRFutureOptionSurface("DEFAULT_PRICE");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdBrlCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdCadCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultCurve("DiscountingImplied");
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdHkdCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdHufCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdKrwCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdMxnCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdNzdCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
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
  protected RepositoryConfigurationSource pnlFunctions() {
    return CombiningRepositoryConfigurationSource.of(super.pnlFunctions(), PNLFunctions.defaults("FUNDING", "FUNDING", "FUNDING"));
  }

}
