/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionSABRPresentValueCurveSensitivityFunction extends SwaptionSABRFunction {
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.");
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
    return getSensitivities(target, swaptionSecurity, presentValueCurveSensitivity);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    ValueProperties resultProperties = getResultProperties((FinancialSecurity) target.getSecurity());
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(), resultProperties));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    ValueProperties resultProperties = getResultProperties((FinancialSecurity) target.getSecurity(), curveNames.getSecond(), curveNames.getFirst());
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(), resultProperties)); 
  }

  private Set<ComputedValue> getSensitivities(final ComputationTarget target, final SwaptionSecurity swaptionSecurity, Map<String, List<DoublesPair>> sensitivities) {
    Set<ComputedValue> computedValues = new HashSet<ComputedValue>();
    for (Map.Entry<String, List<DoublesPair>> entry : sensitivities.entrySet()) {
      final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(), createValueProperties()
          .with(ValuePropertyNames.CURRENCY, swaptionSecurity.getCurrency().getCode())
          .with(ValuePropertyNames.CURVE, entry.getKey())
          .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get());
      List<DoublesPair> data = entry.getValue();
      int n = data.size();
      Double[] keys = new Double[n];
      Object[] labels = new Object[n];
      double[] values = new double[n];
      int i = 0;
      for (DoublesPair pair : entry.getValue()) {
        double tenor = pair.getKey();
        keys[i] = tenor;
        labels[i] = FORMATTER.format(tenor);
        values[i++] = pair.getValue();
      }
      DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
      computedValues.add(new ComputedValue(specification, labelledMatrix));
    }
    return computedValues;
  }
}
