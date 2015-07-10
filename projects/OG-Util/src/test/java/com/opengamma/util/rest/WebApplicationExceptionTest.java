/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.testng.AssertJUnit.assertEquals;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test WebApplicationException.
 */
@Test(groups = TestGroup.UNIT)
public class WebApplicationExceptionTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider="mediaTypes")
  public void test_mapping(MediaType mediaType) throws Exception {
    WebApplicationException ex = new WebApplicationException(Status.CONFLICT.getStatusCode());
    WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
    init(mapper, mediaType);
    
    Response test = mapper.toResponse(ex);
    assertEquals(null, test.getEntity());
    assertEquals(Status.CONFLICT.getStatusCode(), test.getStatus());
  }

}
