/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class CommodityFutureOptionFunction extends FutureOptionFunction {

  /**
   * @param valueRequirementNames The value requirement names
   */
  public CommodityFutureOptionFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.COMMODITY_FUTURE_OPTION_SECURITY;
  }

  @Override
  protected ValueRequirement getVolatilitySurfaceRequirement(final FinancialSecurity security, final String surfaceName, final String smileInterpolator,
      final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceCalculationMethod) {
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String fullSurfaceName = CommodityFutureOptionUtils.getSurfaceName(security, surfaceName);
    final String fullCurveName = CommodityFutureOptionUtils.getSurfaceName(security, forwardCurveName);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, fullSurfaceName)
        .with(ValuePropertyNames.CURVE, fullCurveName)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, smileInterpolator)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .with(ValuePropertyNames.SURFACE_CALCULATION_METHOD, surfaceCalculationMethod)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION)
        .get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

//  protected ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final Security security, final String surfaceName,
//      final String surfaceCalculationMethod) {
//    final ExternalId currencyId = ExternalId.of(Currency.OBJECT_SCHEME, FinancialSecurityUtils.getCurrency(security).getCode());
//    return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, surfaceName, InstrumentTypeProperties.COMMODITY_FUTURE_OPTION,
//        ComputationTargetType.CURRENCY, currencyId);
//  }
  @Override
  protected ValueRequirement getForwardCurveRequirement(final FinancialSecurity security, final String forwardCurveName, final String forwardCurveCalculationMethod) {
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String fullCurveName = CommodityFutureOptionUtils.getSurfaceName(security, forwardCurveName);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, fullCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetSpecification.of(currency), properties);
  }
}
