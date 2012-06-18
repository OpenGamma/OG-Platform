/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueId;

/**
 * A resolver from a target specification to a real target.
 * <p>
 * Within the engine targets are often referred to by the specification for performance reasons.
 * The resolver converts the specifications, identified by a unique identifier, back to a real target.
 */
public interface ComputationTargetResolver {

  // TODO: move to com.opengamma.engine.target

  /**
   * Resolves the specification to a real target.
   * <p>
   * The specification contains a {@link UniqueId} that refers to a real target,
   * such as a portfolio or security. The resolver converts this reference back to the original
   * fully formed object.
   * @param specification  the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  ComputationTarget resolve(ComputationTargetSpecification specification);

  /**
   * Returns the {@link SecuritySource} associated with the resolver, if any. If there is no security source then the resolver will not be able to resolve {@link ComputationTargetType#SECURITY}
   * targets. Structures returned by the source will be fully resolved, as they would be if returned from the {@link #resolve} method. A security source should only be obtained from a resolver if the
   * same resolution semantics are necessary.
   * 
   * @return the security source, or null if none
   */
  SecuritySource getSecuritySource();

  /**
   * Returns the {@link PositionSource} associated with the resolver, if any. If there is no position source then the resolver may not be able to resolve {@link ComputationTargetType#POSITION},
   * {@link ComputationTargetType#PORTFOLIO_NODE}, or {@link ComputationTargetType#TRADE} targets. Structures returned by the source will be fully resolved, as they would be if returned from the
   * {@link #resolve} method. A position source should only be obtained from a resolver if the same resolution semantics are necessary.
   * 
   * @return the position source, or null if none
   */
  PositionSource getPositionSource();

}
