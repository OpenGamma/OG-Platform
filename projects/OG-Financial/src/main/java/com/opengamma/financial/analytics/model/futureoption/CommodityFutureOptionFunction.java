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
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
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
  protected ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final FinancialSecurity security, final String surfaceName,
      final String forwardCurveName, final String surfaceCalculationMethod) {
    final ExternalId currencyId = ExternalId.of(Currency.OBJECT_SCHEME, FinancialSecurityUtils.getCurrency(security).getCode());
    final String fullSurfaceName = CommodityFutureOptionUtils.getSurfaceName(security, surfaceName);
    final String fullForwardCurveName = CommodityFutureOptionUtils.getSurfaceName(security, forwardCurveName);
    final ValueRequirement requirement = BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, ValueProperties.none(), surfaceName, forwardCurveName,
        InstrumentTypeProperties.COMMODITY_FUTURE_OPTION, ComputationTargetType.CURRENCY, currencyId);
    final ValueProperties.Builder properties = requirement.getConstraints().copy();
    properties.withoutAny(ValuePropertyNames.SURFACE).with(ValuePropertyNames.SURFACE, fullSurfaceName);
    properties.withoutAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE, fullForwardCurveName);
    return new ValueRequirement(requirement.getValueName(), requirement.getTargetReference(), properties.get());
  }

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

  @Override
  protected String getSurfaceName(final FinancialSecurity security, final String surfaceName) {
    return CommodityFutureOptionUtils.getSurfaceNameWithoutTicker(security, surfaceName);
  }
}
