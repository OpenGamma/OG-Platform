/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test UnsupportedOperationExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class UnsupportedOperationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    UnsupportedOperationException ex = new UnsupportedOperationException("Test message");
    UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.SERVICE_UNAVAILABLE, ex);
  }

}
