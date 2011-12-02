/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import static com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields.LAST_PRICE;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

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
import com.opengamma.financial.aggregation.BottomPositionValues;
import com.opengamma.financial.aggregation.SortedPositionValues;
import com.opengamma.financial.aggregation.TopPositionValues;
import com.opengamma.financial.analytics.DummyLabelledMatrix2DPortfolioNodeFunction;
import com.opengamma.financial.analytics.DummyLabelledMatrix2DPositionFunction;
import com.opengamma.financial.analytics.DummyPortfolioNodeFunction;
import com.opengamma.financial.analytics.DummyPortfolioNodeMultipleCurrencyAmountFunction;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.analytics.LastHistoricalValueFunction;
import com.opengamma.financial.analytics.PositionScalingFunction;
import com.opengamma.financial.analytics.PositionTradeScalingFunction;
import com.opengamma.financial.analytics.PositionWeightFromNAVFunction;
import com.opengamma.financial.analytics.SummingFunction;
import com.opengamma.financial.analytics.UnitPositionScalingFunction;
import com.opengamma.financial.analytics.UnitPositionTradeScalingFunction;
import com.opengamma.financial.analytics.equity.SecurityMarketPriceFunction;
import com.opengamma.financial.analytics.ircurve.DefaultYieldCurveMarketDataShiftFunction;
import com.opengamma.financial.analytics.ircurve.DefaultYieldCurveShiftFunction;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveMarketDataShiftFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveShiftFunction;
import com.opengamma.financial.analytics.model.bond.BondCleanPriceFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondCleanPriceFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondCouponPaymentDiaryFunction;
import com.opengamma.financial.analytics.model.bond.BondDirtyPriceFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondDirtyPriceFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondMacaulayDurationFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondMacaulayDurationFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketDirtyPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondModifiedDurationFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondModifiedDurationFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondTenorFunction;
import com.opengamma.financial.analytics.model.bond.BondYieldFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.NelsonSiegelSvenssonBondCurveFunction;
import com.opengamma.financial.analytics.model.equity.futures.EquityFuturesFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.StandardEquityModelFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPositionFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPV01Function;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateParallelCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPresentValueFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.forex.ForexForwardCurrencyExposureFunction;
import com.opengamma.financial.analytics.model.forex.ForexForwardPresentValueCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.forex.ForexForwardPresentValueFunction;
import com.opengamma.financial.analytics.model.forex.ForexForwardYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionCurrencyExposureFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionPresentValueCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionPresentValueFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionVegaFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionVegaQuoteFunction;
import com.opengamma.financial.analytics.model.forex.ForexSingleBarrierOptionYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionCurrencyExposureFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionPresentValueCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionPresentValueFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionPresentValueVolatilitySensitivityFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionVegaFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionVegaQuoteFunction;
import com.opengamma.financial.analytics.model.forex.ForexVanillaOptionYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.future.BondFutureGrossBasisFromCurvesFunction;
import com.opengamma.financial.analytics.model.future.BondFutureNetBasisFromCurvesFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionHestonPresentValueFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionPresentValueFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionSABRSensitivitiesFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionVegaFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesMertonModelFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesModelCostOfCarryFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PortfolioExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PortfolioExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionValueGreekSensitivityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.riskfactor.option.OptionGreekToValueGreekConverterFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionSABRPresentValueCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionSABRPresentValueFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionSABRPresentValueSABRFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionSABRVegaFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionSABRYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.var.OptionPortfolioParametricVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.OptionPositionParametricVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.PortfolioHistoricalVaRCalculatorFunction;
import com.opengamma.financial.analytics.model.var.PositionHistoricalVaRCalculatorFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.analytics.volatility.cube.fitting.SABRNonLinearLeastSquaresSwaptionCubeFittingFunction;
import com.opengamma.financial.analytics.volatility.surface.DefaultVolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.fitting.BlackScholesMertonImpliedVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.volatility.surface.fitting.HestonFourierIRFutureSurfaceFittingFunction;
import com.opengamma.financial.analytics.volatility.surface.fitting.SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.DefaultCurrencyInjectionFunction;
import com.opengamma.financial.currency.PortfolioNodeCurrencyConversionFunction;
import com.opengamma.financial.currency.PortfolioNodeDefaultCurrencyFunction;
import com.opengamma.financial.currency.PositionCurrencyConversionFunction;
import com.opengamma.financial.currency.PositionDefaultCurrencyFunction;
import com.opengamma.financial.currency.SecurityCurrencyConversionFunction;
import com.opengamma.financial.currency.SecurityDefaultCurrencyFunction;
import com.opengamma.financial.equity.future.pricing.EquityFuturePricerFactory;
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
    ParameterizedFunctionConfiguration securityScalingFunctionConfig = new ParameterizedFunctionConfiguration(PositionScalingFunction.class.getName(), Collections.singleton(requirementName));
    ParameterizedFunctionConfiguration tradeScalingFunctionConfig = new ParameterizedFunctionConfiguration(PositionTradeScalingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(securityScalingFunctionConfig);
    functionConfigs.add(tradeScalingFunctionConfig);
  }

  protected static void addUnitScalingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration securityScalingFunctionConfig = new ParameterizedFunctionConfiguration(UnitPositionScalingFunction.class.getName(), Collections.singleton(requirementName));
    ParameterizedFunctionConfiguration tradeScalingFunctionConfig = new ParameterizedFunctionConfiguration(UnitPositionTradeScalingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(securityScalingFunctionConfig);
    functionConfigs.add(tradeScalingFunctionConfig);
  }

  protected static void addDummyFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(DummyPortfolioNodeFunction.class.getName(), Arrays.asList(requirementName, "0"));
    functionConfigs.add(functionConfig);
  }

  protected static void addDummyMultipleCurrencyAmountFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(DummyPortfolioNodeMultipleCurrencyAmountFunction.class.getName(), Arrays.asList(requirementName));
    functionConfigs.add(functionConfig);
  }

  protected static void addSummingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    ParameterizedFunctionConfiguration functionConfig = new ParameterizedFunctionConfiguration(SummingFunction.class.getName(), Collections.singleton(requirementName));
    functionConfigs.add(functionConfig);
  }

  protected static void addFilteredSummingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Collections.singleton(requirementName)));
  }

  protected static void addValueGreekAndSummingFunction(List<FunctionConfiguration> functionConfigs, String requirementName) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(OptionGreekToValueGreekConverterFunction.class.getName(), Collections.singleton(requirementName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Collections.singleton(requirementName)));
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

    functionConfigs.add(new StaticFunctionConfiguration(SecurityMarketPriceFunction.class.getName()));
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SECURITY_IMPLIED_VOLATLITY);
    
    functionConfigs.add(new StaticFunctionConfiguration(BondTenorFunction.class.getName()));

    // options
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesMertonModelFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesMertonImpliedVolatilitySurfaceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackScholesModelCostOfCarryFunction.class.getName()));

    // equity and portfolio
    functionConfigs.add(new StaticFunctionConfiguration(PositionExchangeTradedPnLFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(PortfolioExchangeTradedPnLFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(PortfolioExchangeTradedDailyPnLFunction.class.getName()));;
    

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

    functionConfigs.add(new ParameterizedFunctionConfiguration(TradeExchangeTradedPnLFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, LAST_PRICE, "COST_OF_CARRY")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(TradeExchangeTradedDailyPnLFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, LAST_PRICE, "COST_OF_CARRY")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionExchangeTradedDailyPnLFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, LAST_PRICE, "COST_OF_CARRY")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SecurityPriceSeriesFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, LAST_PRICE, startDate, scheduleName,
        samplingCalculatorName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityPnLFunction.class.getName(), Collections.singleton(returnCalculatorName)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionHistoricalVaRCalculatorFunction.class.getName(), Arrays.asList(StatisticsCalculatorFactory.MEAN,
        StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION, "0.99")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PortfolioHistoricalVaRCalculatorFunction.class.getName(), Arrays.asList(StatisticsCalculatorFactory.MEAN,
        StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION, "0.99")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(OptionPositionParametricVaRCalculatorFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, startDate, returnCalculatorName,
        scheduleName, samplingCalculatorName, "0.99", "1", ValueRequirementNames.VALUE_DELTA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(OptionPortfolioParametricVaRCalculatorFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, startDate, returnCalculatorName,
        scheduleName, samplingCalculatorName, "0.99", "1", ValueRequirementNames.VALUE_DELTA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PositionValueGreekSensitivityPnLFunction.class.getName(), Arrays.asList(DEFAULT_CONFIG_NAME, startDate, returnCalculatorName,
        scheduleName, samplingCalculatorName, ValueRequirementNames.VALUE_DELTA)));
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

    addFixedIncomeInstrumentCalculators(functionConfigs);

    // Something to return a LabelledMatrix2D
    functionConfigs.add(new StaticFunctionConfiguration(DummyLabelledMatrix2DPositionFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(DummyLabelledMatrix2DPortfolioNodeFunction.class.getName()));

    functionConfigs.add(new StaticFunctionConfiguration(StandardEquityModelFunction.class.getName()));
    addBondCalculators(functionConfigs);
    addBondFutureCalculators(functionConfigs);
    functionConfigs.add(new ParameterizedFunctionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG")));
    addSwaptionCalculators(functionConfigs);
    addForexVanillaOptionCalculators(functionConfigs);
    addForexSingleBarrierOptionCalculators(functionConfigs);
    addForexForwardCalculators(functionConfigs);
    addInterestRateFutureOptionCalculators(functionConfigs);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.MARK_TO_MARKET)));
    //functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.COST_OF_CARRY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.DIVIDEND_YIELD)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PV01, EquityFuturePricerFactory.MARK_TO_MARKET)));
    //functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PV01, EquityFuturePricerFactory.COST_OF_CARRY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.PV01, EquityFuturePricerFactory.DIVIDEND_YIELD)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_RHO, EquityFuturePricerFactory.MARK_TO_MARKET)));
    //functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_RHO, EquityFuturePricerFactory.COST_OF_CARRY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_RHO, EquityFuturePricerFactory.DIVIDEND_YIELD)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_DELTA, EquityFuturePricerFactory.MARK_TO_MARKET)));
    //functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_DELTA, EquityFuturePricerFactory.COST_OF_CARRY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(), Arrays.asList(ValueRequirementNames.VALUE_DELTA, EquityFuturePricerFactory.DIVIDEND_YIELD)));

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
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_CLEAN_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CLEAN_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MODIFIED_DURATION);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.Z_SPREAD);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CONVEXITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MACAULAY_DURATION);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GROSS_BASIS);
    addSummingFunction(functionConfigs, ValueRequirementNames.GROSS_BASIS);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.NET_BASIS);
    addSummingFunction(functionConfigs, ValueRequirementNames.NET_BASIS);

    addScalingFunction(functionConfigs, ValueRequirementNames.PV01);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PAR_RATE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);

    addSummingFunction(functionConfigs, ValueRequirementNames.FAIR_VALUE);

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

    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.PV01);
    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.FX_PRESENT_VALUE);
    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.VEGA_MATRIX);
    addFilteredSummingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRICE_SERIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.PNL_SERIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.WEIGHT);

    addDummyFunction(functionConfigs, ValueRequirementNames.MARKET_CLEAN_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.MARKET_YTM);
    addDummyFunction(functionConfigs, ValueRequirementNames.CLEAN_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.YTM);
    addDummyFunction(functionConfigs, ValueRequirementNames.DIRTY_PRICE);
    addDummyFunction(functionConfigs, ValueRequirementNames.MODIFIED_DURATION);
    addDummyFunction(functionConfigs, ValueRequirementNames.Z_SPREAD);
    addDummyFunction(functionConfigs, ValueRequirementNames.IMPLIED_REPO);
    addDummyFunction(functionConfigs, ValueRequirementNames.CONVEXITY);
    addDummyFunction(functionConfigs, ValueRequirementNames.MACAULAY_DURATION);
    addSummingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);
    addScalingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);

    addScalingFunction(functionConfigs, ValueRequirementNames.FX_PRESENT_VALUE);
    addScalingFunction(functionConfigs, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addDummyFunction(functionConfigs, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FX_CURVE_SENSITIVITIES);

    addScalingFunction(functionConfigs, ValueRequirementNames.VEGA_MATRIX);
    addSummingFunction(functionConfigs, ValueRequirementNames.VEGA_MATRIX);
    addScalingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addSummingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addScalingFunction(functionConfigs, ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES);

    addScalingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addSummingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addScalingFunction(functionConfigs, ValueRequirementNames.VALUE_RHO);
    addSummingFunction(functionConfigs, ValueRequirementNames.VALUE_RHO);
    

    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_SPEED);

    // Currency conversion
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.FAIR_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PV01);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.DAILY_PNL);
    //TODO PRESENT_VALUE_CURVE_SENSITIVITY
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_SPEED);
    functionConfigs.add(new ParameterizedFunctionConfiguration(SecurityCurrencyConversionFunction.class.getName(), Arrays.asList(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PortfolioNodeDefaultCurrencyFunction.class.getName(), Arrays.asList(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)));

    functionConfigs.add(new ParameterizedFunctionConfiguration(CurrencyMatrixSourcingFunction.class.getName(), Collections.singleton(CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CurrencyMatrixSourcingFunction.class.getName(), Collections.singleton(CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA)));
    functionConfigs.add(new StaticFunctionConfiguration(DefaultCurrencyInjectionFunction.class.getName()));
    // functionConfigs.add(new StaticFunctionConfiguration(CurrencyInversionFunction.class.getName()));
    // functionConfigs.add(new ParameterizedFunctionConfiguration(CurrencyCrossRateFunction.class.getName(), Collections.singleton("USD")));
    // functionConfigs.add(new StaticFunctionConfiguration(BloombergCurrencyRateFunction.class.getName()));

    functionConfigs.add(new StaticFunctionConfiguration(BottomPositionValues.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(SortedPositionValues.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(TopPositionValues.class.getName()));
    
    functionConfigs.add(new StaticFunctionConfiguration(VolatilitySurfaceShiftFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(DefaultVolatilitySurfaceShiftFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(YieldCurveShiftFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(DefaultYieldCurveShiftFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(YieldCurveMarketDataShiftFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(DefaultYieldCurveMarketDataShiftFunction.class.getName()));
    
    // Historical data functions.
    addDummyFunction(functionConfigs, ValueRequirementNames.DAILY_VOLUME);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DAILY_VOLUME);
    functionConfigs.add(new ParameterizedFunctionConfiguration(LastHistoricalValueFunction.class.getName(), Collections.singleton(ValueRequirementNames.DAILY_VOLUME)));
    addDummyFunction(functionConfigs, ValueRequirementNames.DAILY_MARKET_CAP);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DAILY_MARKET_CAP);
    functionConfigs.add(new ParameterizedFunctionConfiguration(LastHistoricalValueFunction.class.getName(), Collections.singleton(ValueRequirementNames.DAILY_MARKET_CAP)));
    addDummyFunction(functionConfigs, ValueRequirementNames.DAILY_APPLIED_BETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DAILY_APPLIED_BETA);
    functionConfigs.add(new ParameterizedFunctionConfiguration(LastHistoricalValueFunction.class.getName(), Collections.singleton(ValueRequirementNames.DAILY_APPLIED_BETA)));
    addDummyFunction(functionConfigs, ValueRequirementNames.DAILY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DAILY_PRICE);
    functionConfigs.add(new ParameterizedFunctionConfiguration(LastHistoricalValueFunction.class.getName(), Collections.singleton(ValueRequirementNames.DAILY_PRICE)));
    
    
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

  private static void addBondFutureCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondFutureGrossBasisFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondFutureNetBasisFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
  }

  private static void addBondCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new StaticFunctionConfiguration(BondCouponPaymentDiaryFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondTenorFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketCleanPriceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketDirtyPriceFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BondMarketYieldFunction.class.getName()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondYieldFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondCleanPriceFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondDirtyPriceFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondMacaulayDurationFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondModifiedDurationFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondCleanPriceFromYieldFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondDirtyPriceFromYieldFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondMacaulayDurationFromYieldFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondModifiedDurationFromYieldFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BondZSpreadFromCurvesFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(NelsonSiegelSvenssonBondCurveFunction.class.getName(), Arrays.asList("USD")));
  }

  private static void addForexSingleBarrierOptionCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexSingleBarrierOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
  }

  private static void addForexVanillaOptionCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionCurrencyExposureFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueCurveSensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionPresentValueVolatilitySensitivityFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionYieldCurveNodeSensitivitiesFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexVanillaOptionVegaQuoteFunction.class.getName(), 
        Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M", "DEFAULT")));
  }

  private static void addForexForwardCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardCurrencyExposureFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardCurrencyExposureFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardCurrencyExposureFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardCurrencyExposureFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueCurveSensitivityFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueCurveSensitivityFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueCurveSensitivityFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardPresentValueCurveSensitivityFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_3M", "FUNDING", "FORWARD_6M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_3M")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexForwardYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FUNDING", "FORWARD_6M", "FUNDING", "FORWARD_6M")));
  }

  private static void addInterestRateFutureOptionCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionPresentValueFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionPresentValueFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionVegaFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionVegaFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SABRNonLinearLeastSquaresIRFutureSurfaceFittingFunction.class.getName(), Arrays.asList("USD", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(HestonFourierIRFutureSurfaceFittingFunction.class.getName(), Arrays.asList("USD", "DEFAULT")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateFutureOptionHestonPresentValueFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING", "DEFAULT")));
  }

  private static void addSwaptionCalculators(List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(SwaptionSABRPresentValueFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG", "true", "FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SwaptionSABRPresentValueCurveSensitivityFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG", "true", "FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SwaptionSABRPresentValueSABRFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG", "FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SwaptionSABRYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG", "true", "FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(SwaptionSABRVegaFunction.class.getName(), Arrays.asList("USD", "BLOOMBERG", "true", "FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Arrays.asList(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY)));
  }

  private static void addFixedIncomeInstrumentCalculators(List<FunctionConfiguration> functionConfigs) {
    //forward/funding
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentParRateFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentPresentValueFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentPV01Function.class.getName(), Arrays.asList("FORWARD_3M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays
        .asList("FORWARD_3M", "FUNDING", MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays
        .asList("FORWARD_3M", "FUNDING", MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentParRateFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentPresentValueFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentPV01Function.class.getName(), Arrays.asList("FORWARD_6M", "FUNDING")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays
        .asList("FORWARD_6M", "FUNDING", MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays
        .asList("FORWARD_6M", "FUNDING", MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FilteringSummingFunction.class.getName(), Arrays.asList(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)));
    
    // Secondary
    functionConfigs.add(new ParameterizedFunctionConfiguration(InterestRateInstrumentPresentValueFunction.class.getName(), Arrays.asList("SECONDARY", "SECONDARY")));
    functionConfigs.add(new ParameterizedFunctionConfiguration(
        InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class.getName(), Arrays.asList("SECONDARY", "SECONDARY", MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING)));
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
