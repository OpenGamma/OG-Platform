/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.cashflow.CashFlowFunctions;
import com.opengamma.financial.analytics.covariance.CovarianceFunctions;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveFunctions;
import com.opengamma.financial.analytics.ircurve.IRCurveFunctions;
import com.opengamma.financial.analytics.model.MarketQuotePositionFunction;
import com.opengamma.financial.analytics.model.ModelFunctions;
import com.opengamma.financial.analytics.model.riskfactor.option.OptionGreekToValueGreekConverterFunction;
import com.opengamma.financial.analytics.timeseries.TimeSeriesFunctions;
import com.opengamma.financial.analytics.volatility.VolatilityFunctions;
import com.opengamma.financial.security.function.DefaultSecurityAttributeFunction;
import com.opengamma.financial.security.function.SecurityFunctions;
import com.opengamma.financial.security.lookup.SecurityAttribute;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class AnalyticsFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new AnalyticsFunctions().getObjectCreating();
  }

  /**
   * Adds an aggregation function for the given requirement name that produces the sum of the child position values.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(FilteringSummingFunction.class, requirementName));
    functions.add(functionConfiguration(SummingFunction.class, requirementName));
  }

  public static void addValueGreekAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(OptionGreekToValueGreekConverterFunction.class, requirementName));
    addScalingAndSummingFunction(functions, requirementName);
  }

  /**
   * Adds a unit scaling function to deliver the value from position's underlying security or trade at the position level. This is normally used for positions in OTC instruments that are stored with a
   * quantity of 1 in OpenGamma.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addUnitScalingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(UnitPositionOrTradeScalingFunction.class, requirementName));
    functions.add(functionConfiguration(PositionTradeScalingFunction.class, requirementName));

  }

  public static void addUnitScalingAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addUnitScalingFunction(functions, requirementName);
    addSummingFunction(functions, requirementName);
  }

  /**
   * Adds a scaling function to deliver the value from a position's underlying security or trade multiplied by the quantity at the position level. This is used for positions in exchange traded
   * instruments.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addScalingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(PositionOrTradeScalingFunction.class, requirementName));
    functions.add(functionConfiguration(PositionTradeScalingFunction.class, requirementName));
  }

  public static void addScalingAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addScalingFunction(functions, requirementName);
    addSummingFunction(functions, requirementName);
  }

  public static void addLastHistoricalValueFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addUnitScalingFunction(functions, requirementName);
    functions.add(functionConfiguration(LastHistoricalValueFunction.class, requirementName));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(AttributesFunction.class));
    functions.add(functionConfiguration(ExternalIdFunction.class));
    functions.add(functionConfiguration(CurrencyPairsFunction.class));
    functions.add(functionConfiguration(DV01Function.class));
    functions.add(functionConfiguration(NotionalFunction.class));
    functions.add(functionConfiguration(PortfolioNodeWeightFunction.class));
    functions.add(functionConfiguration(PositionWeightFunction.class));
    functions.add(functionConfiguration(BucketedPV01Function.class));

    //security attribute functions
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.DIRECTION.name(), ValueRequirementNames.PAY_REC));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.FLOAT_FREQUENCY.name(), ValueRequirementNames.FLOAT_FREQUENCY));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.FREQUENCY.name(), ValueRequirementNames.FREQUENCY));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.INDEX.name(), ValueRequirementNames.INDEX));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.MATURITY.name(), ValueRequirementNames.MATURITY));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.PRODUCT.name(), ValueRequirementNames.PRODUCT));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.QUANTITY.name(), ValueRequirementNames.QUANTITY));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.RATE.name(), ValueRequirementNames.RATE));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.START.name(), ValueRequirementNames.START));
    functions.add(functionConfiguration(DefaultSecurityAttributeFunction.class, SecurityAttribute.TYPE.name(), ValueRequirementNames.TYPE));

    addScalingAndSummingFunction(functions, ValueRequirementNames.ACCRUED_INTEREST);
    addUnitScalingFunction(functions, ValueRequirementNames.ATTRIBUTES);
    addUnitScalingFunction(functions, ValueRequirementNames.EXTERNAL_ID);
    addUnitScalingFunction(functions, ValueRequirementNames.BLACK_VOLATILITY_GRID_PRICE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);
    addUnitScalingFunction(functions, ValueRequirementNames.BOND_TENOR);
    addUnitScalingFunction(functions, ValueRequirementNames.CARRY_RHO);
    addSummingFunction(functions, ValueRequirementNames.CREDIT_SENSITIVITIES);
    addUnitScalingFunction(functions, ValueRequirementNames.CLEAN_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.CONVEXITY);
    addUnitScalingFunction(functions, ValueRequirementNames.CONVEXITY_ADJUSTMENT);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_APPLIED_BETA);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_MARKET_CAP);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_PRICE);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_VOLUME);
    addUnitScalingFunction(functions, ValueRequirementNames.DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.DELTA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.DIRTY_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.DRIFTLESS_THETA);
    addUnitScalingFunction(functions, ValueRequirementNames.DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.DUAL_GAMMA);
    addSummingFunction(functions, ValueRequirementNames.DV01);
    addUnitScalingFunction(functions, ValueRequirementNames.DVANNA_DVOL);
    addUnitScalingFunction(functions, ValueRequirementNames.DZETA_DVOL);
    addUnitScalingFunction(functions, ValueRequirementNames.ELASTICITY);
    addSummingFunction(functions, ValueRequirementNames.EXTERNAL_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FAIR_VALUE);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FIXED_PAY_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FLOATING_PAY_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VOMMA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addUnitScalingFunction(functions, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FX_PRESENT_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_DRIFTLESS_THETA);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_P);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_P_BLEED);
    addScalingAndSummingFunction(functions, ValueRequirementNames.GAMMA_PV01);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_DUAL_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_PRESENT_VALUE);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.GROSS_BASIS);
    addUnitScalingFunction(functions, ValueRequirementNames.IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DUAL_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_FOREX_PV_QUOTES);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_FULL_PDE_GRID);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GRID_IMPLIED_VOL);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GRID_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_PDE_BUCKETED_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_PDE_GREEKS);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.MACAULAY_DURATION);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_CLEAN_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addSummingFunction(functions, ValueRequirementNames.MTM_PNL);
    addUnitScalingFunction(functions, ValueRequirementNames.MTM_PNL);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_YTM);
    addUnitScalingFunction(functions, ValueRequirementNames.MODIFIED_DURATION);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.NET_BASIS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.NETTED_FIXED_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.NOTIONAL);
    addUnitScalingFunction(functions, ValueRequirementNames.PHI);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_SPREAD);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PAY_LEG_PRESENT_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PNL);
    addSummingFunction(functions, ValueRequirementNames.PNL_SERIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_DELTA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_GAMMA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_RHO);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_THETA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.POSITION_WEIGHTED_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY);
    addSummingFunction(functions, ValueRequirementNames.PRICE_SERIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PV01);
    addUnitScalingFunction(functions, ValueRequirementNames.QUANTITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.RECEIVE_LEG_PRESENT_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.RHO);
    addUnitScalingFunction(functions, ValueRequirementNames.SWAP_PAY_LEG_DETAILS);
    addUnitScalingFunction(functions, ValueRequirementNames.SWAP_RECEIVE_LEG_DETAILS);
    addUnitScalingFunction(functions, ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.SECURITY_MODEL_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.MARK_CURRENT);
    addUnitScalingFunction(functions, ValueRequirementNames.SPEED);
    addUnitScalingFunction(functions, ValueRequirementNames.SPEED_P);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT_FX_PERCENTAGE_CHANGE);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT_RATE_FOR_SECURITY);
    addUnitScalingFunction(functions, ValueRequirementNames.STRIKE_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.STRIKE_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.THETA);
    addUnitScalingFunction(functions, ValueRequirementNames.ULTIMA);
    addSummingFunction(functions, ValueRequirementNames.VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_CARRY_RHO);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_DELTA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_DUAL_DELTA);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_GAMMA);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_GAMMA_P);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_PHI);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_RHO);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_SPEED);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_THETA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_VANNA);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_ULTIMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA_BLEED);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_MATRIX);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA_P);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_QUOTE_CUBE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addUnitScalingFunction(functions, ValueRequirementNames.VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VOMMA_P);
    addUnitScalingFunction(functions, ValueRequirementNames.WEIGHTED_VEGA);
    addSummingFunction(functions, ValueRequirementNames.WEIGHTED_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES);
    addUnitScalingFunction(functions, ValueRequirementNames.YTM);
    addUnitScalingFunction(functions, ValueRequirementNames.ZETA);
    addUnitScalingFunction(functions, ValueRequirementNames.ZETA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.Z_SPREAD);
    addUnitScalingFunction(functions, ValueRequirementNames.ZOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.ZOMMA_P);
    addUnitScalingFunction(functions, ValueRequirementNames.BARRIER_DISTANCE);
    addUnitScalingFunction(functions, ValueRequirementNames.CS01);
    addSummingFunction(functions, ValueRequirementNames.CS01);
    addSummingFunction(functions, ValueRequirementNames.BUCKETED_CS01);
    addSummingFunction(functions, ValueRequirementNames.GAMMA_CS01);
    addSummingFunction(functions, ValueRequirementNames.BUCKETED_GAMMA_CS01);
    addSummingFunction(functions, ValueRequirementNames.RR01);
    addSummingFunction(functions, ValueRequirementNames.IR01);
    addSummingFunction(functions, ValueRequirementNames.BUCKETED_IR01);
    addSummingFunction(functions, ValueRequirementNames.NET_MARKET_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.DV01);
    addUnitScalingFunction(functions, ValueRequirementNames.CS01);
    addUnitScalingFunction(functions, ValueRequirementNames.BUCKETED_CS01);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_CS01);
    addUnitScalingFunction(functions, ValueRequirementNames.BUCKETED_GAMMA_CS01);
    addUnitScalingFunction(functions, ValueRequirementNames.RR01);
    addUnitScalingFunction(functions, ValueRequirementNames.IR01);
    addUnitScalingFunction(functions, ValueRequirementNames.BUCKETED_IR01);
    addUnitScalingFunction(functions, ValueRequirementNames.JUMP_TO_DEFAULT);
    addUnitScalingFunction(functions, ValueRequirementNames.HAZARD_RATE_CURVE);
    addUnitScalingFunction(functions, ValueRequirementNames.NET_MARKET_VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.MONETIZED_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.CLEAN_PRESENT_VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.DIRTY_PRESENT_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.ACCRUED_DAYS);
    addUnitScalingFunction(functions, ValueRequirementNames.ACCRUED_PREMIUM);
    addUnitScalingFunction(functions, ValueRequirementNames.PRINCIPAL);
    addUnitScalingFunction(functions, ValueRequirementNames.POINTS_UPFRONT);
    addUnitScalingFunction(functions, ValueRequirementNames.UPFRONT_AMOUNT);
    addUnitScalingFunction(functions, ValueRequirementNames.QUOTED_SPREAD);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.HEDGE_NOTIONAL);

    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.BUCKETED_PV01);

    functions.add(functionConfiguration(MarketQuotePositionFunction.class));
  }

  protected FunctionConfigurationSource cashFlowFunctionConfiguration() {
    return CashFlowFunctions.instance();
  }

  protected FunctionConfigurationSource covarianceFunctionConfiguration() {
    return CovarianceFunctions.instance();
  }

  protected FunctionConfigurationSource irCurveFunctionConfiguration() {
    return IRCurveFunctions.instance();
  }

  protected FunctionConfigurationSource fxForwardCurveFunctionConfiguration() {
    return FXForwardCurveFunctions.instance();
  }

  protected FunctionConfigurationSource modelFunctionConfiguration() {
    return ModelFunctions.instance();
  }

  protected FunctionConfigurationSource securityFunctionConfiguration() {
    return SecurityFunctions.instance();
  }

  protected FunctionConfigurationSource timeSeriesFunctionConfiguration() {
    return TimeSeriesFunctions.instance();
  }

  protected FunctionConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), cashFlowFunctionConfiguration(), covarianceFunctionConfiguration(), irCurveFunctionConfiguration(),
        fxForwardCurveFunctionConfiguration(), modelFunctionConfiguration(), securityFunctionConfiguration(), timeSeriesFunctionConfiguration(), volatilityFunctionConfiguration());
  }

}
