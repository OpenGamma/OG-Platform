/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test ThrowableExceptionMapper.
 */
@Test
public class ThrowableExceptionMapperTest extends AbstractExceptionMapperTestHelper {
  
  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    NullPointerException ex = new NullPointerException("Test message");
    ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.INTERNAL_SERVER_ERROR, ex);
  }

}
