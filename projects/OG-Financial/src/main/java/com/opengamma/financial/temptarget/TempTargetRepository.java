/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;

/**
 * Interface to manage a repository for temporary targets. Items can be posted into the repository and then referenced by nodes within a dependency graph.
 * <p>
 * An implementation may be in-memory, or more likely backed by storage provided by a service such as a {@link ConfigMaster}.
 */
public interface TempTargetRepository extends TempTargetSource {

  /**
   * Stores the object into the repository, allocating a new identifier for it or reusing an existing identifier if an equal object is already held.
   *
   * @param target the object to match or store, not null
   * @return the allocated unique identifier or identifier of an existing object, not null
   */
  UniqueId locateOrStore(TempTarget target);

}
