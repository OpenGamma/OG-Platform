/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

//TODO Have a general n-D lattice (i.e. a tensor) 
/**
 *
 * @param <T> Type of the data that the lattice contains
 */
public interface Lattice3D<T> {
  
  /**
   * Gets the base data of the tree
   * @return The underlying data for the tree 
   */
  T[][][] getNodes();

  

  /**
   * Get an individual node value
   * @param step Time position in the tree 
   * @param i Position at a given time (step) along the first axis
   * @param j Position at a given time (step) along the second axis
   * @return an individual node value
   */
  T getNode(int step, int i, int j);

}
