/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProviderGetRequest;
import com.opengamma.provider.security.SecurityProviderGetResult;
import com.opengamma.provider.security.impl.NoneFoundSecurityProvider;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test.
 */
@Test(groups="unit")
public class NoneFoundSecurityProviderTest {

  @Test
  public void test_get_single() {
    NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    assertEquals(null, test.getSecurity(ExternalIdBundle.of("A", "B")));
  }

  @Test
  public void test_get_bulk() {
    NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries> expected = new HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries>();
    assertEquals(expected, test.getSecurities(ImmutableSet.of(ExternalIdBundle.of("A", "B"))));
  }

  @Test
  public void test_get_request() {
    NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    SecurityProviderGetRequest request = SecurityProviderGetRequest.createGet(ExternalIdBundle.of("A", "B"), "FOO");
    SecurityProviderGetResult expected = new SecurityProviderGetResult();
    assertEquals(expected, test.getSecurities(request));
  }

}
