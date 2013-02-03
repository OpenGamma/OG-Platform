/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

import com.opengamma.id.ObjectId;
import com.opengamma.util.PublicAPI;

/**
 * Re-initialization hook for functions. When a function is being initialized, it can register itself
 * for re-initialization if changes are detected to one or more unique identifiers. The function
 * repository may then be partially re-initialized under certain conditions - for example on changes to
 * the configuration data.
 */
@PublicAPI
public interface FunctionReinitializer {

  /**
   * Requests that the function be reinitialized if a change is detected to the resource identified.
   * 
   * @param function the function to be reinitialized
   * @param identifier the identifier of the resource
   */
  void reinitializeFunction(FunctionDefinition function, ObjectId identifier);

  /**
   * Requests that the function be reinitialized if a change is detected to one or more of the resources
   * identified.
   * 
   * @param function the function to be reinitialized
   * @param identifiers the identifiers of the resources
   */
  void reinitializeFunction(FunctionDefinition function, Collection<ObjectId> identifiers);

}
