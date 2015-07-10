/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.QuantityFunction;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.InterpolatedForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveFuturePriceImpliedPerTickerDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurvePerTickerDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionCalculationMethodDefaultFunction;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerExchangeDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerExchangeDefaults;
import com.opengamma.financial.analytics.model.equity.option.ListedEquityOptionPerSecurityTypeDefaults;
import com.opengamma.financial.analytics.model.equity.option.ListedEquityOptionPerTickerDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.future.FuturesPricingDefaults;
import com.opengamma.financial.analytics.model.futureoption.EquityFutureOptionBlackLognormalDefaults;
import com.opengamma.financial.analytics.model.futureoption.EquityFutureOptionSurfaceCalculationMethodDefaults;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerCurrencyDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerExchangeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerTickerDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityFutureBlackVolatilitySurfacePerCurrencyDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.value.SimpleRenamingFunction;
import com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
@SuppressWarnings("deprecation")
public class DemoStandardFunctionConfiguration extends StandardFunctionConfiguration {

  public static FunctionConfigurationSource instance() {
    return new DemoStandardFunctionConfiguration().getObjectCreating();
  }

  public DemoStandardFunctionConfiguration() {
    setMark2MarketField("PX_LAST");
    setCostOfCarryField("COST_OF_CARRY");
    setAbsoluteTolerance(1.0E-9); // 0.0001
    setRelativeTolerance(1.0E-9); // 0.0001
    setMaximumIterations(100); // 1000
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
    functions.add(functionConfiguration(QuantityFunction.class));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
    addEquityDividendYieldFuturesDefaults(functions);
    addEquityForwardDefaults(functions);
    addEquityFuturePricingDefaults(functions);
    addEquityIndexOptionBlackVolatilitySurfaceDefaults(functions);
    addEquityFutureOptionBlackVolatilitySurfaceDefaults(functions);
    addEquityOptionDefaults(functions);
    addEquityFutureOptionDefaults(functions);
    addEquityPureVolatilitySurfaceDefaults(functions);
    addEquityVarianceSwapDefaults(functions);
    addEquityOptionCalculationMethodDefaults(functions);
    addListedEquityOptionDefaults(functions);
    addValueRenamingFunctions(functions);
  }

  /**
   * These functions provide aliases for the user to rename one of OpenGamma's ValueRequirementNames to one of their own.
   * In addition to adding the name to a class that extends ValueRenamingFunction, such as SimpleRenamingFunction, one must also
   * add the name into ValueRequirementNames or to a project-specific name class 
   * and include that into the [webBasics] section of the engine.ini configuration file. <p>
   * eg: [webBasics] <p>
   *  valueRequirementNameClasses = com.opengamma.engine.value.ValueRequirementNames,com.opengamma.yourproject.function.YourProjectValueRequirementNames
   * @param functions Extends this List<FunctionConfiguration>
   */
  protected void addValueRenamingFunctions(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(SimpleRenamingFunction.class, ValueRequirementNames.VALUE_DELTA, ValueRequirementNames.NET_MARKET_VALUE));
  }

  @Override
  protected CurrencyInfo audCurrencyInfo() {
    final CurrencyInfo i = super.audCurrencyInfo();
    i.setCurveConfiguration(null, "AUDFX");
    i.setCurveConfiguration("model/credit/yield", "ISDAAUDCurveConfig");
    i.setCurveConfiguration("model/forex", "AUDFX");
    i.setCurveConfiguration("model/pnl", "AUDFX");
    i.setCurveName(null, "FX");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveName("model/forex/discounting", "FX");
    i.setCurveName("model/pnl/discounting", "FX");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
    i.setCurveCalculationMethodName("model/forex/discounting", "FXImplied");
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
    i.setCurveConfiguration("model/credit/yield", "ISDACADCurveConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
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
    i.setCurveConfiguration("model/xccyswap", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/credit/yield", "ISDAEURCurveConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
    i.setSurfaceName("model/irfutureoption", "DEFAULT_PRICE");
    i.setSurfaceName("model/swaption", "DEFAULT");
    i.setCubeName(null, "BLOOMBERG");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/credit/yield", "ISDAGBPCurveConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
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
    i.setCurveConfiguration("model/credit/yield", "ISDAJPYCurveConfig");
    i.setCurveConfiguration("model/forex", "JPYFX");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveName("model/forex/discounting", "FX");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
    return i;
  }

  @Override
  protected CurrencyInfo krwCurrencyInfo() {
    final CurrencyInfo i = super.krwCurrencyInfo();
    i.setCurveConfiguration(null, "SingleCurveKRWConfig");
    i.setCurveConfiguration("model/forex", "KRWFX");
    i.setCurveName(null, "Forward");
    i.setCurveName("model/forex/discounting", "FX");
    i.setCurveCalculationMethodName("model/forex/discounting", "FXImplied");
    return i;
  }

  @Override
  protected CurrencyInfo mxnCurrencyInfo() {
    final CurrencyInfo i = super.mxnCurrencyInfo();
    i.setCurveConfiguration(null, "SingleCurveMXNConfig");
    i.setCurveName(null, "Forward28D");
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
    i.setCurveConfiguration("model/credit/yield", "ISDAUSDCurveConfig");
    i.setCurveConfiguration("model/xccyswap", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveUSDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/credit/yield", "ISDA");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setCurveCalculationMethodName("model/credit/yield", "ISDA");
    i.setCurveCalculationMethodName("model/credit/hazardrate", "ISDA");
    i.setSurfaceName("model/bondfutureoption", "BBG");
    i.setSurfaceName("model/futureoption", "BBG");
    i.setSurfaceName("model/irfutureoption", "DEFAULT_PRICE");
    i.setSurfaceName("model/volatility/surface/black", "BBG");
    i.setCubeDefinitionName("model/sabrcube", "BLOOMBERG");
    i.setCubeSpecificationName("model/sabrcube", "BLOOMBERG");
    i.setSurfaceDefinitionName("model/sabrcube", "BLOOMBERG");
    i.setSurfaceSpecificationName("model/sabrcube", "BLOOMBERG");
    i.setForwardCurveName("model/futureoption", "BBG");
    i.setForwardCurveName("model/curve/commodityforward", "BBG");
    i.setForwardCurveName("model/curve/equityforward", "BBG");
    i.setForwardCurveCalculationMethod("model/futureoption", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    i.setForwardCurveCalculationMethod("model/commodityforward", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    i.setForwardCurveCalculationMethod("model/equityforward", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    i.setSurfaceCalculationMethod("model/futureoption", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    return i;
  }

  @Override
  protected CurrencyPairInfo audKrwCurrencyPairInfo() {
    final CurrencyPairInfo i = super.audKrwCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
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
    i.setSurfaceName(null, "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo jpyKrwCurrencyPairInfo() {
    final CurrencyPairInfo i = super.jpyKrwCurrencyPairInfo();
    i.setSurfaceName(null, "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdAudCurrencyPairInfo();
    i.setSurfaceName(null, "DEFAULT");
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
    i.setForwardCurveName("model/forex/forward", "DEFAULT");
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
    i.setSurfaceName(null, "DEFAULT");
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
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  @Override
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    super.setPNLFunctionDefaults(defaults);
    defaults.setCurveName("FUNDING");
    defaults.setPayCurveName("FUNDING");
    defaults.setReceiveCurveName("FUNDING");
  }

  protected void addEquityDividendYieldFuturesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityFutureDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveNames()
        .createPerEquityDefaults();
    final List<String> equityFutureDefaultsWithPriority = new ArrayList<>();
    equityFutureDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityFutureDefaultsWithPriority.addAll(equityFutureDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityDividendYieldPricingDefaults.class.getName(), equityFutureDefaultsWithPriority));
  }

  protected void addEquityForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    // Interpolation Defaults
    functionConfigs.add(functionConfiguration(InterpolatedForwardCurveDefaults.class,
        Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR));
    // EquityForward PerEquityDefaults
    final List<String> equityForwardDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .createPerEquityDefaults();
    final List<String> equityForwardPerEquityDefaults = new ArrayList<>();
    equityForwardPerEquityDefaults.add(PriorityClass.ABOVE_NORMAL.name());
    equityForwardPerEquityDefaults.addAll(equityForwardDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardPerEquityDefaults.class.getName(), equityForwardPerEquityDefaults));

    // EquityForwardCurve PerTickerDefaults (like above but for a curve requirement)
    final EquityInstrumentDefaultValues.Builder equityForwardCurveEquityAndExchangeDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useDividendTypes();
    final List<String> equityForwardCurvePerTickerDefaults = new ArrayList<>();
    equityForwardCurvePerTickerDefaults.add(PriorityClass.ABOVE_NORMAL.name());
    equityForwardCurvePerTickerDefaults.addAll(equityForwardCurveEquityAndExchangeDefaults.createPerEquityDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurvePerTickerDefaults.class.getName(), equityForwardCurvePerTickerDefaults));

    // EquityForwardCurve PerCurrencyDefaults
    final EquityInstrumentDefaultValues.Builder equityForwardCurveCurrencyDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useDividendTypes();
    final List<String> equityForwardCurvePerCurrencyDefaults = new ArrayList<>();
    equityForwardCurvePerCurrencyDefaults.add(PriorityClass.BELOW_NORMAL.name());
    equityForwardCurvePerCurrencyDefaults.addAll(equityForwardCurveCurrencyDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults.class.getName(), equityForwardCurvePerCurrencyDefaults));

    // EquityForwardCurve (from Futures) PerTickerDefaults
    final EquityInstrumentDefaultValues.Builder equityForwardCurveFromFuturesDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useForwardCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames();
    final List<String> equityForwardCurveFromFuturesPerTickerDefaults = new ArrayList<>();
    equityForwardCurveFromFuturesPerTickerDefaults.add(PriorityClass.ABOVE_NORMAL.name());
    equityForwardCurveFromFuturesPerTickerDefaults.addAll(equityForwardCurveFromFuturesDefaults.createPerTickerDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurveFuturePriceImpliedPerTickerDefaults.class.getName(), equityForwardCurveFromFuturesPerTickerDefaults));

  }

  protected void addEquityFuturePricingDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = Arrays.asList(PriorityClass.NORMAL.name(), CalculationPropertyNamesAndValues.MARK_TO_MARKET_METHOD);
    functionConfigs.add(new ParameterizedFunctionConfiguration(FuturesPricingDefaults.class.getName(), defaults));
  }

  protected void addEquityIndexOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final EquityInstrumentDefaultValues.Builder equityDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames()
        .useVolatilitySurfaceNames();
    final List<String> perTickerDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    perTickerDefaults.addAll(equityDefaults.createPerEquityDefaults());
    final List<String> perExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    perExchangeDefaults.addAll(equityDefaults.createPerExchangeDefaults());
    final List<String> perCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    perCurrencyDefaults.addAll(equityDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerTickerDefaults.class.getName(), perTickerDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerExchangeDefaults.class.getName(), perExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerCurrencyDefaults.class.getName(), perCurrencyDefaults));
  }

  protected void addEquityFutureOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = Arrays.asList(PriorityClass.ABOVE_NORMAL.name(), "USD", "BBG", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD, "BBG");
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFutureBlackVolatilitySurfacePerCurrencyDefaults.class.getName(), defaults));
  }

  protected void addEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final EquityInstrumentDefaultValues.Builder equityOptionSurfaceCalculationMethodDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useVolatilitySurfaceCalculationMethodNames();
    final List<String> equityOptionSurfaceCalculationMethodPerEquityDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    equityOptionSurfaceCalculationMethodPerEquityDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerEquityDefaults());
    final List<String> equityOptionSurfaceCalculationMethodPerExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    equityOptionSurfaceCalculationMethodPerExchangeDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerExchangeDefaults());
    final List<String> equityOptionSurfaceCalculationMethodPerCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    equityOptionSurfaceCalculationMethodPerCurrencyDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerEquityDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerEquityDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerExchangeDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerCurrencyDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerCurrencyDefaults));
    final EquityInstrumentDefaultValues.Builder equityOptionBlackSurfaceInterpolationDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .useInterpolationMethodNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames();
    final List<String> equityOptionPerEquityDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    equityOptionPerEquityDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerEquityDefaults());
    final List<String> equityOptionPerExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    equityOptionPerExchangeDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerExchangeDefaults());
    final List<String> equityOptionPerCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    equityOptionPerCurrencyDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerEquityDefaults.class.getName(), equityOptionPerEquityDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerExchangeDefaults.class.getName(), equityOptionPerExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults.class.getName(), equityOptionPerCurrencyDefaults));

    // Defaults added for Listed Equity Options
    // 1. EquityOptionCalculationMethodDefaultFunction added elsewhere in addEquityOptionCalculationMethodDefaults
    // 2. ListedEquityOptionFunction defaults
    // 3. EquityForwardCurveFunction defaults
    final EquityInstrumentDefaultValues.Builder listedEquityOptionMethodDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames();
    final List<String> listedEquityOptionPerTickerDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    listedEquityOptionPerTickerDefaults.addAll(listedEquityOptionMethodDefaults.createPerEquityDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(ListedEquityOptionPerTickerDefaults.class.getName(), listedEquityOptionPerTickerDefaults));
  }

  protected void addEquityFutureOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> surfaceCalculationMethodPerCurrencyDefaults = Arrays.asList(PriorityClass.ABOVE_NORMAL.name(), "USD", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFutureOptionSurfaceCalculationMethodDefaults.class.getName(), surfaceCalculationMethodPerCurrencyDefaults));
    final List<String> surfaceInterpolationDefaults = Arrays.asList(PriorityClass.ABOVE_NORMAL.name(),
        "USD",
        "Discounting",
        "DefaultTwoCurveUSDConfig",
        "BBG",
        BlackVolatilitySurfacePropertyNamesAndValues.SPLINE,
        "BBG",
        ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFutureOptionBlackLognormalDefaults.class.getName(), surfaceInterpolationDefaults));
  }

  protected void addEquityPureVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfacePrimitiveDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfaceSecurityDefaults.class.getName(), defaults));
  }

  protected void addEquityVarianceSwapDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityVarianceSwapStaticReplicationDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    final List<String> equityVarianceSwapStaticReplicationDefaultsWithPriority = new ArrayList<>();
    equityVarianceSwapStaticReplicationDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapStaticReplicationDefaultsWithPriority.addAll(equityVarianceSwapStaticReplicationDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapStaticReplicationDefaults.class.getName(), equityVarianceSwapStaticReplicationDefaultsWithPriority));
    final List<String> equityVarianceSwapDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    final List<String> equityVarianceSwapDefaultsWithPriority = new ArrayList<>();
    equityVarianceSwapDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapDefaultsWithPriority.addAll(equityVarianceSwapDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapDefaults.class.getName(), equityVarianceSwapDefaultsWithPriority));
  }

  protected void addEquityOptionCalculationMethodDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = Arrays.asList(PriorityClass.ABOVE_NORMAL.name(),
        CalculationPropertyNamesAndValues.ROLL_GESKE_WHALEY_LISTED_METHOD, // (American) Equity Options
        CalculationPropertyNamesAndValues.BLACK_LISTED_METHOD, // (European) EquityIndexOptions
        CalculationPropertyNamesAndValues.BJERKSUND_STENSLAND_LISTED_METHOD); // (American) EquityIndexFutureOptions
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionCalculationMethodDefaultFunction.class.getName(), defaults));
  }

  protected void addListedEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ListedEquityOptionPerSecurityTypeDefaults.class, PriorityClass.ABOVE_NORMAL.name(),
        EquityOptionSecurity.SECURITY_TYPE, "OIS", "DefaultTwoCurveUSDConfig", "Forward3M", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD,
        EquityIndexOptionSecurity.SECURITY_TYPE, "OIS", "DefaultTwoCurveUSDConfig", "Forward3M", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD,
        EquityIndexFutureOptionSecurity.SECURITY_TYPE, "OIS", "DefaultTwoCurveUSDConfig", "Forward3M", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD));
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions(), multicurvePricingFunctions());
  }
}
