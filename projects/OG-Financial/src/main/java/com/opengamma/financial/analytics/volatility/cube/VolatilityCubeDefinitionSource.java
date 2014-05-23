/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.id.VersionCorrection;

/**
 * A source of vol cube definitions.
 * <p>
 * This interface provides a simple view of vol cube definitions.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface VolatilityCubeDefinitionSource {

  /**
  * Gets a cube definition for a name and instrument type.
  * @param name  the name, not null
  * @return the definition, null if not found
  */
  VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name);

  /**
  * Gets a cube definition for a name, instrument type and version.
  * @param name  the name, not null
  * @param versionCorrection  the version correction, not null
  * @return the definition, null if not found
  */
  VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name, VersionCorrection versionCorrection);

}
