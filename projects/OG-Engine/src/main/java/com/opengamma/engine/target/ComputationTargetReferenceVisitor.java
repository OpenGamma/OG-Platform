/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.PublicAPI;

/**
 * A visitor for handling {@link ComputationTargetReference} instances that may be either resolved specifications or unresolved requirements.
 * 
 * @param <T> the return type for the visitor
 */
@PublicAPI
public interface ComputationTargetReferenceVisitor<T> {

  /**
   * Visit a reference that is an unresolved requirement.
   * 
   * @param requirement the unresolved requirement, not null
   * @return the result of the visiting operation
   */
  T visitComputationTargetRequirement(ComputationTargetRequirement requirement);

  /**
   * Visit a reference that is a resolvable specification.
   * 
   * @param specification the target specification, not null
   * @return the result of the visiting operation
   */
  T visitComputationTargetSpecification(ComputationTargetSpecification specification);

}
