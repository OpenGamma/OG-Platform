/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.riskfactors.RiskFactorsGatherer;
import com.opengamma.financial.view.HistoricalViewEvaluationResult;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Uses the risk factor values to create the covariance matrix.
 */
public class RiskFactorCovarianceMatrixFunction extends SampledCovarianceMatrixFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(RiskFactorCovarianceMatrixFunction.class);

  // FunctionDefinition

  /**
   * Initializes the function.
   * 
   * @param context the compilation context
   * @deprecated [PLAT-2240] This just checks whether the context is suitably configured
   */
  @Deprecated
  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    if (OpenGammaCompilationContext.getRiskFactorsGatherer(context) == null) {
      throw new IllegalStateException("Function compilation context does not contain " + OpenGammaCompilationContext.RISK_FACTORS_GATHERER_NAME);
    }
  }

  // SampledCovarianceMatrix

  @Override
  protected String getDataType() {
    return "RiskFactors";
  }

  @Override
  protected void addValueRequirements(final FunctionCompilationContext context, final Portfolio portfolio, final ViewCalculationConfiguration calcConfig) {
    final RiskFactorsGatherer riskFactors = OpenGammaCompilationContext.getRiskFactorsGatherer(context);
    calcConfig.addSpecificRequirements(riskFactors.getPositionRiskFactors(portfolio));
  }

  @Override
  protected void addValueRequirements(final FunctionCompilationContext context, final Position position, final ViewCalculationConfiguration calcConfig) {
    final RiskFactorsGatherer riskFactors = OpenGammaCompilationContext.getRiskFactorsGatherer(context);
    calcConfig.addSpecificRequirements(riskFactors.getPositionRiskFactors(position));
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalViewEvaluationResult riskFactors = (HistoricalViewEvaluationResult) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    // Grab the value specifications as a list so that we iterate over them in a consistent order to construct the matrix
    final ValueRequirement[] riskFactorReqs = riskFactors.getValueRequirements().toArray(new ValueRequirement[riskFactors.getValueRequirements().size()]);
    final double[][] values = new double[riskFactorReqs.length][riskFactorReqs.length];
    final Double[] keys = new Double[riskFactorReqs.length];
    for (int i = 0; i < riskFactorReqs.length; i++) {
      keys[i] = 0d;
      // TODO: The line below is just to give the matrix some information until the sub-classes implement their thing properly
      values[i][i] = 1d;
      final LocalDateDoubleTimeSeries timeSeries = riskFactors.getDoubleTimeSeries(riskFactorReqs[i]);
      s_logger.debug("{} = {}", riskFactorReqs[i], timeSeries);
      // TODO: Need to do something more useful with the time series
    }
    final ValueRequirement desiredValueReq = desiredValues.iterator().next();
    final ValueSpecification desiredValueSpec = new ValueSpecification(VALUE_NAME, target.toSpecification(), desiredValueReq.getConstraints());
    return Collections.singleton(new ComputedValue(desiredValueSpec, new DoubleLabelledMatrix2D(keys, riskFactorReqs, keys, riskFactorReqs, values)));
  }

}
