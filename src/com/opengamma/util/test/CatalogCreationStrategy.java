/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

/**
 * 
 *
 */
public interface CatalogCreationStrategy {
  
  boolean catalogExists(String catalog);
  
  /**
   * Creates a catalog.
   * <p>
   * If the catalog already exists, does nothing.
   * 
   * @param catalog Name of the catalog to create.
   */
  void create(String catalog);

}
