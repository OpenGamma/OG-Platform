/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_DAYS_PER_YEAR;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.DEFAULT_DAYS_PER_YEAR;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.OPTION_THETA;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackforex.ValueThetaForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
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
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the value (forward driftless) theta of FX options using a Black surface and curves constructed using the discounting method. The result is scaled by the number of days in a year, with
 * the default being 365.25.
 */
public class BlackDiscountingValueThetaFXOptionFunction extends BlackDiscountingFXOptionFunction {
  /** The value theta calculator */
  private static final InstrumentDerivativeVisitor<BlackForexSmileProviderInterface, CurrencyAmount> CALCULATOR = ValueThetaForexBlackSmileCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_THETA}
   */
  public BlackDiscountingValueThetaFXOptionFunction() {
    super(VALUE_THETA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final BlackForexSmileProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final CurrencyAmount valueTheta = derivative.accept(CALCULATOR, blackData);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        double daysPerYear;
        final ValueProperties.Builder propertiesWithDaysPerYear = properties.copy().withoutAny(PROPERTY_DAYS_PER_YEAR);
        final Set<String> daysPerYearProperty = properties.getValues(PROPERTY_DAYS_PER_YEAR);
        if (daysPerYearProperty.isEmpty() || daysPerYearProperty.size() != 1) {
          daysPerYear = DEFAULT_DAYS_PER_YEAR;
          propertiesWithDaysPerYear.with(PROPERTY_DAYS_PER_YEAR, Double.toString(DEFAULT_DAYS_PER_YEAR));
        } else {
          daysPerYear = Double.parseDouble(Iterables.getOnlyElement(daysPerYearProperty));
          propertiesWithDaysPerYear.with(PROPERTY_DAYS_PER_YEAR, daysPerYearProperty);
        }
        final String currency = Iterables.getOnlyElement(properties.getValues(CURRENCY));
        if (!currency.equals(valueTheta.getCurrency().getCode())) {
          throw new OpenGammaRuntimeException("Currency of result " + valueTheta.getCurrency() + " did not match" + " the expected currency " + currency);
        }
        final ValueSpecification spec = new ValueSpecification(VALUE_THETA, target.toSpecification(), propertiesWithDaysPerYear.get());
        return Collections.singleton(new ComputedValue(spec, valueTheta.getAmount() / daysPerYear));
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Collection<ValueProperties.Builder> properties = super.getResultProperties(compilationContext, target);
        for (ValueProperties.Builder builder : properties) {
          builder.with(PROPERTY_THETA_CALCULATION_METHOD, OPTION_THETA).withAny(PROPERTY_DAYS_PER_YEAR);
        }
        return properties;
      }

    };
  }
}
