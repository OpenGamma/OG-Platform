/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

/**
 * Interface for an implementation that can specify certain capabilities for jobs so that
 * their allocation to {@link JobInvoker} instances by the {@link JobDispatcher} can be
 * more closely controlled.
 */
public interface CapabilityRequirementsProvider {

  /**
   * Returns the {@link CapabilityRequirements} object for the job. The object
   * will never be modified by the caller so the same instance can be returned for multiple
   * jobs.
   * 
   * @param jobs job details
   * @return the requirements, not {@code null}.
   */
  CapabilityRequirements getCapabilityRequirements(Collection<CalculationJob> jobs);

}

