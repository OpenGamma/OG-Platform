/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve.hullwhitediscounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.HULL_WHITE_DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_CURRENCY;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;

/**
 * Base function for all pricing and risk functions that use the discounting curve
 * construction method.
 */
public abstract class HullWhiteFunction extends MultiCurvePricingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public HullWhiteFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Base compiled function for all pricing and risk functions that use the Hull-White one-factor
   * curve construction method.
   */
  protected abstract class HullWhiteCompiledFunction extends MultiCurveCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     */
    protected HullWhiteCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter);
    }

    @Override
    protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
      return createValueProperties()
          .with(PROPERTY_CURVE_TYPE, HULL_WHITE_DISCOUNTING)
          .withAny(CURVE_EXPOSURES)
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .withAny(PROPERTY_HULL_WHITE_CURRENCY);
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposureConfigs == null) {
        return false;
      }
      final Set<String> hullWhiteParameters = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      if (hullWhiteParameters == null || hullWhiteParameters.size() != 1) {
        return false;
      }
      final Set<String> hullWhiteCurrencies = constraints.getValues(PROPERTY_HULL_WHITE_CURRENCY);
      if (hullWhiteCurrencies == null || hullWhiteCurrencies.size() != 1) {
        return false;
      }
      return true;
    }

    @Override
    protected Builder getCurveProperties(final ComputationTarget target, final ValueProperties constraints) {
      final Set<String> currency = constraints.getValues(PROPERTY_HULL_WHITE_CURRENCY);
      final Set<String> hullWhiteParameters = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      return ValueProperties.builder()
          .with(PROPERTY_HULL_WHITE_PARAMETERS, hullWhiteParameters)
          .with(PROPERTY_HULL_WHITE_CURRENCY, currency);
    }

  }
}
