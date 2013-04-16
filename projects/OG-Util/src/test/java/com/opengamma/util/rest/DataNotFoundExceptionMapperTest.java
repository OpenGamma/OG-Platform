/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test DataNotFoundExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class DataNotFoundExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    DataNotFoundException ex = new DataNotFoundException("Test message");
    DataNotFoundExceptionMapper mapper = new DataNotFoundExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.NOT_FOUND, ex);
  }

}
