/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.function;

import java.util.List;

import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardPropertiesFunctions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionPropertiesFunctions;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.util.function.Function;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Constructs a standard function repository.
 */
@SuppressWarnings("deprecation")
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {

  /**
   * Gets an instance of the example function configuration.
   * 
   * @return Gets the instance
   */
  public static FunctionConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  /**
   * The function configuration.
   */
  public ExampleStandardFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
    setAbsoluteTolerance(0.0001);
    setRelativeTolerance(0.0001);
    setMaximumIterations(1000);
  }

  @Override
  protected CurrencyInfo audCurrencyInfo() {
    final CurrencyInfo i = super.audCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultThreeCurveAUDConfig");
    i.setCurveConfiguration("model/fxforward", "AUDFX");
    i.setCurveConfiguration("mode/fxoption/black", "DefaultThreeCurveAUDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/fxforward", "CHFFX");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveCHFConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("mode/future", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/fxforward", "EURFX");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveEURConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/fxforward", "GBPFX");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveGBPConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/fxforward", "JPYFX");
    i.setCurveConfiguration("model/fxforward/black", "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveJPYConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER3");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/fxforward", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/fxforward/black", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/bond/riskFree", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/bond/credit", "DefaultTwoCurveUSDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "Discounting");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setCurveName("model/bond/riskFree", "Discounting");
    i.setCurveName("model/bond/credit", "Discounting");
    i.setCubeDefinitionName("model/sabrcube", "USD PROVIDER1");
    i.setCubeSpecificationName("model/sabrcube", "USD PROVIDER1");
    i.setSurfaceDefinitionName("model/sabrcube", "US FWD SWAP PROVIDER1");
    i.setSurfaceSpecificationName("model/sabrcube", "US FWD SWAP PROVIDER1");
    i.setForwardCurveName(null, "Forward3M");
    i.setSurfaceName(null, "SECONDARY");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdEurCurrencyPairInfo();
    i.setCurveName("model/volatility/surface/black", "Discounting");
    i.setSurfaceName("model/volatility/surface/black", "DEFAULT");
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo chfJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  @Override
  protected FunctionConfigurationSource forexFunctions() {
    final FXForwardPropertiesFunctions fxForwardDefaults = new FXForwardPropertiesFunctions();
    setForexForwardDefaults(fxForwardDefaults);
    final FunctionConfigurationSource fxForwardRepository = getRepository(fxForwardDefaults);
    final FXOptionPropertiesFunctions fxOptionDefaults = new FXOptionPropertiesFunctions();
    setForexOptionDefaults(fxOptionDefaults);
    final FunctionConfigurationSource fxOptionRepository = getRepository(fxOptionDefaults);
    return CombiningFunctionConfigurationSource.of(fxForwardRepository, fxOptionRepository);
  }

  protected void setForexOptionDefaults(final FXOptionPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, FXOptionPropertiesFunctions.CurrencyInfo>() {
      @Override
      public FXOptionPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final FXOptionPropertiesFunctions.CurrencyInfo d = new FXOptionPropertiesFunctions.CurrencyInfo();
        setForexOptionDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo, FXOptionPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public FXOptionPropertiesFunctions.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final FXOptionPropertiesFunctions.CurrencyPairInfo d = new FXOptionPropertiesFunctions.CurrencyPairInfo();
        setForexOptionDefaults(i, d);
        return d;
      }
    }));

  }

  protected void setForexForwardDefaults(final FXForwardPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, FXForwardPropertiesFunctions.CurrencyInfo>() {
      @Override
      public FXForwardPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final FXForwardPropertiesFunctions.CurrencyInfo d = new FXForwardPropertiesFunctions.CurrencyInfo();
        setForexForwardDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo, FXForwardPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public FXForwardPropertiesFunctions.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final FXForwardPropertiesFunctions.CurrencyPairInfo d = new FXForwardPropertiesFunctions.CurrencyPairInfo();
        setForexForwardDefaults(i, d);
        return d;
      }
    }));

  }

  protected void setForexForwardDefaults(final CurrencyInfo i, final FXForwardPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/fxforward"));
    defaults.setDiscountingCurve(i.getCurveName("model/fxforward"));
  }

  protected void setForexForwardDefaults(final CurrencyPairInfo i, final FXForwardPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setForwardCurveName(i.getForwardCurveName("model/fxforward"));
  }

  protected void setForexOptionDefaults(final CurrencyInfo i, final FXOptionPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/fxoption/black"));
    defaults.setDiscountingCurve(i.getCurveName("model/fxoption/black"));
  }

  protected void setForexOptionDefaults(final CurrencyPairInfo i, final FXOptionPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/fxoption/black"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  @Override
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    super.setPNLFunctionDefaults(defaults);
    defaults.setCurveName("SECONDARY");
    defaults.setPayCurveName("SECONDARY");
    defaults.setReceiveCurveName("SECONDARY");
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions());
  }
}
