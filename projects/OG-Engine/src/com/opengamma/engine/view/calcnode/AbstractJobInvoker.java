/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.Collections;

/**
 * Partial implementation of a {@link JobInvoker}.
 */
public abstract class AbstractJobInvoker implements JobInvoker {

  private final String _identifier;

  protected AbstractJobInvoker(final String identifier) {
    _identifier = identifier;
  }

  /**
   * Returns an empty capability set.
   * 
   * @return the capabilities, not {@code null}
   */
  @Override
  public Collection<Capability> getCapabilities() {
    return Collections.emptySet();
  }

  @Override
  public String getInvokerId() {
    return _identifier;
  }

  /**
   * No-op.
   * 
   * @param jobs jobs to cancel
   */
  @Override
  public void cancel(final Collection<CalculationJobSpecification> jobs) {
    // No-op
  }

  /**
   * Always returns {@code true}.
   * @param jobs jobs to query, ignored
   * @return {@code true}
   */
  @Override
  public boolean isAlive(final Collection<CalculationJobSpecification> jobs) {
    return true;
  }

  @Override
  public String toString() {
    return getInvokerId();
  }

}
