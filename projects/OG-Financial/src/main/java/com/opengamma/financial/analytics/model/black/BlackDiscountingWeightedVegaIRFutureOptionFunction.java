/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.POSITION_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.POSITION_WEIGHTED_VEGA;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
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
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Calculates the weighted position vega of interest rate future options using a Black surface and curves constructed using the discounting method.
 */
public class BlackDiscountingWeightedVegaIRFutureOptionFunction extends BlackDiscountingIRFutureOptionFunction {
  /** Property name for the number of base days */
  public static final String PROPERTY_BASE_DAYS = "BaseDays";
  /** Default number of base days to use */
  private static final double DEFAULT_BASE_DAYS = 90;

  /**
   * Sets the value requirement to {@link ValueRequirementNames#POSITION_WEIGHTED_VEGA}
   */
  public BlackDiscountingWeightedVegaIRFutureOptionFunction() {
    super(POSITION_WEIGHTED_VEGA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> baseDayProperty = constraints.getValues(PROPERTY_BASE_DAYS);
        final ValueProperties.Builder resultConstraints = constraints.copy();
        final double baseDays;
        if (baseDayProperty.size() == 1) {
          baseDays = Double.parseDouble(Iterables.getOnlyElement(baseDayProperty));
          resultConstraints.with(PROPERTY_BASE_DAYS, baseDayProperty);
        } else {
          baseDays = DEFAULT_BASE_DAYS;
          resultConstraints.with(PROPERTY_BASE_DAYS, Double.toString(DEFAULT_BASE_DAYS));
        }
        final double positionVega = (Double) inputs.getValue(POSITION_VEGA);
        final IRFutureOptionSecurity security = (IRFutureOptionSecurity) target.getTrade().getSecurity();
        final Expiry expiry = security.getExpiry();
        if (expiry.getAccuracy().equals(ExpiryAccuracy.MONTH_YEAR) || expiry.getAccuracy().equals(ExpiryAccuracy.YEAR)) {
          throw new OpenGammaRuntimeException("Security's expiry is not accurate to the day, which is required: " + security.toString());
        }
        final long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(executionContext.getValuationClock()), expiry.getExpiry().toLocalDate());
        final double weighting = Math.sqrt(baseDays / Math.max(daysToExpiry, 1.0));
        final double weightedVega = weighting * positionVega;
        final ValueSpecification valueSpecification = new ValueSpecification(POSITION_WEIGHTED_VEGA, target.toSpecification(), resultConstraints.get());
        final ComputedValue result = new ComputedValue(valueSpecification, weightedVega);
        return Sets.newHashSet(result);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (super.getRequirements(compilationContext, target, desiredValue) == null) {
          return null;
        }
        final ValueProperties properties = desiredValue.getConstraints();
        return Collections.singleton(new ValueRequirement(POSITION_VEGA, target.toSpecification(), properties.copy().get()));
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Collection<ValueProperties.Builder> properties = super.getResultProperties(compilationContext, target);
        for (ValueProperties.Builder builder : properties) {
          builder.withAny(PROPERTY_BASE_DAYS);
        }
        return properties;
      }
    };
  }
}
