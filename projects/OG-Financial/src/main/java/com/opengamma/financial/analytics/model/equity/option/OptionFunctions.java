/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.equity.futures.EquityNetMarketValueFunction;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceDefaults;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class OptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  /**
   * Gets the default values for calculations
   * @return The repository with equity option defaults set
   */
  public static FunctionConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * @param overhedge The overhedge to use for equity barrier options
   * @param callSpreadFullWidth The width of the call spread to use for barrier options
   * @param barrierFormat the barrier output display format
   * @return The repository with equity barrier option defaults set
   */
  public static FunctionConfigurationSource defaults(final double overhedge, final double callSpreadFullWidth, final String barrierFormat) {
    final Defaults factory = new Defaults();
    factory.setOverhedge(overhedge);
    factory.setCallSpreadFullWidth(callSpreadFullWidth);
    factory.setBarrierDistanceFormat(barrierFormat);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default function for equity barrier options contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private double _overhedge; /* = 0.0; */
    private double _callSpreadFullWidth = 0.001;
    private String _barrierFormat = BarrierOptionDistanceFunction.BARRIER_ABS;

    public void setOverhedge(final double overhedge) {
      _overhedge = overhedge;
    }

    public double getOverhedge() {
      return _overhedge;
    }

    public void setCallSpreadFullWidth(final double callSpreadFullWidth) {
      _callSpreadFullWidth = callSpreadFullWidth;
    }

    public double getCallSpreadFullWidth() {
      return _callSpreadFullWidth;
    }

    public void setBarrierDistanceFormat(final String format) {
      _barrierFormat = format;
    }

    public String getBarrierDistanceFormat() {
      return _barrierFormat;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityVanillaBarrierOptionDefaults.class, Double.toString(getOverhedge()), Double.toString(getCallSpreadFullWidth())));
      functions.add(functionConfiguration(BarrierOptionDistanceDefaults.class, getBarrierDistanceFormat()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionPDEPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionPDEScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueGammaFunction.class));

    functions.add(functionConfiguration(EquityOptionBlackBasicPresentValueFunction.class));

    functions.add(functionConfiguration(ListedEquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackScenarioPnLFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandScenarioPnLFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyGreeksFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionPresentValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionRhoFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVommaFunction.class));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_DELTA, ValueRequirementNames.DELTA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_GAMMA, ValueRequirementNames.GAMMA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_RHO, ValueRequirementNames.RHO));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_THETA, ValueRequirementNames.THETA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_VEGA, ValueRequirementNames.VEGA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_WEIGHTED_VEGA, ValueRequirementNames.WEIGHTED_VEGA));
    functions.add(functionConfiguration(WeightedVegaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionDistanceFunction.class));
    functions.add(functionConfiguration(NetCapitalFunction.class));
    functions.add(functionConfiguration(EquityNetCapitalFunction.class));
    functions.add(functionConfiguration(EquityNetMarketValueFunction.class));
  }

}
