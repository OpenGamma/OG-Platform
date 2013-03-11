/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;


/**
 * Interface for an implementation that can specify certain capabilities for jobs so that
 * their allocation to {@link JobInvoker} instances by the {@link JobDispatcher} can be
 * more closely controlled.
 */
public interface CapabilityRequirementsProvider {

  /**
   * Returns the {@link CapabilityRequirements} object for the job. The object will never be modified by the caller so the same instance can be returned for multiple jobs.
   * 
   * @param job job details
   * @return the requirements, not null.
   */
  CapabilityRequirements getCapabilityRequirements(CalculationJob job);

}

