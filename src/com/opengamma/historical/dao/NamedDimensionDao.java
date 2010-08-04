/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.util.Set;

/**
 * Data access object for a named dimension.
 */
public interface NamedDimensionDao {

  /**
   * Gets all the named dimensions.
   * @return the named dimensions
   */
  Set<NamedDimensionDao> getAll();

  /**
   * Gets all the names.
   * @return the names
   */
  Set<String> getAllNames();

  /**
   * Gets by id.
   * @return the values
   */
  Set<String> getById();

}
