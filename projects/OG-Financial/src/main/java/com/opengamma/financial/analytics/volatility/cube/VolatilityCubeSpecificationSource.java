/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.id.VersionCorrection;

/**
 * A source of volatility cube specifications.
 * <p>
 * This interface provides a simple view of volatility cube specifications.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface VolatilityCubeSpecificationSource {

  /**
  * Gets a cube specification for a name.
  * @param name  the name, not null
  * @return the specification, null if not found
  */
  VolatilityCubeSpecification getSpecification(final String name);

  /**
  * Gets a cube specification for a name and version.
  * @param name  the name, not null
  * @param versionCorrection  the version correction, not null
  * @return the specification, null if not found
  */
  VolatilityCubeSpecification getSpecification(final String name, VersionCorrection versionCorrection);

}
