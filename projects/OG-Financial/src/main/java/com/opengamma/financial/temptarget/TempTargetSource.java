/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;

/**
 * Interface to read from a repository for temporary targets. Items can be posted into the repository and then referenced by nodes within a dependency graph.
 * <p>
 * An implementation may be in-memory, or more likely backed by storage provided by a service such as a {@link ConfigMaster}.
 */
public interface TempTargetSource extends ChangeProvider {

  /**
   * Fetches an object from the repository.
   *
   * @param identifier the identifier of the object in the repository, not null
   * @return the object or null if there is no object in the repository with that identifier
   */
  TempTarget get(UniqueId identifier);

}
