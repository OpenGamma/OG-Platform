/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.id.UniqueIdentifier;

/**
 * A resolver from a target specification to a real target.
 * <p>
 * Within the engine targets are often referred to by the specification for performance reasons.
 * The resolver converts the specifications, identified by a unique identifier, back to a real target.
 */
public interface ComputationTargetResolver {

  /**
   * Resolves the specification to a real target.
   * <p>
   * The specification contains a {@link UniqueIdentifier} that refers to a real target,
   * such as a portfolio or security. The resolver converts this reference back to the original
   * fully formed object.
   * @param specification  the specification to resolve, not null
   * @return the resolved target, null if not found
   */
  ComputationTarget resolve(ComputationTargetSpecification specification);

}
