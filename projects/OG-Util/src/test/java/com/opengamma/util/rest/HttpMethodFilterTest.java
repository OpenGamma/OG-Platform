/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Test HttpMethodFilter.
 */
@Test(groups = TestGroup.UNIT)
public class HttpMethodFilterTest {

  public void test_filter_noActionOnGet() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("GET");
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verifyNoMoreInteractions(mock);
  }

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

  public void test_filter_noActionOnPostFormOptions() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("OPTIONS"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("OPTIONS");
    verifyNoMoreInteractions(mock);
  }

  public void test_filter_noActionOnPostFormHead() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("HEAD"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("HEAD");
    verifyNoMoreInteractions(mock);
  }

  public void test_filter_noActionOnPostFormPost() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("POST"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("POST");
    verifyNoMoreInteractions(mock);
  }

  public void test_filter_noActionOnPostFormGet() {
    ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    Form form = new Form();
    form.put("method", Arrays.asList("GET"));
    when(mock.getFormParameters()).thenReturn(form);
    
    HttpMethodFilter test = new HttpMethodFilter();
    ContainerRequest result = test.filter(mock);
    
    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("GET");
    verifyNoMoreInteractions(mock);
  }

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
