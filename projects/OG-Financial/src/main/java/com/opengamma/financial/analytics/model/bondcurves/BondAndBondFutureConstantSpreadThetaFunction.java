/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.BondConstantSpreadHorizonCalculator;
import com.opengamma.analytics.financial.horizon.BondFutureConstantSpreadHorizonCalculator;
import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.instrument.bond.BondTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the one day theta for bonds and bond futures by rolling down the curves without slide.
 */
public class BondAndBondFutureConstantSpreadThetaFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Void> {
  /** The theta calculator for bond futures */
  private static final HorizonCalculator<BondFuturesTransactionDefinition, IssuerProviderInterface, Double> BOND_FUTURE_CALCULATOR =
      BondFutureConstantSpreadHorizonCalculator.getInstance();
  /** The theta calculator for bonds */
  private static final HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> BOND_CALCULATOR =
      BondConstantSpreadHorizonCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_THETA} and
   * the calculator to null
   */
  public BondAndBondFutureConstantSpreadThetaFunction() {
    super(VALUE_THETA, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final IssuerProviderInterface issuerCurves = (IssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final Security security = target.getTrade().getSecurity();
    final MultipleCurrencyAmount theta;
    if (security instanceof BondFutureSecurity) {
      final BondFuturesTransactionDefinition definition = (BondFuturesTransactionDefinition) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
      final HistoricalTimeSeries futurePriceSeries = (HistoricalTimeSeries) inputs.getValue(HISTORICAL_TIME_SERIES);
      final LocalDateDoubleTimeSeries ts = futurePriceSeries.getTimeSeries();
      final int length = ts.size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getLatestValue();
      final int daysForward = Integer.parseInt(desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD));
      theta = BOND_FUTURE_CALCULATOR.getTheta(definition, now, issuerCurves, daysForward, null, lastMarginPrice);
    } else if (security instanceof BondSecurity) {
      final BondTransactionDefinition<?, ?> definition = (BondTransactionDefinition<?, ?>) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
      final int daysForward = Integer.parseInt(desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD));
      theta = BOND_CALCULATOR.getTheta(definition, now, issuerCurves, daysForward, null);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle securities of type " + security.getClass());
    }
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
  protected Collection<ValueProperties.Builder> getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final Collection<ValueProperties.Builder> properties = super.getResultProperties(target);
    final Collection<ValueProperties.Builder> result = new HashSet<>();
    for (final ValueProperties.Builder builder : properties) {
      result.add(builder
          .withAny(PROPERTY_DAYS_TO_MOVE_FORWARD)
          .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
          .with(CURRENCY, currency));
    }
    return result;
  }
}
