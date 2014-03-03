/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class ViewProcessorManagerTest {

  //-------------------------------------------------------------------------
  private static class MockViewProcessor implements ViewProcessorInternal {
    private final CompiledFunctionService _compiledFunctionService;
    private final LinkedBlockingQueue<Boolean> _suspendState = new LinkedBlockingQueue<Boolean>();
    private boolean _running;
    private boolean _suspended;

    public MockViewProcessor() {
      final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
      _compiledFunctionService = new CompiledFunctionService(functions, new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
      functions.addFunction(new MockFunction("mock", ComputationTarget.NULL) {

        @Override
        public void init(final FunctionCompilationContext context) {
          context.getFunctionReinitializer().reinitializeFunction(getFunctionDefinition(), ObjectId.of("Test", "Watched"));
        }

      });
    }

    @Override
    public Future<Runnable> suspend(final ExecutorService executorService) {
      return executorService.submit(new Runnable() {
        @Override
        public void run() {
          synchronized (MockViewProcessor.this) {
            assertTrue(_running);
            assertFalse(_suspended);
            _suspended = true;
            _suspendState.add(Boolean.TRUE);
          }
        }
      }, (Runnable) new Runnable() {
        @Override
        public void run() {
          synchronized (MockViewProcessor.this) {
            assertTrue(_running);
            assertTrue(_suspended);
            _suspended = false;
            _suspendState.add(Boolean.FALSE);
          }
        }
      });
    }

    @Override
    public synchronized boolean isRunning() {
      return _running;
    }

    @Override
    public synchronized void start() {
      assertFalse(_running);
      _running = true;
    }

    @Override
    public synchronized void stop() {
      assertTrue(_running);
      _running = false;
    }

    public Boolean isSuspended(final long timeout) throws InterruptedException {
      return _suspendState.poll(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public ConfigSource getConfigSource() {
      return null;
    }

    @Override
    public Collection<? extends ViewProcess> getViewProcesses() {
      return null;
    }

    @Override
    public ViewProcess getViewProcess(UniqueId viewProcessId) {
      return null;
    }

    @Override
    public Collection<ViewClient> getViewClients() {
      return null;
    }

    @Override
    public ViewClient createViewClient(UserPrincipal clientUser) {
      return null;
    }

    @Override
    public ViewClient getViewClient(UniqueId clientId) {
      return null;
    }

    @Override
    public CompiledFunctionService getFunctionCompilationService() {
      return _compiledFunctionService;
    }

    @Override
    public ViewProcessorEventListenerRegistry getViewProcessorEventListenerRegistry() {
      return null;
    }

    @Override
    public EngineResourceManager<ViewCycle> getViewCycleManager() {
      return null;
    }

    @Override
    public InMemoryNamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
      return null;
    }

  }

  //-------------------------------------------------------------------------
  private static final class MockChangeManager implements ChangeManager {
    private ChangeListener _listener;

    @Override
    public void addChangeListener(ChangeListener listener) {
      assertNull(_listener);
      _listener = listener;
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
      assertEquals(listener, _listener);
      _listener = null;
    }

    public boolean hasListener() {
      return _listener != null;
    }

    @Override
    public void entityChanged(ChangeType type, ObjectId oid, Instant versionFrom, Instant versionTo, Instant versionInstant) {
    }

    public void notifyListenerUnwatchedIdentifier() {
      _listener.entityChanged(new ChangeEvent(ChangeType.CHANGED, ObjectId.of("Test", "Unwatched"), null, null, Instant.now()));
    }

    public void notifyListenerWatchedIdentifier() {
      _listener.entityChanged(new ChangeEvent(ChangeType.CHANGED, ObjectId.of("Test", "Watched"), null, null, Instant.now()));
    }
  }

  //-------------------------------------------------------------------------
  private static class MockNotifyingMaster implements ChangeProvider {
    private ChangeManager _changeManager = new MockChangeManager();

    @Override
    public ChangeManager changeManager() {
      return _changeManager;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void testBasicOperation() throws InterruptedException {
    final ViewProcessorManager vpm = new ViewProcessorManager();
    final MockViewProcessor vp = new MockViewProcessor();
    vpm.setViewProcessor(vp);
    final MockNotifyingMaster master = new MockNotifyingMaster();
    final MockChangeManager changeManger = (MockChangeManager) master.changeManager();
    vpm.setMaster(master);
    // Check normal startup
    vpm.start();
    assertTrue(changeManger.hasListener());
    assertTrue(vpm.isRunning());
    assertTrue(vp.isRunning());
    Long initialId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    assertNotNull(initialId);
    // Notify it of a change to the master
    Thread.sleep(10);
    changeManger.notifyListenerUnwatchedIdentifier();
    assertNull(vp.isSuspended(Timeout.standardTimeoutMillis()));
    changeManger.notifyListenerWatchedIdentifier();
    assertEquals(Boolean.TRUE, vp.isSuspended(Timeout.standardTimeoutMillis()));
    Long newId = 0L;
    for (int i = 0; i < 10; i++) {
      Thread.sleep(Timeout.standardTimeoutMillis() / 10);
      newId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    }
    assertTrue(newId > initialId);
    assertEquals(Boolean.FALSE, vp.isSuspended(Timeout.standardTimeoutMillis()));
    // Shutdown
    vpm.stop();
    assertFalse(vpm.isRunning());
    assertFalse(vp.isRunning());
    assertFalse(changeManger.hasListener());
  }

}
