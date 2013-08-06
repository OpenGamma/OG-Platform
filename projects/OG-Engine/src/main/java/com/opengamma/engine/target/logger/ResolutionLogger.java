/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.id.UniqueId;

/**
 * Callback interface for recording resolutions.
 */
public interface ResolutionLogger {

  /**
   * Reports a resolution of a reference (a requirement by external identifier, or a specification representing an object identifier) to a versioned unique identifier.
   * 
   * @param reference the resolved reference, not null
   * @param resolvedId the resolved identifier, not null
   */
  void log(ComputationTargetReference reference, UniqueId resolvedId);

}
