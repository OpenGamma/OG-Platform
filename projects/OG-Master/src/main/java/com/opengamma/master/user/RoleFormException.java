/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.PublicSPI;

/**
 * Exception thrown when mutating a role.
 */
@PublicSPI
public class RoleFormException extends OpenGammaRuntimeException {

  /**
   * Serialization version.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The list of errors.
   */
  private final ImmutableList<RoleFormError> _errors;

  /**
   * Creates an instance of the exception.
   * 
   * @param errors  the list of errors, not null
   */
  public RoleFormException(List<RoleFormError> errors) {
    super("Role form is invalid");
    _errors = ImmutableList.copyOf(errors);
  }

  /**
   * Creates an instance of the exception.
   * 
   * @param ex  the unexpected exception, not null
   */
  public RoleFormException(RuntimeException ex) {
    super("Unexpected error during role mutation", ex);
    _errors = ImmutableList.of(RoleFormError.UNEXPECTED);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of errors.
   * 
   * @return the list of errors, not null
   */
  public ImmutableList<RoleFormError> getErrors() {
    return _errors;
  }

  /**
   * Logs if the error is unexpected.
   * 
   * @param logger  the logger, not null
   */
  public void logUnexpected(Logger logger) {
    if (_errors.contains(RoleFormError.UNEXPECTED)) {
      logger.warn(getMessage(), this);
    }
  }

}
