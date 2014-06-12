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

import com.opengamma.DataDuplicationException;

/**
 * A JAX-RS exception mapper to convert {@code DataDuplicationException} to a RESTful 409.
 */
@Provider
public class DataDuplicationExceptionMapper
    extends AbstractSpecificExceptionMapper<DataDuplicationException> {

  /**
   * Creates the mapper.
   */
  public DataDuplicationExceptionMapper() {
    super(Status.CONFLICT);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(DataDuplicationException exception) {
    Map<String, String> data = new HashMap<>();
    return createHtmlErrorPage("error-dataduplication.html", data);
  }

}
