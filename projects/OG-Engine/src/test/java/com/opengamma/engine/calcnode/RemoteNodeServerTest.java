/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.RemoteNodeServer;
import com.opengamma.engine.calcnode.RemoteNodeServer.FunctionBlacklistMaintainerProvider;
import com.opengamma.engine.calcnode.RemoteNodeServer.FunctionBlacklistMaintainerProviderBean;
import com.opengamma.engine.calcnode.RemoteNodeServer.FunctionBlacklistQueryProvider;
import com.opengamma.engine.calcnode.RemoteNodeServer.FunctionBlacklistQueryProviderBean;
import com.opengamma.engine.calcnode.RemoteNodeServer.MultipleFunctionBlacklistMaintainerProvider;
import com.opengamma.engine.calcnode.RemoteNodeServer.MultipleFunctionBlacklistQueryProvider;
import com.opengamma.engine.calcnode.RemoteNodeServer.StaticFunctionBlacklistMaintainerProvider;
import com.opengamma.engine.calcnode.RemoteNodeServer.StaticFunctionBlacklistQueryProvider;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.EmptyFunctionBlacklist;
import com.opengamma.engine.function.blacklist.EmptyFunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklist;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklistProvider;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistRule;
import com.opengamma.engine.function.blacklist.ManageableFunctionBlacklist;
import com.opengamma.engine.function.blacklist.ManageableFunctionBlacklistProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link RemoteNodeServer} class.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteNodeServerTest {

  private final CalculationJobItem JOB_ITEM = new CalculationJobItem("1", new EmptyFunctionParameters(), ComputationTargetSpecification.NULL,
      Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS);

  // Blacklisting subclasses

  public void testStaticFunctionBlacklistMaintainerProvider() {
    final FunctionBlacklistMaintainer maintainer = Mockito.mock(FunctionBlacklistMaintainer.class);
    final StaticFunctionBlacklistMaintainerProvider provider = new StaticFunctionBlacklistMaintainerProvider(maintainer);
    assertSame(provider.getUpdate("Foo"), maintainer);
  }

  public void testFunctionBlacklistMaintainerProviderBean_emptyPolicy() {
    final FunctionBlacklistMaintainerProviderBean bean = new FunctionBlacklistMaintainerProviderBean();
    bean.setBlacklistProvider(Mockito.mock(ManageableFunctionBlacklistProvider.class));
    bean.setBlacklistPrefix("BL_");
    bean.setBlacklistPolicy(new EmptyFunctionBlacklistPolicy());
    assertNull(bean.getUpdate("Foo"));
  }

  public void testFunctionBlacklistMaintainerProviderBean_livePolicy() {
    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      final FunctionBlacklistMaintainerProviderBean bean = new FunctionBlacklistMaintainerProviderBean();
      final ManageableFunctionBlacklistProvider provider = Mockito.mock(ManageableFunctionBlacklistProvider.class);
      final ManageableFunctionBlacklist blacklist = Mockito.mock(ManageableFunctionBlacklist.class);
      Mockito.when(provider.getBlacklist("BL_Foo")).thenReturn(blacklist);
      bean.setBlacklistProvider(provider);
      bean.setBlacklistPrefix("BL_");
      bean.setBlacklistPolicy(new DefaultFunctionBlacklistPolicy(UniqueId.of("Test", "Foo"), 60, Arrays.asList(FunctionBlacklistPolicy.Entry.WILDCARD)));
      final FunctionBlacklistMaintainer maintainer = bean.getUpdate("Foo");
      maintainer.failedJobItem(JOB_ITEM);
      Mockito.verify(blacklist).addBlacklistRule(new FunctionBlacklistRule(), 60);
    } finally {
      scheduler.shutdown();
    }
  }

  public void testMultipleFunctionBlacklistMaintainerProvider_empty() {
    final MultipleFunctionBlacklistMaintainerProvider provider = new MultipleFunctionBlacklistMaintainerProvider(
        Arrays.<FunctionBlacklistMaintainerProvider>asList(new StaticFunctionBlacklistMaintainerProvider(null)));
    assertNull(provider.getUpdate("Foo"));
  }

  public void testMultipleFunctionBlacklistMaintainerProvider_full() {
    final FunctionBlacklistMaintainer a = Mockito.mock(FunctionBlacklistMaintainer.class);
    final FunctionBlacklistMaintainer b = Mockito.mock(FunctionBlacklistMaintainer.class);
    final MultipleFunctionBlacklistMaintainerProvider provider = new MultipleFunctionBlacklistMaintainerProvider(Arrays.<FunctionBlacklistMaintainerProvider>asList(
        new StaticFunctionBlacklistMaintainerProvider(null), new StaticFunctionBlacklistMaintainerProvider(a), new StaticFunctionBlacklistMaintainerProvider(b)));
    final FunctionBlacklistMaintainer m = provider.getUpdate("Foo");
    m.failedJobItem(JOB_ITEM);
    Mockito.verify(a).failedJobItem(JOB_ITEM);
    Mockito.verify(b).failedJobItem(JOB_ITEM);
  }

  public void testStaticFunctionBlacklistQueryProvider() {
    final FunctionBlacklistQuery query = Mockito.mock(FunctionBlacklistQuery.class);
    final StaticFunctionBlacklistQueryProvider provider = new StaticFunctionBlacklistQueryProvider(query);
    assertSame(provider.getQuery("Foo"), query);
  }

  public void testFunctionBlacklistQueryProviderBean() {
    final FunctionBlacklistProvider provider = Mockito.mock(FunctionBlacklistProvider.class);
    final FunctionBlacklist blacklist = new EmptyFunctionBlacklist();
    Mockito.when(provider.getBlacklist("BL_Foo")).thenReturn(blacklist);
    final FunctionBlacklistQueryProviderBean bean = new FunctionBlacklistQueryProviderBean();
    bean.setBlacklistPrefix("BL_");
    bean.setBlacklistProvider(provider);
    final FunctionBlacklistQuery query = bean.getQuery("Foo");
    assertTrue(query.isEmpty());
  }

  public void testMultipleFunctionBlacklistQueryProvider_empty() {
    final MultipleFunctionBlacklistQueryProvider provider = new MultipleFunctionBlacklistQueryProvider(
        Arrays.<FunctionBlacklistQueryProvider>asList(new StaticFunctionBlacklistQueryProvider(null)));
    assertNull(provider.getQuery("Foo"));
  }

  public void testMultipleFunctionBlacklistQueryProvider_full() {
    final FunctionBlacklistQuery a = Mockito.mock(FunctionBlacklistQuery.class);
    final FunctionBlacklistQuery b = Mockito.mock(FunctionBlacklistQuery.class);
    final MultipleFunctionBlacklistQueryProvider provider = new MultipleFunctionBlacklistQueryProvider(Arrays.<FunctionBlacklistQueryProvider>asList(
        new StaticFunctionBlacklistQueryProvider(null), new StaticFunctionBlacklistQueryProvider(a), new StaticFunctionBlacklistQueryProvider(b)));
    final FunctionBlacklistQuery q = provider.getQuery("Foo");
    Mockito.when(a.isBlacklisted(JOB_ITEM)).thenReturn(Boolean.FALSE);
    Mockito.when(b.isBlacklisted(JOB_ITEM)).thenReturn(Boolean.FALSE);
    assertFalse(q.isBlacklisted(JOB_ITEM));
    Mockito.verify(a).isBlacklisted(JOB_ITEM);
    Mockito.verify(b).isBlacklisted(JOB_ITEM);
  }

  // TODO: test the other aspects

}
