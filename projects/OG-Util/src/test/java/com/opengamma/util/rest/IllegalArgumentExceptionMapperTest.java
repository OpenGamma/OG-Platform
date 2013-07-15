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
 * Test IllegalArgumentExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class IllegalArgumentExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    IllegalArgumentException ex = new IllegalArgumentException("Test message");
    IllegalArgumentExceptionMapper mapper = new IllegalArgumentExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.BAD_REQUEST, ex);
  }

}
