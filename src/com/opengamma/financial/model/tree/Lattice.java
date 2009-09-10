package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public interface Lattice<T> {

  public void setNode(T value, int step, int node) throws Exception;// TODO
                                                                    // rename
                                                                    // node
                                                                    // variable

  public T[][] getTree() throws Exception;

  public T getNode(int step, int node) throws Exception;
}
