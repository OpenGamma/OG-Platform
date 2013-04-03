/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.provider.security.SecurityEnhancer;
import com.opengamma.provider.security.SecurityEnhancerRequest;
import com.opengamma.provider.security.SecurityEnhancerResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityEnhancerTest {

  private static final SimpleSecurity SECURITY1 = new SimpleSecurity("A1");
  private static final SimpleSecurity SECURITY2 = new SimpleSecurity("A2");

  @Test
  public void test_get_single() {
    final int invoked[] = new int[1];
    SecurityEnhancer test = new AbstractSecurityEnhancer() {
      @Override
      protected SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request) {
        invoked[0]++;
        SecurityEnhancerResult r = new SecurityEnhancerResult();
        r.getResultList().add(SECURITY2);
        return r;
      }
    };
    Security result = test.enhanceSecurity(SECURITY1);
    assertEquals(SECURITY2, result);
    assertEquals(1, invoked[0]);
  }

  @Test
  public void test_get_bulk() {
    final int invoked[] = new int[1];
    SecurityEnhancer test = new AbstractSecurityEnhancer() {
      @Override
      protected SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request) {
        invoked[0]++;
        return new SecurityEnhancerResult(request.getSecurities());
      }
    };
    List<Security> result = test.enhanceSecurities(Arrays.<Security>asList(SECURITY1, SECURITY2));
    assertEquals(Arrays.asList(SECURITY1, SECURITY2), result);
    assertEquals(1, invoked[0]);
  }

  @Test
  public void test_get_bulkMap() {
    final int invoked[] = new int[1];
    SecurityEnhancer test = new AbstractSecurityEnhancer() {
      @Override
      protected SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request) {
        invoked[0]++;
        return new SecurityEnhancerResult(request.getSecurities());
      }
    };
    Map<String, Security> map = new HashMap<>();
    map.put("A", SECURITY1);
    map.put("B", SECURITY2);
    Map<String, Security> result = test.enhanceSecurities(map);
    assertEquals(map, result);
    assertEquals(1, invoked[0]);
  }

  @Test
  public void test_get_request() {
    final int invoked[] = new int[1];
    SecurityEnhancer test = new AbstractSecurityEnhancer() {
      @Override
      protected SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request) {
        invoked[0]++;
        return new SecurityEnhancerResult(request.getSecurities());
      }
    };
    SecurityEnhancerRequest request = SecurityEnhancerRequest.create(SECURITY1, SECURITY2);
    SecurityEnhancerResult result = test.enhanceSecurities(request);
    assertEquals(Arrays.asList(SECURITY1, SECURITY2), result.getResultList());
    assertEquals(1, invoked[0]);
  }

}
