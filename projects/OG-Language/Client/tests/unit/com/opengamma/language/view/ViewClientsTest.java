/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.LiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.context.AbstractSessionContextEventHandler;
import com.opengamma.language.context.AbstractUserContextEventHandler;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextEventHandler;
import com.opengamma.language.context.UserContextEventHandler;
import com.opengamma.language.test.TestUtils;
import com.opengamma.livedata.UserPrincipal;

/**
 * Tests the {@link ViewClients} class and its subclasses.
 */
@Test
public class ViewClientsTest {

  private static final String VIEW_NAME = "Test View";
  
  private static class MockViewProcessor implements ViewProcessor {

    private final AtomicInteger _nextId = new AtomicInteger();

    @Override
    public ViewClient createViewClient(final UserPrincipal clientUser) {
      return new MockViewClient(UniqueId.of("Test", Integer.toString(_nextId.getAndIncrement())));
    }

    @Override
    public UniqueId getUniqueId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewClient getViewClient(final UniqueId clientId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public EngineResourceManager<? extends ViewCycle> getViewCycleManager() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewDefinitionRepository getViewDefinitionRepository() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewProcess getViewProcess(final UniqueId viewProcessId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
      throw new UnsupportedOperationException();
    }
  }
  
  private static class MockViewClient implements ViewClient {

    private final UniqueId _identifier;
    private String _attachedViewDefinitionName;
    private boolean _shutdown;

    public MockViewClient(final UniqueId identifier) {
      _identifier = identifier;
    }

    public String getAttachedViewDefinitionName() {
      return _attachedViewDefinitionName;
    }

    public boolean isShutdown() {
      return _shutdown;
    }

    @Override
    public void attachToViewProcess(final String viewDefinitionName, final ViewExecutionOptions executionOptions) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void attachToViewProcess(final String viewDefinitionName, final ViewExecutionOptions executionOptions, final boolean newPrivateProcess) {
      _attachedViewDefinitionName = viewDefinitionName;
    }

    @Override
    public void attachToViewProcess(final UniqueId processId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public EngineResourceReference<? extends ViewCycle> createCycleReference(final UniqueId cycleId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public EngineResourceReference<? extends ViewCycle> createLatestCycleReference() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void detachFromViewProcess() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompiledViewDefinition getLatestCompiledViewDefinition() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewComputationResultModel getLatestResult() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewDefinition getLatestViewDefinition() {
      throw new UnsupportedOperationException();
    }

    @Override
    public MarketDataInjector getLiveDataOverrideInjector() {
      throw new UnsupportedOperationException();
    }

    @Override
    public VersionCorrection getProcessVersionCorrection() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewResultMode getResultMode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewClientState getState() {
      throw new UnsupportedOperationException();
    }

    @Override
    public UniqueId getUniqueId() {
      return _identifier;
    }

    @Override
    public UserPrincipal getUser() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ViewProcessor getViewProcessor() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAttached() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCompleted() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResultAvailable() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isViewCycleAccessSupported() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void pause() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setResultListener(final ViewResultListener resultListener) {
      // Ignore
    }

    @Override
    public void setResultMode(final ViewResultMode viewResultMode) {
      assertEquals(viewResultMode, ViewResultMode.DELTA_ONLY);
    }

    @Override
    public void setUpdatePeriod(final long periodMillis) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setViewCycleAccessSupported(final boolean isViewCycleAccessSupported) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
      assertFalse(_shutdown);
      _shutdown = true;
    }

    @Override
    public void triggerCycle() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
      throw new UnsupportedOperationException();
    }
  }

  private TestUtils createTestUtils() {
    final TestUtils testUtils = new TestUtils() {
      
      @Override
      protected UserContextEventHandler createUserContextEventHandler() {
        return new AbstractUserContextEventHandler (super.createUserContextEventHandler ()) {
          
          @Override
          protected void initContextImpl(final MutableUserContext context) {
            context.setViewClients(new UserViewClients (context));
          }
          
          @Override
          protected void doneContextImpl (final MutableUserContext context) {
            context.getViewClients ().destroyAll();
          }
          
        };
      }

      @Override
      protected SessionContextEventHandler createSessionContextEventHandler() {
        return new AbstractSessionContextEventHandler (super.createSessionContextEventHandler ()) {
          
          @Override
          protected void initContextImpl(final MutableSessionContext context) {
            context.setViewClients(new SessionViewClients (context));
          }
          
          @Override
          protected void doneContextImpl(final MutableSessionContext context) {
            context.getViewClients ().destroyAll ();
          }
          
        };
      }
      
    };
    testUtils.setViewProcessor(new MockViewProcessor ());
    return testUtils;
  }
  
  public void testLockView () {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle1 = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(VIEW_NAME, true));
    final MockViewClient viewClient1 = (MockViewClient) handle1.get().getViewClient();
    assertEquals(viewClient1.getAttachedViewDefinitionName(), VIEW_NAME);
    final ViewClientHandle handle2 = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(VIEW_NAME, true));
    final MockViewClient viewClient2 = (MockViewClient) handle2.get().getViewClient();
    assertSame(viewClient2, viewClient1);
    handle1.unlock();
    assertFalse(viewClient1.isShutdown());
    handle2.unlock();
    assertTrue(viewClient1.isShutdown());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testUnlockUnlock() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(VIEW_NAME, true));
    handle.unlock();
    handle.unlock();
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testUnlockGet() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(VIEW_NAME, true));
    handle.unlock();
    handle.get();
  }

  public void testDetachView() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle1 = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(VIEW_NAME, true));
    final MockViewClient viewClient1 = (MockViewClient) handle1.get().getViewClient();
    assertEquals(viewClient1.getAttachedViewDefinitionName(), VIEW_NAME);
    final UniqueId uid = handle1.detachAndUnlock(context);
    assertFalse(viewClient1.isShutdown());
    final ViewClientHandle handle2 = context.getViewClients().lockViewClient(uid);
    final MockViewClient viewClient2 = (MockViewClient) handle2.get().getViewClient();
    assertSame(viewClient1, viewClient2);
    handle2.unlock();
    assertFalse(viewClient1.isShutdown());
    final DetachedViewClientHandle handle3 = context.getViewClients().lockViewClient(uid);
    handle3.attachAndUnlock();
    assertTrue(viewClient1.isShutdown());
    final DetachedViewClientHandle handle4 = context.getViewClients().lockViewClient(uid);
    assertNull(handle4);
  }

}
