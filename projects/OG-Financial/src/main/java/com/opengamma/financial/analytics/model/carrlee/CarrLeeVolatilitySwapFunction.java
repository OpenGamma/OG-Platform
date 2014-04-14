/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.carrlee;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_REALIZED_VARIANCE_METHOD;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeData;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.conversion.VolatilitySwapSecurityConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Base function for all volatility swap pricing and risk function that use the Carr-Lee method.
 */
public abstract class CarrLeeVolatilitySwapFunction extends DiscountingFunction {
  /** The calculation method */
  public static final String CARR_LEE = "CarrLee";

  /**
   * @param valueRequirements The value requirement names, not null
   */
  public CarrLeeVolatilitySwapFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final VolatilitySwapSecurityConverter securityConverter = new VolatilitySwapSecurityConverter(holidaySource);
    return new DefaultTradeConverter(securityConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use the Carr-Lee method.
   */
  protected abstract class CarrLeeVolatilitySwapCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected CarrLeeVolatilitySwapCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .with(CALCULATION_METHOD, CARR_LEE)
          .withAny(SURFACE)
          .withAny(X_INTERPOLATOR_NAME)
          .withAny(LEFT_X_EXTRAPOLATOR_NAME)
          .withAny(RIGHT_X_EXTRAPOLATOR_NAME)
          .withAny(PROPERTY_REALIZED_VARIANCE_METHOD)
          .withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
        properties.with(CURRENCY, currency);
        return Collections.singleton(properties);
      }
      return Collections.singleton(properties);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.add(getVolatilitySurfaceRequirement(desiredValue, target));
      requirements.add(getSpotRequirement(target));
      // TODO ideally, realized variance should only be requested for seasoned swaps
      requirements.add(getRealizedVarianceRequirement(desiredValue, target));
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> surfaceNames = constraints.getValues(SURFACE);
      if (surfaceNames == null) {
        return false;
      }
      final Set<String> interpolatorNames = constraints.getValues(X_INTERPOLATOR_NAME);
      if (interpolatorNames == null || interpolatorNames.size() != 1) {
        return false;
      }
      final Set<String> leftExtrapolatorNames = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
      if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
        return false;
      }
      final Set<String> rightExtrapolatorNames = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
      if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
        return false;
      }
      final Set<String> varianceCalculationMethods = constraints.getValues(PROPERTY_REALIZED_VARIANCE_METHOD);
      if (varianceCalculationMethods == null || varianceCalculationMethods.size() != 1) {
        return false;
      }
      return super.requirementsSet(constraints);
    }

    /**
     * Gets the volatility surface requirement.
     *
     * @param desiredValue The desired value
     * @param target The target
     * @return The volatility surface requirement
     */
    protected abstract ValueRequirement getVolatilitySurfaceRequirement(ValueRequirement desiredValue, ComputationTarget target);

    /**
     * Gets the spot requirement.
     *
     * @param target The target
     * @return The spot requirement
     */
    protected abstract ValueRequirement getSpotRequirement(ComputationTarget target);

    /**
     * Gets the realized variance requirement.
     *
     * @param desiredValue The desired vale
     * @param target The target
     * @return The realized variance requirement
     */
    protected abstract ValueRequirement getRealizedVarianceRequirement(ValueRequirement desiredValue, ComputationTarget target);

    /**
     * Gets the Carr-Lee data.
     *
     * @param executionContext The execution context, not null
     * @param inputs The function inputs, not null
     * @param target The computation target, not null
     * @param fxMatrix The FX matrix, not null
     * @return The Carr-Lee data
     */
    protected abstract CarrLeeData<? extends MulticurveProviderInterface, ?> getCarrLeeData(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final FXMatrix fxMatrix);
  }
}
