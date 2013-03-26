/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy.Entry;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FunctionBlacklistPolicyFactoryBean} class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionBlacklistPolicyFactoryBeanTest {

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testNoUniqueIdOrName() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName(null);
    bean.afterPropertiesSet();
  }

  public void testDefaultName() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.afterPropertiesSet();
    assertNotNull(bean.getName());
  }

  public void testNoName() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setUniqueId(UniqueId.of("Test", "Foo"));
    bean.setName(null);
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getUniqueId(), UniqueId.of("Test", "Foo"));
    assertEquals(policy.getName(), "Foo");
    assertTrue(policy.getEntries().isEmpty());
  }

  public void testNoUniqueId() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getUniqueId(), UniqueId.of("com.opengamma.engine.function.blacklist", "Foo"));
    assertEquals(policy.getName(), "Foo");
    assertTrue(policy.getEntries().isEmpty());
  }

  public void testDefaultObjects() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.setDefaultEntryActivationPeriod(60);
    bean.setWildcard(true);
    assertTrue(bean.isWildcard());
    bean.setFunction(false);
    assertFalse(bean.isFunction());
    bean.setParameterizedFunctionActivationPeriod(1000);
    assertTrue(bean.isParameterizedFunction());
    bean.setPartialNode(false);
    assertFalse(bean.isPartialNode());
    bean.setBuildNode(true);
    assertTrue(bean.isBuildNode());
    bean.setExecutionNode(false);
    assertFalse(bean.isExecutionNode());
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    assertEquals(policy.getDefaultEntryActivationPeriod(), 60);
    final Set<Entry> entries = policy.getEntries();
    assertEquals(entries.size(), 3);
    assertTrue(entries.contains(Entry.WILDCARD));
    assertTrue(entries.contains(Entry.PARAMETERIZED_FUNCTION.activationPeriod(1000)));
    assertTrue(entries.contains(Entry.BUILD_NODE));
  }

  public void testArbitraryEntries() {
    final FunctionBlacklistPolicyFactoryBean bean = new FunctionBlacklistPolicyFactoryBean();
    bean.setName("Foo");
    bean.setWildcard(true);
    bean.setEntries(Arrays.asList(Entry.BUILD_NODE, Entry.PARTIAL_NODE));
    bean.afterPropertiesSet();
    final FunctionBlacklistPolicy policy = bean.getObject();
    final Set<Entry> entries = policy.getEntries();
    assertEquals(entries.size(), 3);
    assertTrue(entries.contains(Entry.WILDCARD));
    assertTrue(entries.contains(Entry.PARTIAL_NODE));
    assertTrue(entries.contains(Entry.BUILD_NODE));
  }

}
