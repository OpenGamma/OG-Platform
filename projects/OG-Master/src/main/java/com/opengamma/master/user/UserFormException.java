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
 * Exception thrown when mutating a user.
 */
@PublicSPI
public class UserFormException extends OpenGammaRuntimeException {

  /**
   * Serialization version.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The list of errors.
   */
  private final ImmutableList<UserFormError> _errors;

  /**
   * Creates an instance of the exception.
   * 
   * @param errors  the list of errors, not null
   */
  public UserFormException(List<UserFormError> errors) {
    super("User form is invalid");
    _errors = ImmutableList.copyOf(errors);
  }

  /**
   * Creates an instance of the exception.
   * 
   * @param ex  the unexpected exception, not null
   */
  public UserFormException(RuntimeException ex) {
    super("Unexpected error during user mutation", ex);
    _errors = ImmutableList.of(UserFormError.UNEXPECTED);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of errors.
   * 
   * @return the list of errors, not null
   */
  public ImmutableList<UserFormError> getErrors() {
    return _errors;
  }

  /**
   * Logs if the error is unexpected.
   * 
   * @param logger  the logger, not null
   */
  public void logUnexpected(Logger logger) {
    if (_errors.contains(UserFormError.UNEXPECTED)) {
      logger.warn(getMessage(), this);
    }
  }

}
