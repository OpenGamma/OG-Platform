/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.future;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.BondFutureConstantSpreadHorizonCalculator;
import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.analytics.model.bondcurves.BondAndBondFutureFromCurvesFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the one day theta for bond futures by rolling down the curves without slide.
 */
public class BondFutureConstantSpreadThetaFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Void> {
  /** The theta calculator */
  private static final HorizonCalculator<BondFuturesTransactionDefinition, IssuerProviderDiscount, Double> CALCULATOR =
      BondFutureConstantSpreadHorizonCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_THETA} and
   * the calculator to null
   */
  public BondFutureConstantSpreadThetaFunction() {
    super(VALUE_THETA, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final BondFutureSecurity security = (BondFutureSecurity) target.getTrade().getSecurity();
    final BondFuturesTransactionDefinition definition = (BondFuturesTransactionDefinition) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
    final IssuerProviderDiscount issuerCurves = (IssuerProviderDiscount) inputs.getValue(CURVE_BUNDLE);
    final HistoricalTimeSeries futurePriceSeries = (HistoricalTimeSeries) inputs.getValue(HISTORICAL_TIME_SERIES);
    final LocalDateDoubleTimeSeries ts = futurePriceSeries.getTimeSeries();
    final int length = ts.size();
    if (length == 0) {
      throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
    }
    final double lastMarginPrice = ts.getLatestValue();
    final int daysForward = Integer.parseInt(desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD));
    final MultipleCurrencyAmount theta = CALCULATOR.getTheta(definition, now, issuerCurves, daysForward, null, lastMarginPrice);
    if (theta.size() != 1) {
      throw new OpenGammaRuntimeException("Got result with more than one currency for theta: " + theta);
    }
    final ValueSpecification spec = new ValueSpecification(VALUE_THETA, target.toSpecification(), properties);
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    return Collections.singleton(new ComputedValue(spec, theta.getAmount(currency)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> daysForward = constraints.getValues(PROPERTY_DAYS_TO_MOVE_FORWARD);
    if (daysForward == null || daysForward.size() != 1) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return super.getResultProperties(target)
        .withAny(PROPERTY_DAYS_TO_MOVE_FORWARD)
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
        .with(CURRENCY, currency);
  }
}
