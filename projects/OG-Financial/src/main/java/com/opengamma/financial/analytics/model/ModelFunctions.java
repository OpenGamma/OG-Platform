/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondcleanprice.BondCleanPriceFunctions;
import com.opengamma.financial.analytics.model.bondcurves.BondCurveFunctions;
import com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves.InflationBondCurveFunctions;
import com.opengamma.financial.analytics.model.bondyield.BondYieldFunctions;
import com.opengamma.financial.analytics.model.carrlee.CarrLeeFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.discounting.DiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.fx.FXForwardPricingFunctions;
import com.opengamma.financial.analytics.model.g2ppdiscounting.G2ppPricingFunctions;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhitePricingFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.swaption.SwaptionFunctions;
import com.opengamma.financial.analytics.model.timeseries.TimeSeriesFunctions;
import com.opengamma.financial.analytics.model.trs.TotalReturnSwapFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class ModelFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new ModelFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(MarginPriceFunction.class));
    functions.add(functionConfiguration(PVCashBalanceFunction.class));
    functions.add(functionConfiguration(FXCurrencyExposureFunction.class));
    /*functions.add(functionConfiguration(InflationBondFromCurvesFunction.class));*/

  }

  /**
   * Adds deprecated bond functions.
   * @return A configuration source containing deprecated bond functions
   * @deprecated The new versions of these functions are added in {{@link #bondCleanPriceFunctionConfiguration()}
   */
  @Deprecated
  protected FunctionConfigurationSource bondFunctionConfiguration() {
    return BondFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from the clean price.
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondCleanPriceFunctionConfiguration() {
    return BondCleanPriceFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from yield curves.
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondCurveFunctionConfiguration() {
    return BondCurveFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from yield curves.
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource inflationbondCurveFunctionConfiguration() {
    return InflationBondCurveFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from the clean price.
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondYieldFunctionConfiguration() {
    return BondYieldFunctions.instance();
  }

  /**
   * Adds functions that produce analytics for volatility swaps using the Carr-Lee
   * model.
   * @return A configuration source containing pricing and analytics functions
   */
  protected FunctionConfigurationSource carrLeeFunctionConfiguration() {
    return CarrLeeFunctions.instance();
  }


  /**
   * Adds functions that produce curves.
   * @return A configuration source containing curve functions
   */
  protected FunctionConfigurationSource curveFunctionConfiguration() {
    return CurveFunctions.instance();
  }

  /**
   * Adds equity functions.
   * @return A configuration source containing equity functions
   */
  protected FunctionConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.instance();
  }

  /**
   * Adds deprecated interest rate instrument functions.
   * @return A configuration source containing the deprecated interest rate instrument functions
   * @deprecated The current versions of these functions are added in {@link #discountingFunctionConfiguration}
   */
  @Deprecated
  protected FunctionConfigurationSource fixedIncomeFunctionConfiguration() {
    return com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed with the discounting method.
   * @return A configuration source containing these functions.
   */
  protected FunctionConfigurationSource discountingFunctionConfiguration() {
    return DiscountingPricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use Black surfaces and curve constructed with
   * the discounting method.
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource blackDiscountingFunctionConfiguration() {
    return BlackDiscountingPricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the Hull-White
   * one factor discounting method.
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource hullWhitePricingFunctionConfiguration() {
    return HullWhitePricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the G2++
   * discounting method
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource g2ppPricingFunctionConfiguration() {
    return G2ppPricingFunctions.instance();
  }

  protected FunctionConfigurationSource fxPricingFunctionConfiguration() {
    return FXForwardPricingFunctions.instance();
  }

  protected FunctionConfigurationSource yieldCurveFunctionConfiguration() {
    return InterestRateFunctions.instance();
  }

  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource forexFunctionConfiguration() {
    return ForexFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource futureOptionFunctionConfiguration() {
    return FutureOptionFunctions.instance();
  }

  /**
   * Adds interest rate future-specific functions.
   * @return A configuration source containing the deprecated interest rate future functions.
   * @deprecated The current versions of these functions are added in {@link ModelFunctions#blackDiscountingFunctionConfiguration}
   */
  @Deprecated
  protected FunctionConfigurationSource interestRateFutureFunctionConfiguration() {
    return FutureFunctions.deprecated();
  }

  /**
   * Adds general option functions.
   * @return A configuration source containing option functions
   * @deprecated The underlying-specific functions should be used
   */
  @Deprecated
  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected FunctionConfigurationSource pnlFunctionConfiguration() {
    return PNLFunctions.instance();
  }

  protected FunctionConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  /**
   * Adds SABR pricing functions for swaptions, cap/floors, CMS and cap/floor CMS spreads
   * @return A configuration source containing the deprecated functions
   * @deprecated The current versions of these functions are added in {@link ModelFunctions#sabrDiscountingFunctionConfiguration()}
   */
  @Deprecated
  protected FunctionConfigurationSource sabrCubeFunctionConfiguration() {
    return SABRCubeFunctions.instance();
  }

  protected FunctionConfigurationSource sabrDiscountingFunctionConfiguration() {
    return SABRDiscountingPricingFunctions.instance();
  }

  protected FunctionConfigurationSource sensitivitiesFunctionConfiguration() {
    return SensitivitiesFunctions.instance();
  }

  protected FunctionConfigurationSource swaptionFunctionConfiguration() {
    return SwaptionFunctions.instance();
  }

  protected FunctionConfigurationSource varFunctionConfiguration() {
    return VaRFunctions.instance();
  }

  protected FunctionConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  protected FunctionConfigurationSource futureCurveFunctionConfiguration() {
    return com.opengamma.financial.analytics.model.curve.future.FutureFunctions.instance();
  }

  /**
   * Adds time series functions.
   * @return A configuration source containing time series functions
   */
  protected FunctionConfigurationSource timeSeriesFunctionConfiguration() {
    return TimeSeriesFunctions.instance();
  }

  /**
   * Adds total return swap functions.
   * @return A configuration source containing total return swap functions
   */
  protected FunctionConfigurationSource totalReturnSwapFunctionConfiguration() {
    return TotalReturnSwapFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(),
        bondFunctionConfiguration(),
        bondCleanPriceFunctionConfiguration(),
        bondCurveFunctionConfiguration(),
        inflationbondCurveFunctionConfiguration(),
        bondYieldFunctionConfiguration(),
        carrLeeFunctionConfiguration(),
        curveFunctionConfiguration(),
        equityFunctionConfiguration(),
        fixedIncomeFunctionConfiguration(),
        forexFunctionConfiguration(),
        futureFunctionConfiguration(),
        futureOptionFunctionConfiguration(),
        optionFunctionConfiguration(),
        pnlFunctionConfiguration(),
        riskFactorFunctionConfiguration(),
        sabrCubeFunctionConfiguration(),
        sensitivitiesFunctionConfiguration(),
        swaptionFunctionConfiguration(),
        varFunctionConfiguration(),
        volatilityFunctionConfiguration(),
        yieldCurveFunctionConfiguration(),
        forwardFunctionConfiguration(),
        futureCurveFunctionConfiguration(),
        discountingFunctionConfiguration(),
        hullWhitePricingFunctionConfiguration(),
        interestRateFutureFunctionConfiguration(),
        fxPricingFunctionConfiguration(),
        blackDiscountingFunctionConfiguration(),
        sabrDiscountingFunctionConfiguration(),
        g2ppPricingFunctionConfiguration(),
        timeSeriesFunctionConfiguration(),
        totalReturnSwapFunctionConfiguration());
  }

}
