/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public interface Lattice<T> {

  public T[][] getTree();

  public T getNode(int step, int node);
}
