/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.depgraph.DependencyNodeFilter;

/**
 * Strategy for filtering computation targets as dependency nodes.
 * <p>
 * This interface is used to decide whether a specific node should be
 * included in the sub-graph.
 */
public interface ComputationTargetFilter extends DependencyNodeFilter {

}
