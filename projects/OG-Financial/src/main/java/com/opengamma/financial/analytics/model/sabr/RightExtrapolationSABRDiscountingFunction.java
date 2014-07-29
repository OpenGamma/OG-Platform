/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

import static com.opengamma.engine.value.ValuePropertyNames.CUBE;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_MU;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_STRIKE_CUTOFF;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.RIGHT_EXTRAPOLATION;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.SABR;

import java.util.Collection;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * Base function for all pricing and risk functions that use SABR parameter surfaces and curves constructed using the discounting method and right extrapolation.
 */
public abstract class RightExtrapolationSABRDiscountingFunction extends SABRDiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public RightExtrapolationSABRDiscountingFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Base compiled function for all pricing and risk functions that use SABR parameter surfaces and curves constructed using the discounting method.
   */
  protected abstract class RightExtrapolationSABRDiscountingCompiledFunction extends SABRDiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected RightExtrapolationSABRDiscountingCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final Collection<ValueProperties.Builder> properties = super.getResultProperties(context, target);
      for (ValueProperties.Builder builder : properties) {
        builder.withAny(PROPERTY_STRIKE_CUTOFF).withAny(PROPERTY_MU);
      }
      return properties;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueProperties constraints = desiredValue.getConstraints();
      final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
      final Set<String> cube = constraints.getValues(CUBE);
      final ValueProperties properties = ValueProperties.builder().with(CUBE, cube).with(CURRENCY, currency.getCode()).with(PROPERTY_VOLATILITY_MODEL, SABR).get();
      final ValueRequirement surfacesRequirement = new ValueRequirement(ValueRequirementNames.SABR_SURFACES, ComputationTargetSpecification.of(currency), properties);
      requirements.add(surfacesRequirement);
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> cubeNames = constraints.getValues(CUBE);
      if (cubeNames == null) {
        return false;
      }
      final Set<String> strikeCutoffs = constraints.getValues(PROPERTY_STRIKE_CUTOFF);
      if (strikeCutoffs == null || strikeCutoffs.size() != 1) {
        return false;
      }
      final Set<String> mus = constraints.getValues(PROPERTY_MU);
      if (mus == null || mus.size() != 1) {
        return false;
      }
      return super.requirementsSet(constraints);
    }

    @Override
    protected String getCalculationMethod() {
      return RIGHT_EXTRAPOLATION;
    }

  }

}
