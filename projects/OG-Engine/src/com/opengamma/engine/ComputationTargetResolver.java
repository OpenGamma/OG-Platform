/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * A resolver from a target specification to a real target.
 * <p>
 * Within the engine targets are often referred to by the specification for performance reasons. The resolver converts the specifications, identified by a unique identifier, back to a real target.
 */
public interface ComputationTargetResolver {

  // TODO: move to com.opengamma.engine.target.resolver

  /**
   * Resolves the specification to a real target.
   * <p>
   * The specification contains a {@link UniqueId} that refers to a real target, such as a portfolio or security. The resolver converts this reference back to the original fully formed object. The
   * type component of the specification may be used as a hint on how to resolve the unique identifier. The type field of the resolved target may be a sub-type of the hinted type that more accurately
   * describes the target object.
   * 
   * @param specification the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  ComputationTarget resolve(ComputationTargetSpecification specification);

  /**
   * Simplifies the type to the simplest form that this resolver will recognize. For example {@code CTSpec[FooSecurity, Sec~1]} might be simplified to {@code CTSpec[SECURITY, Sec~1]} if the same
   * resolution will take place regardless of whether the type is a security or a sub-class of it. If no simplification is possible, the original type may be returned.
   * <p>
   * Note that it is always correct to return the type object unchanged.
   * 
   * @param type the type to simplify, not null
   * @return the simplified type, not null
   */
  ComputationTargetType simplifyType(ComputationTargetType type);

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
