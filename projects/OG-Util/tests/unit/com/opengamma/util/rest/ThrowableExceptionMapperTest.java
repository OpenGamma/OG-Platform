/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test ThrowableExceptionMapper.
 */
@RunWith(Parameterized.class)
public class ThrowableExceptionMapperTest extends AbstractExceptionMapperTestHelper {
  
  /**
   * @param mediaType
   */
  public ThrowableExceptionMapperTest(MediaType mediaType) {
    super(mediaType);
  }

  @Test
  public void test_mapping() throws Exception {
    NullPointerException ex = new NullPointerException("Test message");
    ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.INTERNAL_SERVER_ERROR, ex);
  }

}
