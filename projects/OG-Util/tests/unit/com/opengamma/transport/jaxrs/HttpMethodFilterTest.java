/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Test HttpMethodFilter.
 */
public class HttpMethodFilterTest {

  @Test
  public void test_filter_noActionOnGet() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("GET");
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verifyNoMoreInteractions(mock);
  }

  @Test
  public void test_filter_noActionOnPostNoForm() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    when(mock.getFormParameters()).thenReturn(new Form());
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verifyNoMoreInteractions(mock);
  }

  @Test
  public void test_filter_noActionOnPostFormPut() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("PUT"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("PUT");
    verifyNoMoreInteractions(mock);
  }

  @Test
  public void test_filter_noActionOnPostFormDelete() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("DELETE"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("DELETE");
    verifyNoMoreInteractions(mock);
  }

  @Test
  public void test_filter_noActionOnPostFormNoMatch() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("FOOBAR"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verifyNoMoreInteractions(mock);
  }

}
