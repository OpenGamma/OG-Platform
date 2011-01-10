/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test UnsupportedOperationExceptionMapper.
 */
public class UnsupportedOperationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test
  public void test_mapping() throws Exception {
    UnsupportedOperationException ex = new UnsupportedOperationException("Test message");
    UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.SERVICE_UNAVAILABLE, ex);
  }

}
