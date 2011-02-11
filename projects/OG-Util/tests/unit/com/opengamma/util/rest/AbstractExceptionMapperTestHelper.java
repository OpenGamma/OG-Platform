/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.junit.runners.Parameterized.Parameters;

import com.opengamma.transport.jaxrs.FudgeRest;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Abstract helper for mapper tests.
 */
public abstract class AbstractExceptionMapperTestHelper {
  
  private MediaType _mediaType;
  
  public AbstractExceptionMapperTestHelper(MediaType mediaType) {
    _mediaType = mediaType;
  }

  protected void init(ExceptionMapper<?> mapper) throws Exception {
    HttpHeaders headers = mock(HttpHeaders.class);
    when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(_mediaType));
    
    Field field = AbstractExceptionMapper.class.getDeclaredField("_headers");
    field.setAccessible(true);
    field.set(mapper, headers);
  }

  protected void testResult(Response test, Status status, Throwable th) {
    assertEquals("Status: " + status.getStatusCode() + " " + status.getReasonPhrase() + "; Message: " + th.getMessage(), test.getEntity());
    assertEquals(status.getStatusCode(), test.getStatus());
    assertEquals(1, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_TYPE).size());
    assertEquals(th.getClass().getName(), test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_TYPE).get(0));
    assertEquals(1, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE).size());
    assertEquals(th.getMessage(), test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE).get(0));
  }
  
  @Parameters
  public static Collection<Object[]> getParameters() {
    final List<Object[]> params = new ArrayList<Object[]> (2);
    params.add (new Object[] {MediaType.APPLICATION_JSON_TYPE});
    params.add (new Object[] {FudgeRest.MEDIA_TYPE });
    return params;
  }

}
