/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test WebApplicationException.
 */
public class WebApplicationExceptionTest {

  @Test
  public void test_mapping() {
    WebApplicationException ex = new WebApplicationException(Status.CONFLICT.getStatusCode());
    Response test = new WebApplicationExceptionMapper().toResponse(ex);
    assertEquals(null, test.getEntity());
    assertEquals(Status.CONFLICT.getStatusCode(), test.getStatus());
  }

}
