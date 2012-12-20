/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

/**
 * 
 * @param <T>
 */
public interface Lattice<T> {

  /**
   * Gets the base data of the tree
   * @return The underlying data for the tree 
   */
  T[][] getNodes();

  /**
   * Gets an empty tree with steps equal to size and nodes at each step dependent on the concrete implementation  
   * @param size The number of steps in the tree
   * @return an empty data set representing a tree
   */
 // T[][] getEmptyNodes(int size);

  /**
   * Get an individual node value
   * @param step Time position in the tree 
   * @param node Position at a given time (step) 
   * @return an individual node value
   */
  T getNode(int step, int node);
}
