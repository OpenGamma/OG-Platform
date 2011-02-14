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
 * Test IllegalArgumentExceptionMapper.
 */
@RunWith(Parameterized.class)
public class IllegalArgumentExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  /**
   * @param mediaType
   */
  public IllegalArgumentExceptionMapperTest(MediaType mediaType) {
    super(mediaType);
  }

  @Test
  public void test_mapping() throws Exception {
    IllegalArgumentException ex = new IllegalArgumentException("Test message");
    IllegalArgumentExceptionMapper mapper = new IllegalArgumentExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.BAD_REQUEST, ex);
  }

}
