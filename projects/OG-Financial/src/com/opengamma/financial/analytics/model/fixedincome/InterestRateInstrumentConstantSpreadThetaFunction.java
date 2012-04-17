/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class InterestRateInstrumentConstantSpreadThetaFunction extends InterestRateInstrumentFunction {
  /** Property value for constant spread theta calculations */
  public static final String CONSTANT_SPREAD = "ConstantSpread";
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public InterestRateInstrumentConstantSpreadThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ZonedDateTime tomorrow = snapshotClock.zonedDateTime().plusDays(1);
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final YieldCurveBundle bundle = getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, curveCalculationMethod);
    final InstrumentDefinition<?> definition = security.accept(getVisitor());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final String[] curveNamesForSecurity = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName);
    final InstrumentDerivative nowDerivative = getConverter().convert(security, definition, now, curveNamesForSecurity, dataSource);
    final InstrumentDerivative tomorrowDerivative = getConverter().convert(security, definition, tomorrow, curveNamesForSecurity, dataSource);
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final double presentValueNow = CALCULATOR.visit(nowDerivative, bundle);
    final double presentValueTomorrow = CALCULATOR.visit(tomorrowDerivative, bundle);
    double theta = presentValueTomorrow - presentValueNow;
    if (security instanceof BondSecurity) {
      final BondSecurity bondSec = (BondSecurity) security;
      theta *= bondSec.getParAmount();
    }
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency), theta));
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security,
      final ComputationTarget target, final String forwardCurveName,
      final String fundingCurveName, final String curveCalculationMethod, final String currency) {
    throw new UnsupportedOperationException("Should never get here");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CALCULATION_METHOD, CONSTANT_SPREAD);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CALCULATION_METHOD, CONSTANT_SPREAD);
    return properties;
  }
}
