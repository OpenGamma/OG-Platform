/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.sabr;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public final class SABRVolatilitySurfaceFunction {
  /** Name for all SABR parameters contained in bundle */
  //shouldn't be needed once the constraints are working 
  public static final String PROPERTY_SABR_SURFACES = "SABR_SURFACES";
  /** Identifies the fitted surface associated with the alpha parameter of SABR*/
  public static final String PROPERTY_ALPHA_SURFACE = "SABR_ALPHA_" + ValuePropertyNames.SURFACE;
  /** Identifies the fitted surface associated with the beta parameter of SABR */
  public static final String PROPERTY_BETA_SURFACE = "SABR_BETA_" + ValuePropertyNames.SURFACE;
  /** Identifies the fitted surface associated with the nu parameter of SABR */
  public static final String PROPERTY_NU_SURFACE = "SABR_NU_" + ValuePropertyNames.SURFACE;
  /** Identifies the fitted surface associated with the rho parameter of SABR */
  public static final String PROPERTY_RHO_SURFACE = "SABR_RHO_" + ValuePropertyNames.SURFACE;

  //TODO eventually need information and constraints showing how it was calculated and what the underlying data set is

  private SABRVolatilitySurfaceFunction() {
  }

  public static ValueRequirement getSABRSurfaceRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES,
        ComputationTargetType.PRIMITIVE,
        currency.getUniqueId(),
        ValueProperties.with(PROPERTY_SABR_SURFACES, PROPERTY_SABR_SURFACES).get());
  }

  public static ValueRequirement getAlphaSurfaceRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.SABR_ALPHA_SURFACE,
                                          ComputationTargetType.PRIMITIVE,
                                          currency.getUniqueId(),
                                          ValueProperties.with(PROPERTY_ALPHA_SURFACE, PROPERTY_ALPHA_SURFACE).get());
  }

  public static ValueRequirement getBetaSurfaceRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.SABR_BETA_SURFACE,
        ComputationTargetType.PRIMITIVE,
        currency.getUniqueId(),
        ValueProperties.with(PROPERTY_BETA_SURFACE, PROPERTY_BETA_SURFACE).get());
  }

  public static ValueRequirement getNuSurfaceRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.SABR_NU_SURFACE,
        ComputationTargetType.PRIMITIVE,
        currency.getUniqueId(),
        ValueProperties.with(PROPERTY_NU_SURFACE, PROPERTY_NU_SURFACE).get());
  }

  public static ValueRequirement getRhoSurfaceRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.SABR_RHO_SURFACE,
        ComputationTargetType.PRIMITIVE,
        currency.getUniqueId(),
        ValueProperties.with(PROPERTY_RHO_SURFACE, PROPERTY_RHO_SURFACE).get());
  }
}
