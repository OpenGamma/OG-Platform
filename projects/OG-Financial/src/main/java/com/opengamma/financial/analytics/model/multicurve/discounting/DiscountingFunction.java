/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Base function for all pricing and risk functions that use the discounting
 * construction method.
 */
public abstract class DiscountingFunction extends MultiCurvePricingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public DiscountingFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Base compiled function for all pricing and risk functions that use the discounting
   * curve construction method.
   */
  protected abstract class DiscountingCompiledFunction extends MultiCurveCompiledFunction {
    /** True if the result properties set the {@link ValuePropertyNames#CURRENCY} property */
    private final boolean _withCurrency;

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property
     */
    protected DiscountingCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter);
      _withCurrency = withCurrency;
    }

    @Override
    protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .withAny(CURVE_EXPOSURES);
      if (_withCurrency) {
        properties.with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode());
      }
      return properties;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> curveExposures = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposures == null) {
        return false;
      }
      return true;
    }

    @Override
    protected Builder getCurveProperties(final ComputationTarget target, final ValueProperties constraints) {
      return ValueProperties.builder();
    }

  }
}
