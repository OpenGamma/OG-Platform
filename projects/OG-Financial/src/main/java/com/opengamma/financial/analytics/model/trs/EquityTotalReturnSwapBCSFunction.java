/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.equity.PresentValueCurveSensitivityEquityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Block Curve Sensitivity for Equity Total Return Swap
 */
public class EquityTotalReturnSwapBCSFunction extends EquityTotalReturnSwapFunction {

   /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<EquityTrsDataBundle, MultipleCurrencyMulticurveSensitivity> PVCSEDC =
      PresentValueCurveSensitivityEquityDiscountingCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<EquityTrsDataBundle> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSEDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<EquityTrsDataBundle> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  /**
   *
   */
  public EquityTotalReturnSwapBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
    return new EquityTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context),
                                                     getDefinitionToDerivativeConverter(context),
                                                     true) {

      @Override
      protected Set<ComputedValue> getValues(FunctionExecutionContext executionContext,
                                             FunctionInputs inputs,
                                             ComputationTarget target,
                                             Set<ValueRequirement> desiredValues,
                                             InstrumentDerivative derivative,
                                             FXMatrix fxMatrix) {
        EquityTrsDataBundle data = getDataBundle(inputs, fxMatrix);
        CurveBuildingBlockBundle blocks = new CurveBuildingBlockBundle();
        for (ComputedValue cv : inputs.getAllValues()) {
          if (JACOBIAN_BUNDLE.equals(cv.getSpecification().getValueName())) {
            blocks.addAll((CurveBuildingBlockBundle) cv.getValue()); 
          }
        }
        
        Set<ComputedValue> result = new HashSet<>();
        MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, data, blocks);
        for (ValueRequirement desiredValue : desiredValues) {
          final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES,
                                                                 target.toSpecification(),
                                                                 desiredValue.getConstraints().copy().get());
          result.add(new ComputedValue(spec, sensitivities));
        }
        return result;
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext compilationContext,
                                                   ComputationTarget target,
                                                   ValueRequirement desiredValue) {
        Set<ValueRequirement> req = super.getRequirements(compilationContext, target, desiredValue);
        return req;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext compilationContext,
                                                ComputationTarget target,
                                                Map<ValueSpecification,
                                                ValueRequirement> inputs) {
        Set<ValueSpecification> spec = super.getResults(compilationContext, target, inputs);
        return spec;
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(FunctionCompilationContext compilationContext,
                                                                        ComputationTarget target) {
        ValueProperties.Builder properties = createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .withAny(PROPERTY_CURVE_TYPE);
        return Collections.singleton(properties);
      }
    };
  }
}
