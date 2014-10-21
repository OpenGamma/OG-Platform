/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.VEGA_MATRIX;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.BlackSwaptionSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSensitivityBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.VegaMatrixUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the At-The-Money {@link ValueRequirementNames#VEGA_MATRIX} of a {@link SwaptionSecurity} 
 * using a Black (lognormal) surface and curves constructed using the Discounting method.
 * 
 */
public class BlackDiscountingVegaMatrixSwaptionFunction extends BlackDiscountingSwaptionFunction {
  /** The value vega calculator */
  private static final InstrumentDerivativeVisitor<BlackSwaptionFlatProviderInterface, PresentValueSwaptionSurfaceSensitivity> VEGA_CALCULATOR =
      PresentValueBlackSensitivityBlackSwaptionCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VEGA_MATRIX}
   */
  public BlackDiscountingVegaMatrixSwaptionFunction() {
    super(VEGA_MATRIX);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackSwaptionFlatProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        // Compute scalar value of the Black Vega
        final PresentValueSwaptionSurfaceSensitivity vegaSens = derivative.accept(VEGA_CALCULATOR, blackData);
        // Distribute the vega back onto the nodes of the Vol Surface according to the interpolator
        final PresentValueSwaptionSurfaceSensitivity vegaMap = (new BlackSwaptionSensitivityNodeCalculator()).calculateNodeSensitivities(vegaSens, blackData.getBlackParameters());
        // Repackage the sensitivities into a format easy that's fit for display
        DoubleLabelledMatrix2D vegaMatrix = VegaMatrixUtils.getVegaSwaptionMatrix(vegaMap);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(VEGA_MATRIX, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, vegaMatrix));
      }

    };
  }

}
