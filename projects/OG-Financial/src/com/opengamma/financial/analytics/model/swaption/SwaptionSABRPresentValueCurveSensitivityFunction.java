/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveLabelGenerator;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionSABRPresentValueCurveSensitivityFunction extends SwaptionSABRFunction {
  private final PresentValueSensitivityCalculator _calculator;

  public SwaptionSABRPresentValueCurveSensitivityFunction(final String currency, final String definitionName, final String useSABRExtrapolation) {
    this(Currency.of(currency), definitionName, Boolean.parseBoolean(useSABRExtrapolation));
  }

  public SwaptionSABRPresentValueCurveSensitivityFunction(final Currency currency, final String definitionName, final boolean useSABRExtrapolation) {
    super(currency, definitionName, useSABRExtrapolation);
    _calculator = useSABRExtrapolation ? PresentValueCurveSensitivitySABRExtrapolationCalculator.getInstance() : PresentValueCurveSensitivitySABRCalculator.getInstance();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final SwaptionSecurity swaptionSecurity = (SwaptionSecurity) target.getSecurity();
    final FixedIncomeInstrumentConverter<?> swaptionDefinition = swaptionSecurity.accept(getConverter());
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(curveNames.getFirst(), curveNames.getSecond(), target, inputs));
    final InterestRateDerivative swaption = swaptionDefinition.toDerivative(now, curveNames.getFirst(), curveNames.getSecond());
    final Map<String, List<DoublesPair>> presentValueCurveSensitivity = _calculator.visit(swaption, data);
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, swaptionSecurity.getCurrency().getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
        .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get());
    return Sets.newHashSet(new ComputedValue(specification, presentValueCurveSensitivity));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
            .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get()));
  }

  //TODO this code is very similar to InterestRateInstrumentYieldCurveNodeSensitivitiesFunction - should be put into a utility class
  
  private Set<ComputedValue> getSensitivitiesForSingleCurve(final ComputationTarget target, final FinancialSecurity security, final String curveName,
      final YieldCurveBundle bundle, Map<String, List<DoublesPair>> sensitivities, final Currency currency) {
    Set<ComputedValue> computedValues = new HashSet<ComputedValue>();
    for (Map.Entry<String, List<DoublesPair>> entry : sensitivities.entrySet()) {
      ValueSpecificiation specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
      computedValues.add(new ComputedValue(specification, data.getLabelledMatrix()));
    }
    return computedValues;
  }

  //TODO at some point this needs to deal with more than two curves
  private Set<ComputedValue> getSensitivitiesForMultipleCurves(final ComputationTarget target, final FinancialSecurity security, final String forwardCurveName, final String fundingCurveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurves, final Currency currency) {
    final int nForward = bundle.getCurve(forwardCurveName).getCurve().size();
    final int nFunding = bundle.getCurve(fundingCurveName).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<String, DoubleMatrix1D>();
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nForward)));
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nForward, nFunding + 1)));
    final String[] relevantCurvesForDerivative = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        fundingCurveName, forwardCurveName);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final String curveName : relevantCurvesForDerivative) {
      results.addAll(getSensitivitiesForSingleCurve(target, security, curveName, bundle, sensitivities.get(curveName), currency));
    }
    return results;
  }
}
