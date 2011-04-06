/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom exception for returning HTTP status codes from the REST calls.
 */
public class RestRuntimeException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The RESTful method.
   */
  private final String _method;
  /**
   * The RESTful target.
   */
  private final RestTarget _target;
  /**
   * The resulting status code.
   */
  private final int _statusCode;

  /**
   * Creates an exception.
   * @param method  the RESTful method
   * @param target  the RESTful target
   * @param statusCode  the status code
   * @param statusMessage  the status message
   */
  public RestRuntimeException(final String method, final RestTarget target, final int statusCode, final String statusMessage) {
    super(method + " " + target + " - " + statusCode + " " + statusMessage);
    _method = method;
    _target = target;
    _statusCode = statusCode;
  }

  //-------------------------------------------------------------------------  
  public String getMethod() {
    return _method;
  }

  public RestTarget getTarget() {
    return _target;
  }

  public int getStatusCode() {
    return _statusCode;
  }

}
