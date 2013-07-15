/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Source of convention data.
 */
public interface ConventionSource {

  /**
   * Gets a convention for the given identifier.
   * @param identifier An identifier of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found.
   */
  Convention getConvention(ExternalId identifier);

  /**
   * Gets a convention for the given instrument identifiers.
   * @param identifiers The identifiers of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found.
   */
  Convention getConvention(ExternalIdBundle identifiers);

  /**
   * Gets a convention for an instrument's unique identifier.
   * @param identifier An identifier of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found.
   */
  Convention getConvention(UniqueId identifier);

}
