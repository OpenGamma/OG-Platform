/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.apache.http.StatusLine;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom exception for returning HTTP status codes from the REST calls.
 * 
 * @author Andrew Griffin
 */
public class RestRuntimeException extends OpenGammaRuntimeException {
  
  private final String _method;
  private final RestTarget _target;
  private final int _statusCode;

  public RestRuntimeException(final String method, final RestTarget target, final int statusCode, final String statusMessage) {
    super(method + " " + target.toString () + " - " + statusCode + " " + statusMessage);
    _method = method;
    _target = target;
    _statusCode = statusCode;
  }
  
  public String getMethod () {
    return _method;
  }
  
  public RestTarget getTarget () {
    return _target;
  }
  
  public int getStatusCode () {
    return _statusCode;
  }
  
}