/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.hullwhitediscounting;

import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY_ADJUSTMENT;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ConvexityAdjustmentHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * Calculates the convexity adjustment of instruments that have been priced using
 * the Hull-White one factor method.
 */
public class HullWhiteConvexityAdjustmentFunction extends HullWhiteDiscountingFunction {
  /** The convexity adjustment calculator */
  private static final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> CALCULATOR =
      ConvexityAdjustmentHullWhiteCalculator.getInstance();

  /**
   * Sets the value requirements to {@link ValueRequirementNames#CONVEXITY_ADJUSTMENT}
   */
  public HullWhiteConvexityAdjustmentFunction() {
    super(CONVEXITY_ADJUSTMENT);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new HullWhiteCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final HullWhiteOneFactorProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final double convexityAdjustment = derivative.accept(CALCULATOR, data);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(CONVEXITY_ADJUSTMENT, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, convexityAdjustment));
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        return security instanceof DeliverableSwapFutureSecurity ||
            security instanceof InterestRateFutureSecurity;
      }

    };
  }
}
