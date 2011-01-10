/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

/**
 * 
 * @param <T>
 */
public interface Lattice<T> {

  T[][] getTree();

  T getNode(int step, int node);
}
