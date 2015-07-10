/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.DataDuplicationException;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test DataDuplicationExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class DataDuplicationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    DataDuplicationException ex = new DataDuplicationException("Test message");
    DataDuplicationExceptionMapper mapper = new DataDuplicationExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.CONFLICT, ex);
  }

}
