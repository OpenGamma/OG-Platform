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

import com.opengamma.DataNotFoundException;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test DataNotFoundExceptionMapper.
 */
@RunWith(Parameterized.class)
public class DataNotFoundExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  /**
   * @param mediaType
   */
  public DataNotFoundExceptionMapperTest(MediaType mediaType) {
    super(mediaType);
  }

  @Test
  public void test_mapping() throws Exception {
    DataNotFoundException ex = new DataNotFoundException("Test message");
    DataNotFoundExceptionMapper mapper = new DataNotFoundExceptionMapper();
    init(mapper);
    
    Response test = mapper.toResponse(ex);
    testResult(test, Status.NOT_FOUND, ex);
  }

}
