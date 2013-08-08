/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.id.VersionCorrection;

/**
 * A source of volatility surface specifications.
 * <p>
 * This interface provides a simple view of volatility surface specifications.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 * @deprecated should use config source instead.
 */
@Deprecated
public interface VolatilitySurfaceSpecificationSource {


  /**
   * Gets a volatility surface specification for a currency and name.
   * @param name  the name, not null
   * @param instrumentType the instrument type, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceSpecification getSpecification(String name, String instrumentType);

  /**
   * Gets a volatility surface specification for a currency, name and version.
   * @param name  the name, not null
   * @param instrumentType the instrument type, not null
   * @param versionCorrection  the version correction, not null
   * @return the definition, null if not found
   */
  VolatilitySurfaceSpecification getSpecification(String name, String instrumentType, VersionCorrection versionCorrection);
}
