/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLSettings;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.DummyPortfolioNodeFunction;
import com.opengamma.financial.analytics.PositionScalingFunction;
import com.opengamma.financial.analytics.PositionWeightFromNAVFunction;
import com.opengamma.financial.analytics.SummingFunction;
import com.opengamma.financial.analytics.UnitPositionScalingFunction;
import com.opengamma.financial.analytics.model.bond.BondConvexityFunction;
import com.opengamma.financial.analytics.model.bond.BondCouponPaymentDiaryFunction;
import com.opengamma.financial.analytics.model.bond.BondMacaulayDurationFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketDirtyPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondPV01CountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPV01CurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPreviousCloseDirtyPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondTenorFunction;
import com.opengamma.financial.analytics.model.bond.BondYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadCountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadCurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.NelsonSiegelSvenssonBondCurveFunction;
import com.opengamma.financial.analytics.model.equity.CAPMBetaModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.CAPMBetaModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.CAPMFromRegressionModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.CAPMFromRegressionModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.JensenAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.JensenAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.PortfolioEquityPnLFunction;
import com.opengamma.financial.analytics.model.equity.PositionEquityPnLFunction;
import com.opengamma.financial.analytics.model.equity.SharpeRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.SharpeRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.StandardEquityModelFunction;
import com.opengamma.financial.analytics.model.equity.TotalRiskAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.TotalRiskAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.TradeEquityPnLFunction;
import com.opengamma.financial.analytics.model.equity.TreynorRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.TreynorRatioPositionFunction;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveNodeSensitivityFixedFloatSwapFunction;
import com.opengamma.financial.analytics.model.future.BondFutureImpliedRepoFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesMertonModelFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesModelCostOfCarryFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionValueGreekSensitivityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesFunction;
import com.opengamma.financial.analytics.model.riskfactor.option.OptionGreekToValueGreekConverterFunction;
import com.opengamma.financial.analytics.model.swap.PV01FixedFloatSwapFunction;
import com.opengamma.financial.analytics.model.swap.ParRateFixedFloatSwapFunction;
import com.opengamma.financial.analytics.model.swap.ParRateParallelCurveShiftFixedFloatSwapFunction;
import com.opengamma.financial.analytics.model.swap.PresentValueFixedFloatSwapFunction;
import com.opengamma.financial.analytics.model.var.OptionPortfolioParametricVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.OptionPositionParametricVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.PortfolioHistoricalVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.PositionHistoricalVaRCalculatorFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.analytics.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceFunction;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.DefaultCurrencyInjectionFunction;
import com.opengamma.financial.currency.PortfolioNodeCurrencyConversionFunction;
import com.opengamma.financial.currency.PortfolioNodeDefaultCurrencyFunction;
import com.opengamma.financial.currency.PositionCurrencyConversionFunction;
import com.opengamma.financial.currency.PositionDefaultCurrencyFunction;
import com.opengamma.financial.currency.SecurityCurrencyConversionFunction;
import com.opengamma.financial.currency.SecurityDefaultCurrencyFunction;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class DemoStandardFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  private static final boolean OUTPUT_REPO_CONFIGURATION = false;

  protected static void addScalingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(PositionScalingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(functionConfig);
  }

  protected static void addUnitScalingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(UnitPositionScalingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(functionConfig);
  }

  protected static void addDummyFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(DummyPortfolioNodeFunction.class.getName(), Arrays.asList(requirementName, "0"));
    functionConfigs.add(functionConfig);
  }

  protected static void addSummingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(SummingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(functionConfig);
  }

  protected static void addValueGreekAndSummingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration converterFunctionConfig = new ParameterizedFunctionConfiguration(OptionGreekToValueGreekConverterFunction.class.getName(), Collections
        .singleton(requirementName));
    functionConfigs.add(converterFunctionConfig);
    addSummingFunction(functionConfigs, requirementName);
  }

  protected static void addCurrencyConversionFunctions(List<FunctionConfiguration> functionConfigs, String requirementName) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(PortfolioNodeCurrencyConversionFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionCurrencyConversionFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SecurityCurrencyConversionFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PortfolioNodeDefaultCurrencyFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionDefaultCurrencyFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SecurityDefaultCurrencyFunction.class.getName(), Collections.singleton(requirementName)));
  }

  public static RepositoryConfiguration constructRepositoryConfiguration() {
    List<FunctionConfiguration> functionConfigs = new ArrayList<FunctionConfiguration>();

    functionConfigs.add(new StaticFunctionConfiguration(BondTenorFunction.class.getName()));

    // options
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesMertonModelFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesMertonImpliedVolatilitySurfaceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesModelCostOfCarryFunction.class.getName()));

    // equity and portfolio
    functionConfigs.add(new StaticFunctionConfiguration(PositionEquityPnLFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(PortfolioEquityPnLFunction.class.getName()));

    String dataSource = "BLOOMBERG";
    String dataField = "PX_LAST";
    String returnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT;
    String startDate = "2008-09-22";
    // TODO if this is changed, need to have the ability to change the # of observations in a year in the portfolio analysis calculators (e.g. Sharpe ratio)
    String scheduleName = ScheduleCalculatorFactory.DAILY;
    String samplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    String expectedMarketReturnCalculatorName = StatisticsCalculatorFactory.MEAN;
    String expectedAssetReturnCalculatorName = StatisticsCalculatorFactory.MEAN;
    String expectedRiskFreeReturnCalculatorName = StatisticsCalculatorFactory.MEAN;
    String assetStandardDeviationCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    String marketStandardDeviationCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;

    functionConfigs.add(new ParameterizedFunctionConfiguration(TradeEquityPnLFunction.class.getName(), Arrays.asList("BLOOMBERG", "CMPL", "PX_LAST", "COST_OF_CARRY")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SecurityPriceSeriesFunction.class.getName(), Arrays.asList(dataSource, dataField, startDate, scheduleName, samplingCalculatorName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityPnLFunction.class.getName(), Collections.singleton(returnCalculatorName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionHistoricalVaRCalculatorFunction.class.getName(), Arrays.asList(StatisticsCalculatorFactory.MEAN,
        StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION, "0.99")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PortfolioHistoricalVaRCalculatorFunction.class.getName(), Arrays.asList(StatisticsCalculatorFactory.MEAN,
        StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION, "0.99")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(OptionPositionParametricVaRCalculatorFunction.class.getName(), Arrays.asList(dataSource, startDate, returnCalculatorName, scheduleName,
        samplingCalculatorName, "0.99", "1", ValueRequirementNames.VALUE_DELTA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(OptionPortfolioParametricVaRCalculatorFunction.class.getName(), Arrays.asList(dataSource, startDate, returnCalculatorName, scheduleName,
        samplingCalculatorName, "0.99", "1", ValueRequirementNames.VALUE_DELTA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionValueGreekSensitivityPnLFunction.class.getName(), Arrays.asList(dataSource, startDate, returnCalculatorName, scheduleName,
        samplingCalculatorName, ValueRequirementNames.VALUE_DELTA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CAPMBetaModelPositionFunction.class.getName(), Arrays.asList(returnCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CAPMBetaModelPortfolioNodeFunction.class.getName(), Arrays.asList(returnCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CAPMFromRegressionModelPositionFunction.class.getName(), Collections.singleton(startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CAPMFromRegressionModelPortfolioNodeFunction.class.getName(), Collections.singleton(startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SharpeRatioPositionFunction.class.getName(), Arrays.asList(returnCalculatorName, StatisticsCalculatorFactory.MEAN,
        assetStandardDeviationCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SharpeRatioPortfolioNodeFunction.class.getName(), Arrays.asList(returnCalculatorName, StatisticsCalculatorFactory.MEAN,
        assetStandardDeviationCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(TreynorRatioPositionFunction.class.getName(), Arrays.asList(expectedAssetReturnCalculatorName, expectedRiskFreeReturnCalculatorName,
        startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(TreynorRatioPortfolioNodeFunction.class.getName(), Arrays.asList(expectedAssetReturnCalculatorName,
        expectedRiskFreeReturnCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(JensenAlphaPositionFunction.class.getName(), Arrays.asList(returnCalculatorName, expectedAssetReturnCalculatorName,
        expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(JensenAlphaPortfolioNodeFunction.class.getName(), Arrays.asList(returnCalculatorName, expectedAssetReturnCalculatorName,
        expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(TotalRiskAlphaPositionFunction.class.getName(), Arrays.asList(returnCalculatorName, expectedAssetReturnCalculatorName,
        expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, assetStandardDeviationCalculatorName, marketStandardDeviationCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class.getName(), Arrays.asList(returnCalculatorName, expectedAssetReturnCalculatorName,
        expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, assetStandardDeviationCalculatorName, marketStandardDeviationCalculatorName, startDate)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionWeightFromNAVFunction.class.getName(), Collections.singleton("56000000")));

    functionConfigs.add(new StaticFunctionConfiguration(PV01FixedFloatSwapFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(PresentValueFixedFloatSwapFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ParRateFixedFloatSwapFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ParRateParallelCurveShiftFixedFloatSwapFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(YieldCurveNodeSensitivityFixedFloatSwapFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(StandardEquityModelFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketCleanPriceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondPreviousCloseDirtyPriceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketDirtyPriceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondYieldFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketYieldFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondZSpreadCurrencyCurveFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondZSpreadCountryCurveFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondConvexityFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMacaulayDurationFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondCouponPaymentDiaryFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondPresentValueCountryCurveFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondPresentValueCurrencyCurveFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondPV01CountryCurveFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondPV01CurrencyCurveFunction.class.getName()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(NelsonSiegelSvenssonBondCurveFunction.class.getName(), Arrays.asList("USD")));
    functionConfigs.add(new StaticFunctionConfiguration(BondFutureImpliedRepoFunction.class.getName()));

    //functionConfigs.add(new StaticFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName())); // TODO: haven't been brave enough for this one yet

    addDummyFunction(functionConfigs, ValueRequirementNames.PAR_RATE);
    addDummyFunction(functionConfigs, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);

    addScalingFunction(functionConfigs, ValueRequirementNames.FAIR_VALUE);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DELTA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.STRIKE_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DRIFTLESS_THETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.STRIKE_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_P_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.THETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.RHO);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CARRY_RHO);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZETA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DZETA_DVOL);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ELASTICITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PHI);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZOMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ULTIMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_ULTIMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SPEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SPEED_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DVANNA_DVOL);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VOMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VOMMA);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.BOND_TENOR);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CLEAN_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.Z_SPREAD);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CONVEXITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MACAULAY_DURATION);

    addScalingFunction(functionConfigs, ValueRequirementNames.PV01);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addScalingFunction(functionConfigs, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);

    addSummingFunction(functionConfigs, ValueRequirementNames.FAIR_VALUE);
    addSummingFunction(functionConfigs, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);

    addDummyFunction(functionConfigs, ValueRequirementNames.DELTA);
    addDummyFunction(functionConfigs, ValueRequirementNames.DELTA_BLEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.STRIKE_DELTA);
    addDummyFunction(functionConfigs, ValueRequirementNames.DRIFTLESS_THETA);
    addDummyFunction(functionConfigs, ValueRequirementNames.GAMMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.GAMMA_P);
    addDummyFunction(functionConfigs, ValueRequirementNames.STRIKE_GAMMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.GAMMA_BLEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.GAMMA_P_BLEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.VEGA);
    addDummyFunction(functionConfigs, ValueRequirementNames.VEGA_P);
    addDummyFunction(functionConfigs, ValueRequirementNames.VARIANCE_VEGA);
    addDummyFunction(functionConfigs, ValueRequirementNames.VEGA_BLEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.THETA);
    addDummyFunction(functionConfigs, ValueRequirementNames.RHO);
    addDummyFunction(functionConfigs, ValueRequirementNames.CARRY_RHO);
    addDummyFunction(functionConfigs, ValueRequirementNames.ZETA);
    addDummyFunction(functionConfigs, ValueRequirementNames.ZETA_BLEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.DZETA_DVOL);
    addDummyFunction(functionConfigs, ValueRequirementNames.ELASTICITY);
    addDummyFunction(functionConfigs, ValueRequirementNames.PHI);
    addDummyFunction(functionConfigs, ValueRequirementNames.ZOMMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.ZOMMA_P);
    addDummyFunction(functionConfigs, ValueRequirementNames.ULTIMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.VARIANCE_ULTIMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.SPEED);
    addDummyFunction(functionConfigs, ValueRequirementNames.SPEED_P);
    addDummyFunction(functionConfigs, ValueRequirementNames.VANNA);
    addDummyFunction(functionConfigs, ValueRequirementNames.VARIANCE_VANNA);
    addDummyFunction(functionConfigs, ValueRequirementNames.DVANNA_DVOL);
    addDummyFunction(functionConfigs, ValueRequirementNames.VOMMA);
    addDummyFunction(functionConfigs, ValueRequirementNames.VOMMA_P);
    addDummyFunction(functionConfigs, ValueRequirementNames.VARIANCE_VOMMA);

    addDummyFunction(functionConfigs, ValueRequirementNames.BOND_TENOR);

    addSummingFunction(functionConfigs, ValueRequirementNames.PV01);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);

    addSummingFunction(functionConfigs, ValueRequirementNames.PRICE_SERIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.PNL_SERIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.WEIGHT);

    addDummyFunction(functionConfigs, ValueRequirementNames.CLEAN_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.DIRTY_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.YTM);
    addDummyFunction(functionConfigs, ValueRequirementNames.MARKET_YTM);
    addDummyFunction(functionConfigs, ValueRequirementNames.Z_SPREAD);
    addDummyFunction(functionConfigs, ValueRequirementNames.IMPLIED_REPO);
    addDummyFunction(functionConfigs, ValueRequirementNames.CONVEXITY);
    addDummyFunction(functionConfigs, ValueRequirementNames.MACAULAY_DURATION);
    addSummingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);
    addScalingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);

    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_SPEED);

    // Currency conversion
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.FAIR_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PV01);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_SPEED);

    functionConfigs.add(new ParameterizedFunctionConfiguration(CurrencyMatrixSourcingFunction.class.getName(), Collections.singleton("BloombergLiveData")));
    functionConfigs.add(new StaticFunctionConfiguration(DefaultCurrencyInjectionFunction.class.getName()));
    // functionConfigs.add(new StaticFunctionConfiguration(CurrencyInversionFunction.class.getName()));
    // functionConfigs.add(new ParameterizedFunctionConfiguration(CurrencyCrossRateFunction.class.getName(), Collections.singleton("USD")));
    // functionConfigs.add(new StaticFunctionConfiguration(BloombergCurrencyRateFunction.class.getName()));

    RepositoryConfiguration repoConfig = new RepositoryConfiguration(functionConfigs);

    if (OUTPUT_REPO_CONFIGURATION) {
      FudgeMsg msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(repoConfig).getMessage();
      FudgeMsgFormatter.outputToSystemOut(msg);
      try {
        FudgeXMLSettings xmlSettings = new FudgeXMLSettings();
        xmlSettings.setEnvelopeElementName(null);
        FudgeMsgWriter msgWriter = new FudgeMsgWriter(new FudgeXMLStreamWriter(FudgeContext.GLOBAL_DEFAULT, new OutputStreamWriter(System.out), xmlSettings));
        msgWriter.setDefaultMessageProcessingDirectives(0);
        msgWriter.setDefaultMessageVersion(0);
        msgWriter.setDefaultTaxonomyId(0);
        msgWriter.writeMessage(msg);
        msgWriter.flush();
      } catch (Exception e) {
        // Just swallow it.
      }
    }
    return repoConfig;
  }

  public static RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new RepositoryConfigurationSource() {
      private final RepositoryConfiguration _config = constructRepositoryConfiguration();

      @Override
      public RepositoryConfiguration getRepositoryConfiguration() {
        return _config;
      }
    };
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
