/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertNotNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;

/**
 * Tests the {@link FunctionBlacklistMaintainerFactoryBean} class.
 */
@Test
public class FunctionBlacklistMaintainerFactoryBeanTest {

  public void testDirectConstruction() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setFunctionBlacklist(Mockito.mock(ManageableFunctionBlacklist.class));
    bean.setPolicy(Mockito.mock(FunctionBlacklistPolicy.class));
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testMissingBlacklist() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setPolicy(Mockito.mock(FunctionBlacklistPolicy.class));
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testMissingPolicy() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setFunctionBlacklist(Mockito.mock(ManageableFunctionBlacklist.class));
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  public void testBlacklistProvider_byName() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setPolicy(Mockito.mock(FunctionBlacklistPolicy.class));
    final ManageableFunctionBlacklistProvider blacklists = Mockito.mock(ManageableFunctionBlacklistProvider.class);
    final ManageableFunctionBlacklist blacklist = Mockito.mock(ManageableFunctionBlacklist.class);
    Mockito.when(blacklists.getBlacklist("Foo")).thenReturn(blacklist);
    bean.setFunctionBlacklistSource(blacklists);
    bean.setFunctionBlacklistName("Foo");
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testBlacklistProvider_missing() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setPolicy(Mockito.mock(FunctionBlacklistPolicy.class));
    final ManageableFunctionBlacklistProvider blacklists = Mockito.mock(ManageableFunctionBlacklistProvider.class);
    Mockito.when(blacklists.getBlacklist("Foo")).thenReturn(null);
    bean.setFunctionBlacklistSource(blacklists);
    bean.setFunctionBlacklistName("Foo");
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  public void testPolicySource_byName() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setFunctionBlacklist(Mockito.mock(ManageableFunctionBlacklist.class));
    final FunctionBlacklistPolicySource policies = Mockito.mock(FunctionBlacklistPolicySource.class);
    final FunctionBlacklistPolicy policy = Mockito.mock(FunctionBlacklistPolicy.class);
    Mockito.when(policies.getPolicy("Foo")).thenReturn(policy);
    bean.setPolicyName("Foo");
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  public void testPolicySource_byUniqueId() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setFunctionBlacklist(Mockito.mock(ManageableFunctionBlacklist.class));
    final FunctionBlacklistPolicySource policies = Mockito.mock(FunctionBlacklistPolicySource.class);
    final FunctionBlacklistPolicy policy = Mockito.mock(FunctionBlacklistPolicy.class);
    Mockito.when(policies.getPolicy(UniqueId.of("Test", "Foo"))).thenReturn(policy);
    bean.setPolicySource(policies);
    bean.setPolicyUniqueId(UniqueId.of("Test", "Foo"));
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testPolicySource_missing() {
    final FunctionBlacklistMaintainerFactoryBean bean = new FunctionBlacklistMaintainerFactoryBean();
    bean.setFunctionBlacklist(Mockito.mock(ManageableFunctionBlacklist.class));
    final FunctionBlacklistPolicySource policies = Mockito.mock(FunctionBlacklistPolicySource.class);
    Mockito.when(policies.getPolicy("Foo")).thenReturn(null);
    bean.setPolicySource(policies);
    bean.setPolicyName("Foo");
    bean.afterPropertiesSet();
    assertNotNull(bean.getObject());
  }

}
