/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import com.opengamma.id.VersionCorrection;

/**
 * A source of volatility surface specifications.
 * <p>
 * This interface provides a simple view of volatility surface specifications.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface SurfaceSpecificationSource {


  /**
   * Gets a surface specification for a currency and name.
   * @param name  the name, not null
   * @return the specification, null if not found
   */
  SurfaceSpecification getSpecification(String name);

  /**
   * Gets a surface specification for a currency, name and version.
   * @param name  the name, not null
   * @param versionCorrection  the version correction, not null
   * @return the specification, null if not found
   */
  SurfaceSpecification getSpecification(String name, VersionCorrection versionCorrection);
}
