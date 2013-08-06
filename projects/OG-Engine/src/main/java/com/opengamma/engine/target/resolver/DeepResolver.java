/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Specialised form of resolver that performs "deep" resolution based on a version/correction timestamp in addition to the primary resolution of the top most object. For example, {@link Position} and
 * any other portfolio structure objects gets deep resolved so that the they have securities attached to them rather than just the identifiers that users must then resolve.
 */
public interface DeepResolver {

  /**
   * Wrap an object instance in an equivalent form that will report any deep resolutions to the supplied logger.
   * 
   * @param underlying the object to wrap, not null
   * @param logger the logging service, not null
   * @return the wrapped object or null if it is not possible to wrap it
   */
  UniqueIdentifiable withLogger(UniqueIdentifiable underlying, ResolutionLogger logger);

}
