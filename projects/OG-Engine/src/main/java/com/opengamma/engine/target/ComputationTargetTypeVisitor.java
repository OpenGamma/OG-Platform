/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.List;
import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A visitor for working with {@link ComputationTargetType} instances.
 * 
 * @param <D> the data parameter for the visitor
 * @param <T> the return type for the visitor
 */
@PublicAPI
public interface ComputationTargetTypeVisitor<D, T> {

  /**
   * Handle a type that is one of multiple types, for example "a POSITION or a TRADE".
   * 
   * @param types the possible alternative types, not null and containing at least two elements
   * @param data the data parameter passed to the visitor operation
   * @return the result of the visitor operation
   */
  T visitMultipleComputationTargetTypes(Set<ComputationTargetType> types, D data);

  /**
   * Handle a type that is nested, for example "a POSITION within a PORTFOLIO_NODE".
   * 
   * @param types the sequence of types, not null and containing at least two elements. The outermost type is listed first, for example {@code [PORTFOLIO_NODE, POSITION]}.
   * @param data the data parameter passed to the visitor operation
   * @return the result of the visitor operation
   */
  T visitNestedComputationTargetTypes(List<ComputationTargetType> types, D data);

  /**
   * Handle the explicit null type instance.
   * 
   * @param data the data parameter passed to the visitor operation
   * @return the result of the visitor operation
   */
  T visitNullComputationTargetType(D data);

  /**
   * Handle the basic type construct.
   * 
   * @param type the target type, not null
   * @param data the data parameter passed to the visitor operation
   * @return the result of the visitor operation
   */
  T visitClassComputationTargetType(Class<? extends UniqueIdentifiable> type, D data);

}
