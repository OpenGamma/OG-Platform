/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

/**
 * Strategy handling the creation of a database catalog.
 * <p>
 * This allows databases to be created on the fly.
 */
public interface CatalogCreationStrategy {

  /**
   * Checks if the database catalog exists already.
   * 
   * @param catalog  the catalog name, not null
   * @return true if it exists
   */
  boolean catalogExists(String catalog);

  /**
   * Creates a database catalog.
   * <p>
   * If the catalog already exists, does nothing.
   * 
   * @param catalog  the name of the catalog to create, not null
   */
  void create(String catalog);

}
