/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

/**
 * Provides access to code-based configuration of Hibernate mapping files.
 */
public interface HibernateMappingFiles {

  /**
   * Gets an array of classes that have associated mapping files.
   * This is useful to avoid listing the mapping files multiple times in a Spring configuration
   * and for type-safety.
   * @return the mapping file classes, not null
   */
  Class<?>[] getHibernateMappingFiles();

}
