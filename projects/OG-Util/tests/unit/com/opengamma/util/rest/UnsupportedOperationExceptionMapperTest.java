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
 * Test UnsupportedOperationExceptionMapper.
 */
@RunWith(Parameterized.class)
public class UnsupportedOperationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  /**
   * @param mediaType
   */
  public UnsupportedOperationExceptionMapperTest(MediaType mediaType) {
    super(mediaType);
  }

  @Test
  public void test_mapping() throws Exception {
    UnsupportedOperationException ex = new UnsupportedOperationException("Test message");
    UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.SERVICE_UNAVAILABLE, ex);
  }

}
