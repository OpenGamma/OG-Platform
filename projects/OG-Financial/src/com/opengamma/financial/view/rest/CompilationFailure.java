/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

/**
 * 
 */
public class CompilationFailure {

  private final String _details;
  private final Exception _exception;
  
  public CompilationFailure(String details, Exception exception) {
    _details = details;
    _exception = exception;
  }

  public String getDetails() {
    return _details;
  }

  public Exception getException() {
    return _exception;
  }
  
}
