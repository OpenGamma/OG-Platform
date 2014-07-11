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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
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
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;

/**
 * 
 */
public class BillTotalReturnSwapBCSFunction extends BillTotalReturnSwapFunction {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BillTotalReturnSwapBCSFunction.class);
  
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityIssuerCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  /**
   * 
   */
  public BillTotalReturnSwapBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
    return new BillTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context),
                                                   getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues,
          InstrumentDerivative derivative, FXMatrix fxMatrix) {
        ParameterIssuerProviderInterface issuerCurves = getMergedWithIssuerProviders(inputs, fxMatrix);
        CurveBuildingBlockBundle blocks = new CurveBuildingBlockBundle();
        for (ComputedValue cv : inputs.getAllValues()) {
          if (JACOBIAN_BUNDLE.equals(cv.getSpecification().getValueName())) {
            blocks.addAll((CurveBuildingBlockBundle) cv.getValue()); 
          }
        }
        
        Set<ComputedValue> result = new HashSet<>();
        MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, issuerCurves, blocks);
        for (ValueRequirement desiredValue : desiredValues) {
          final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), desiredValue.getConstraints().copy().get());
          result.add(new ComputedValue(spec, sensitivities));
        }
        return result;
      }
      
      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext compilationContext,
                                                   ComputationTarget target,
                                                   ValueRequirement desiredValue) {
        return super.getRequirements(compilationContext, target, desiredValue);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext compilationContext,
                                                ComputationTarget target,
                                                Map<ValueSpecification, ValueRequirement> inputs) {
        return super.getResults(compilationContext, target, inputs);
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(FunctionCompilationContext compilationContext,
                                                                        ComputationTarget target) {
        return Collections.singleton(createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .withAny(PROPERTY_CURVE_TYPE));
      }

      @Override
      protected String getCurrencyOfResult(BillTotalReturnSwapSecurity security) {
        throw new IllegalStateException("BillTotalReturnSwapBCSFunction does not set the Currency property in this method");
      }
    };
  }
}
