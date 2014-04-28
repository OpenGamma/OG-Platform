/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.AuthorizationException;

import com.opengamma.util.auth.AuthUtils;

/**
 * A JAX-RS exception mapper to convert {@code AuthorizationException} to a RESTful 403.
 */
@Provider
public class AuthorizationExceptionMapper
    extends AbstractSpecificExceptionMapper<AuthorizationException> {

  /**
   * Creates the mapper.
   */
  public AuthorizationExceptionMapper() {
    super(Status.FORBIDDEN);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(AuthorizationException exception) {
    Map<String, String> data = new HashMap<>();
    data.put("user", AuthUtils.getSubject().isAuthenticated() ? AuthUtils.getUserName() : "Not Logged in");
    String msg = exception.getMessage();
    String permission = StringUtils.substringBetween(msg, "[", "]");
    data.put("locator", "");
    if (StringUtils.isNotEmpty(permission)) {
      data.put("message", "Required permission: " + permission);
    } else {
      buildOutputMessage(exception, data);
    }
    return createHtmlErrorPage("error-authorization.html", data);
  }

}
