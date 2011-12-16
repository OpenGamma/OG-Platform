/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cmsspread;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class CapFloorCMSSpreadSABRPresentValueSABRFunction extends CapFloorCMSSpreadSABRFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public CapFloorCMSSpreadSABRPresentValueSABRFunction(final String currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public CapFloorCMSSpreadSABRPresentValueSABRFunction(final Currency currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, forwardCurveName, fundingCurveName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final CapFloorCMSSpreadSecurity capFloorSecurity = (CapFloorCMSSpreadSecurity) target.getSecurity();
    final InstrumentDefinition<?> capFloorDefinition = capFloorSecurity.accept(getVisitor());
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(target, inputs));
    final InstrumentDerivative capFloor = capFloorDefinition.toDerivative(now, getFundingCurveName(), getForwardCurveName());
    final PresentValueSABRSensitivityDataBundle presentValue = CALCULATOR.visit(capFloor, data);
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, capFloorSecurity.getCurrency().getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
        .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get();
    final ValueSpecification alphaSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.toSpecification(), resultProperties);
    final ValueSpecification nuSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.toSpecification(), resultProperties);
    final ValueSpecification rhoSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.toSpecification(), resultProperties);
    final Map<DoublesPair, Double> alpha = presentValue.getAlpha().getMap();
    final Map<DoublesPair, Double> nu = presentValue.getNu().getMap();
    final Map<DoublesPair, Double> rho = presentValue.getRho().getMap();
    final DoubleLabelledMatrix2D alphaValue = getMatrix(alpha);
    final DoubleLabelledMatrix2D nuValue = getMatrix(nu);
    final DoubleLabelledMatrix2D rhoValue = getMatrix(rho);
    return Sets.newHashSet(new ComputedValue(alphaSpec, alphaValue), new ComputedValue(nuSpec, nuValue), new ComputedValue(rhoSpec, rhoValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
        .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get();
    return getResults(target.toSpecification(), resultProperties);
  }

  private Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification alphaSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, targetSpec, resultProperties);
    final ValueSpecification nuSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, targetSpec, resultProperties);
    final ValueSpecification rhoSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, targetSpec, resultProperties);
    return Sets.newHashSet(alphaSpec, nuSpec, rhoSpec);
  }

  private DoubleLabelledMatrix2D getMatrix(final Map<DoublesPair, Double> map) {
    final Map.Entry<DoublesPair, Double> entry = map.entrySet().iterator().next();
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
  }

}
