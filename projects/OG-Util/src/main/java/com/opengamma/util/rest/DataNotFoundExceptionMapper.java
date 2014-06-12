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

import com.opengamma.DataNotFoundException;

/**
 * A JAX-RS exception mapper to convert {@code DataNotFoundException} to a RESTful 404.
 */
@Provider
public class DataNotFoundExceptionMapper
    extends AbstractSpecificExceptionMapper<DataNotFoundException> {

  /**
   * Creates the mapper.
   */
  public DataNotFoundExceptionMapper() {
    super(Status.NOT_FOUND);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(DataNotFoundException exception) {
    Map<String, String> data = new HashMap<>();
    return createHtmlErrorPage("error-datanotfound.html", data);
  }

}
