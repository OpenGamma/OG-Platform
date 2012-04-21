/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SABRRightExtrapolationPresentValueFunction extends SABRRightExtrapolationFunction {
  private final PresentValueCalculator _calculator = PresentValueSABRExtrapolationCalculator.getInstance();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String cutoff = desiredValue.getConstraint(PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(PROPERTY_TAIL_THICKNESS_PARAMETER);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(getVisitor());
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, currency, desiredValue);
    final InstrumentDerivative derivative = getConverter().convert(security, definition, now, new String[] {fundingCurveName, forwardCurveName}, dataSource);
    final double presentValue = _calculator.visit(derivative, data);
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency.getCode(), forwardCurveName, fundingCurveName, cubeName, cutoff, mu);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, presentValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
  }
}
