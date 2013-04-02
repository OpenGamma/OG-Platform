/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.view.HistoricalViewEvaluationTarget;
import com.opengamma.financial.view.ViewEvaluationFunction;
import com.opengamma.id.UniqueId;

/**
 * Iterates a view client over a window of historical data to get time series of values from which a covariance matrix can be constructed. The target will identify the item(s) for which data should be
 * gathered to build the matrix from.
 */
public abstract class SampledCovarianceMatrixFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The value name produced by this function.
   */
  // TODO: Promote this into ValueRequirementNames
  public static final String VALUE_NAME = "CovarianceMatrix";

  /**
   * When used in "permissive" mode, will use this period as a default sampling duration.
   */
  private static final Period DEFAULT_SAMPLING_PERIOD = Period.ofMonths(1);

  private static final ComputationTargetType TYPE = ComputationTargetType.PORTFOLIO.or(ComputationTargetType.PORTFOLIO_NODE).or(ComputationTargetType.POSITION);

  /**
   * Returns the type of data used to construct the matrix, and distinguish between different sub-class implementations. For example, this might be market data, risk factors or something else.
   * 
   * @return the type, not null
   */
  protected abstract String getDataType();

  protected Set<ValueRequirement> createRequirements(final ComputationTargetSpecification tempTargetSpec) {
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, tempTargetSpec, ValueProperties.withAny(ViewEvaluationFunction.PROPERTY_CALC_CONFIG).get()));
  }

  protected void addValueRequirements(final FunctionCompilationContext context, final Portfolio portfolio, final ViewCalculationConfiguration calcConfig) {
    addValueRequirements(context, portfolio.getRootNode(), calcConfig);
  }

  protected void addValueRequirements(final FunctionCompilationContext context, final PortfolioNode node, final ViewCalculationConfiguration calcConfig) {
    for (PortfolioNode child : node.getChildNodes()) {
      addValueRequirements(context, child, calcConfig);
    }
    for (Position child : node.getPositions()) {
      addValueRequirements(context, child, calcConfig);
    }
  }

  protected abstract void addValueRequirements(FunctionCompilationContext context, Position position, ViewCalculationConfiguration calcConfig);

  protected void addValueRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ViewCalculationConfiguration calcConfig) {
    if (target.getValue() instanceof Portfolio) {
      addValueRequirements(context, (Portfolio) target.getValue(), calcConfig);
    } else if (target.getValue() instanceof PortfolioNode) {
      addValueRequirements(context, (PortfolioNode) target.getValue(), calcConfig);
    } else if (target.getValue() instanceof Position) {
      addValueRequirements(context, (Position) target.getValue(), calcConfig);
    }
  }

  protected ViewCalculationConfiguration createViewCalculationConfiguration(final ViewDefinition viewDefinition, final String calcConfigName) {
    return new ViewCalculationConfiguration(viewDefinition, calcConfigName);
  }

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return context.getViewCalculationConfiguration() != null;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.with("Type", getDataType());
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(VALUE_NAME, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.SAMPLING_PERIOD).get()));
  }

  private String anyConstraintOrNull(final ValueProperties constraints, final String name) {
    final Set<String> values = constraints.getValues(name);
    if ((values == null) || values.isEmpty()) {
      return null;
    } else {
      return values.iterator().next();
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String lookbackPeriodString = anyConstraintOrNull(constraints, ValuePropertyNames.SAMPLING_PERIOD);
    final DateConstraint startDate;
    if (lookbackPeriodString == null) {
      if (!OpenGammaCompilationContext.isPermissive(context)) {
        return null;
      }
      startDate = DateConstraint.VALUATION_TIME.minus(DEFAULT_SAMPLING_PERIOD);
    } else {
      startDate = DateConstraint.VALUATION_TIME.minus(Period.parse(lookbackPeriodString));
    }
    final ViewDefinition viewDefinition = context.getViewCalculationConfiguration().getViewDefinition();
    final HistoricalViewEvaluationTarget tempTarget = new HistoricalViewEvaluationTarget(viewDefinition.getMarketDataUser(), startDate.toString(), true, DateConstraint.VALUATION_TIME.toString(),
        false);
    final ViewCalculationConfiguration calcConfig = createViewCalculationConfiguration(tempTarget.getViewDefinition(), context.getViewCalculationConfiguration().getName());
    addValueRequirements(context, target, calcConfig);
    tempTarget.getViewDefinition().addViewCalculationConfiguration(calcConfig);
    final TempTargetRepository targets = OpenGammaCompilationContext.getTempTargets(context);
    final UniqueId tempTargetId = targets.locateOrStore(tempTarget);
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(TempTarget.TYPE, tempTargetId);
    return createRequirements(targetSpec);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final TempTarget tempTargetObject = OpenGammaCompilationContext.getTempTargets(context).get(inputs.keySet().iterator().next().getTargetSpecification().getUniqueId());
    if (!(tempTargetObject instanceof HistoricalViewEvaluationTarget)) {
      return null;
    }
    final HistoricalViewEvaluationTarget historicalTarget = (HistoricalViewEvaluationTarget) tempTargetObject;
    final DateConstraint startDate = DateConstraint.parse(historicalTarget.getStartDate());
    final DateConstraint endDate = DateConstraint.parse(historicalTarget.getEndDate());
    final Period samplingPeriod = startDate.periodUntil(endDate);
    return Collections
        .singleton(new ValueSpecification(VALUE_NAME, target.toSpecification(), createValueProperties().with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod.toString()).get()));
  }

}
