/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.Collections;

/**
 * Abstract job invoker that provides default implementations for most methods.
 */
public abstract class AbstractJobInvoker implements JobInvoker {

  /**
   * The identifier.
   */
  private final String _identifier;

  /**
   * Creates an instance.
   * 
   * @param identifier  the identifier, not null
   */
  protected AbstractJobInvoker(final String identifier) {
    _identifier = identifier;
  }

  /**
   * Default implementation that returns an empty set.
   * 
   * @return the capabilities, not null
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
   * Default implementation that does nothing.
   * 
   * @param jobs  the jobs to cancel, not null
   */
  @Override
  public void cancel(final Collection<CalculationJobSpecification> jobs) {
    // do nothing
  }

  /**
   * Default implementation of alive always returning true.
   * 
   * @param jobs  the jobs to query, ignored
   * @return true always
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
