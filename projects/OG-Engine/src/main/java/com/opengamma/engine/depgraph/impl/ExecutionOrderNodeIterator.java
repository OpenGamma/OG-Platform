/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;

/**
 * Iterator over all nodes in a graph in execution order.
 */
public final class ExecutionOrderNodeIterator extends HashSet<DependencyNode> implements Iterator<DependencyNode> {

  private static final long serialVersionUID = 1L;

  private final DependencyGraph _graph;
  private final int _rootCount;
  private DependencyNode[] _node;
  private int[] _index;
  private int _stack;
  private DependencyNode _next;

  // _node[_stack] is the parent we're returning child nodes from
  // _index[_stack] is the child index last used from _node[_stack]

  private static int defaultLoadFactorSize(final int elements) {
    return elements + (elements / 3);
  }

  public ExecutionOrderNodeIterator(final DependencyGraph graph) {
    super(defaultLoadFactorSize(graph.getSize()));
    _node = new DependencyNode[16];
    _index = new int[_node.length];
    _graph = graph;
    _rootCount = graph.getRootCount();
    if (_rootCount != 0) {
      _next = graph.getRootNode(0);
      _node[0] = null;
      _index[0] = 0;
      while (_next.getInputCount() != 0) {
        final int nextIndex = ++_stack;
        if (nextIndex >= _node.length) {
          resize();
        }
        _node[nextIndex] = _next;
        _next = _next.getInputNode(0);
      }
      add(_next);
    } else {
      _stack = -1;
    }
  }

  private void resize() {
    final DependencyNode[] newNode = new DependencyNode[_node.length * 2];
    System.arraycopy(_node, 0, newNode, 0, _node.length);
    _node = newNode;
    final int[] newIndex = new int[_index.length * 2];
    System.arraycopy(_index, 0, newIndex, 0, _index.length);
    _index = newIndex;
  }

  @Override
  public boolean hasNext() {
    if (_next != null) {
      return true;
    }
    nextNode: do { //CSIGNORE
      if (_stack > 0) {
        final DependencyNode parent = _node[_stack];
        final int child = _index[_stack] + 1;
        if (child < parent.getInputCount()) {
          _index[_stack] = child;
          _next = parent.getInputNode(child);
          if (contains(_next)) {
            // Already visited this child; don't go any deeper
            continue;
          }
          while (_next.getInputCount() != 0) {
            final int nextIndex = ++_stack;
            if (nextIndex >= _node.length) {
              resize();
            }
            _node[nextIndex] = _next;
            _index[nextIndex] = 0;
            _next = _next.getInputNode(0);
            if (contains(_next)) {
              // Already visited this child
              continue nextNode;
            }
          }
          add(_next);
        } else {
          _stack--;
          _next = parent;
          if (!add(_next)) {
            continue;
          }
        }
        return true;
      } else if (_stack == 0) {
        final int root = _index[0] + 1;
        if (root >= _rootCount) {
          _stack = -1;
          return false;
        } else {
          _index[0] = root;
          _next = _graph.getRootNode(root);
          assert !contains(_next); // Already visited this root; this is bad - the roots should not be present elsewhere
          while (_next.getInputCount() != 0) {
            final int nextIndex = ++_stack;
            if (nextIndex >= _node.length) {
              resize();
            }
            _node[nextIndex] = _next;
            _index[nextIndex] = 0;
            _next = _next.getInputNode(0);
            if (contains(_next)) {
              // Already visited this child
              continue nextNode;
            }
          }
          add(_next);
          return true;
        }
      } else {
        return false;
      }
    } while (true);
  }

  @Override
  public DependencyNode next() {
    if (_next == null) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
    }
    DependencyNode next = _next;
    _next = null;
    return next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
