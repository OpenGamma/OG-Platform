/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;

import org.joda.beans.MetaBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BeanHierarchyTest {

  @Test
  public void oneMetaBean() {
    BeanHierarchy hierarchy = new BeanHierarchy(Sets.<MetaBean>newHashSet(FXForwardSecurity.meta()));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(FinancialSecurity.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(ManageableSecurity.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(Object.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(FXForwardSecurity.class));
  }

  @Test
  public void multipleMetaBeansOneHierarchy() {
    BeanHierarchy hierarchy = new BeanHierarchy(Sets.<MetaBean>newHashSet(ManageableSecurity.meta(), FXForwardSecurity.meta()));
    assertEquals(ImmutableSet.of(ManageableSecurity.class, FXForwardSecurity.class), hierarchy.subtypes(Object.class));
    assertEquals(ImmutableSet.of(ManageableSecurity.class, FXForwardSecurity.class), hierarchy.subtypes(ManageableSecurity.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(FXForwardSecurity.class));
  }

  @Test
  public void abstractBean() {
    BeanHierarchy hierarchy = new BeanHierarchy(Sets.<MetaBean>newHashSet(FinancialSecurity.meta(), FXForwardSecurity.meta()));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(Object.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(ManageableSecurity.class));
    assertEquals(ImmutableSet.of(FXForwardSecurity.class), hierarchy.subtypes(FinancialSecurity.class));
  }
}
