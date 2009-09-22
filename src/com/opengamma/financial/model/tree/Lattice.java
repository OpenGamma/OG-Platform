package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public interface Lattice<T> {

  public void setNode(T value, int step, int node);// TODO

  // rename
  // node
  // variable

  public T[][] getTree();

  public T getNode(int step, int node);
}
