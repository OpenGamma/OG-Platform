/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

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
   * @param jobSpec job spec details, e.g. the capability requirements might depend on the configuration or view name
   * @param items job item details, e.g. the capability requirements might depend on what is being calculated
   * @return the requirements, not {@code null}.
   */
  CapabilityRequirements getCapabilityRequirements(CalculationJobSpecification jobSpec, List<CalculationJobItem> items);

}

