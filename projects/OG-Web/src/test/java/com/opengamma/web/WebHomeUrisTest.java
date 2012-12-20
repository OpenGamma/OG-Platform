/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import static org.mockito.Mockito.mock;

import javax.ws.rs.core.UriInfo;

import org.testng.annotations.Test;

/**
 * Test WebHomeUris.
 */
@Test
public class WebHomeUrisTest {

  public void test_constructable() {
    UriInfo uriInfo = mock(UriInfo.class);
    new WebHomeUris(uriInfo);
  }

}
