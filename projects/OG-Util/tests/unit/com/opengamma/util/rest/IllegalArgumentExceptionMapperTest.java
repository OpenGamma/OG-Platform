/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test IllegalArgumentExceptionMapper.
 */
public class IllegalArgumentExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test
  public void test_mapping() throws Exception {
    IllegalArgumentException ex = new IllegalArgumentException("Test message");
    IllegalArgumentExceptionMapper mapper = new IllegalArgumentExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.BAD_REQUEST, ex);
  }

}
