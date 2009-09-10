package com.opengamma.financial.model.tree;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class RecombiningTree<T> implements Lattice<T> {
  private final T[][] _tree;

  public RecombiningTree(T[][] data) {
    _tree = data;
  }

  protected abstract int getMaxNodesForStep(int step) throws Exception;

  @Override
  public T getNode(int step, int node) throws Exception {
    if (step > _tree.length)
      throw new IllegalArgumentException("Step number " + step + " is greater than maximum in this tree (max =  " + _tree.length + ")");
    int max = getMaxNodesForStep(step);
    if (node > max)
      throw new IllegalArgumentException("Node number " + node + " is greater than the number of nodes at this step number (max = " + max + ")");
    return _tree[step][node];
  }

  @Override
  public T[][] getTree() {
    return _tree;
  }

  @Override
  public void setNode(T value, int step, int node) throws Exception {
    if (step > _tree.length)
      throw new IllegalArgumentException("Step number " + step + " is greater than maximum in this tree (max =  " + _tree.length + ")");
    int max = getMaxNodesForStep(step);
    if (node > max)
      throw new IllegalArgumentException("Node number " + node + " is greater than the number of nodes at this step number (max = " + max + ")");
    _tree[step][node] = value;
  }
}
