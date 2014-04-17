/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, enabled = false)
public class JsonBeanStructureVisitorTest {

  static {
    JodaBeanConverters.getInstance();
  }

  @Test(enabled = false)
  public void fxForward() {
    Set<MetaBean> metaBeans = ImmutableSet.<MetaBean>of(FXForwardSecurity.meta(), ExternalIdBundle.meta());
    JsonBeanStructureVisitor visitor = new JsonBeanStructureVisitor(metaBeans);
    System.out.println(new BeanTraverser().traverse(FXForwardSecurity.meta(), visitor));
  }

  @Test(enabled = false)
  public void swap() {
    Set<MetaBean> metaBeans = ImmutableSet.<MetaBean>of(
        SwapSecurity.meta(),
        SwapLeg.meta(),
        FixedInterestRateLeg.meta(),
        FloatingInterestRateLeg.meta(),
        InterestRateNotional.meta(),
        ExternalIdBundle.meta());
    JsonBeanStructureVisitor visitor = new JsonBeanStructureVisitor(metaBeans);
    System.out.println(new BeanTraverser().traverse(SwapSecurity.meta(), visitor));
    System.out.println(new BeanTraverser().traverse(FixedInterestRateLeg.meta(), visitor));
  }

}
