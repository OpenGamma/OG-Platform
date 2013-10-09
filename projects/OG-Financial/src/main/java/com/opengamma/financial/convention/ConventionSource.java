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
   * Gets a convention for the unique identifier of an instrument.
   * @param identifier An identifier of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found.
   */
  Convention getConvention(UniqueId identifier);

  /**
   * Gets a convention for the given identifier that is of the specified type.
   * @param <T> The expected type of the convention
   * @param clazz The expected class of the convention
   * @param identifier An identifier of the instrument
   * @return The convention data or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found, or if
   * the type of the convention does not match that expected
   */
  <T extends Convention> T getConvention(Class<T> clazz, ExternalId identifier);

  /**
   * Gets a convention for the given instrument identifiers that is of the specified
   * type.
   * @param <T> The expected type of the convention
   * @param clazz The expected class of the convention
   * @param identifiers The identifiers of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found, or if
   * the type of the convention does not match that expected
   */
  <T extends Convention> T getConvention(Class<T> clazz, ExternalIdBundle identifiers);

  /**
   * Gets a convention for the unique identifier of an instrument that is of the specified
   * type.
   * @param <T> The expected type of the convention
   * @param clazz The expected class of the convention
   * @param identifier An identifier of the instrument
   * @return The convention data, or null if not found
   * @throws OpenGammaRuntimeException if multiple matches to the identifier are found.
   */
  <T extends Convention> T getConvention(Class<T> clazz, UniqueId identifier);
}
