/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import com.opengamma.id.VersionCorrection;

/**
 * A source of surface definitions.
 * <p>
 * This interface provides a simple view of surface definitions.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface SurfaceDefinitionSource {

  /**
   * Gets a surface definition for a currency and name.
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  SurfaceDefinition<?, ?> getDefinition(String name);

  /**
   * Gets a surface definition for a currency, name and version.
   * @param name  the name, not null
   * @param versionCorrection  the version correction, not null
   * @return the definition, null if not found
   */
  SurfaceDefinition<?, ?> getDefinition(String name, VersionCorrection versionCorrection);

}
