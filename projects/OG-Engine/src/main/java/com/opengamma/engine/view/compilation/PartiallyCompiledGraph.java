/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.ExecutionOrderNodeIterator;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Holds state relating to an incremental compilation. This is essentially the content of a {@link DependencyGraph} in mutable form.
 * <p>
 * Note that this implements {@link DependencyGraph} as a convenience for some uses of the data; it should not be passed around as a general purpose dependency graph as not all methods are
 * implemented.
 */
public final class PartiallyCompiledGraph implements DependencyGraph {

  /**
   * The current roots of the graph.
   */
  private final List<DependencyNode> _roots;

  /**
   * The current terminal output set. The map is mutable but the sets of value requirements are not.
   */
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminals;

  /**
   * The ejected value requirements that need to be re-added as part of an incremental build. The set is mutable.
   */
  private final Set<ValueRequirement> _requirements;

  /**
   * Creates a new instance, populated with the content of an existing graph compilation.
   * 
   * @param original the graph to initialise state from, not null
   */
  public PartiallyCompiledGraph(final DependencyGraph original) {
    final int count = original.getRootCount();
    _roots = new ArrayList<DependencyNode>(count);
    for (int i = 0; i < count; i++) {
      _roots.add(original.getRootNode(i));
    }
    _terminals = new HashMap<ValueSpecification, Set<ValueRequirement>>(original.getTerminalOutputs());
    _requirements = new HashSet<ValueRequirement>();
  }

  /**
   * Returns the current root nodes. The collection may be modified by the caller.
   * 
   * @return the root nodes
   */
  public Collection<DependencyNode> getRoots() {
    return _roots;
  }

  /**
   * Returns the current missing requirement set for incremental compilation. The caller may modify this to add or remove requirements.
   * 
   * @return the current missing requirement set, not null
   */
  public Set<ValueRequirement> getMissingRequirements() {
    return _requirements;
  }

  // DependencyGraph

  @Override
  public Iterator<DependencyNode> nodeIterator() {
    return new ExecutionOrderNodeIterator(this);
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
    return _terminals;
  }

  @Override
  public String getCalculationConfigurationName() {
    return null;
  }

  @Override
  public int getSize() {
    // This is not correct, but better than nothing
    return _roots.size();
  }

  @Override
  public int getRootCount() {
    return _roots.size();
  }

  @Override
  public DependencyNode getRootNode(int index) {
    return _roots.get(index);
  }

}
