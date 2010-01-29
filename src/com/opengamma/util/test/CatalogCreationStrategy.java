/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

/**
 * 
 *
 * @author pietari
 */
public interface CatalogCreationStrategy {
  
  public boolean catalogExists(String catalog);
  
  /**
   * Creates a catalog.
   * <p>
   * If the catalog already exists, does nothing.
   */
  public void create(String catalog);

}
