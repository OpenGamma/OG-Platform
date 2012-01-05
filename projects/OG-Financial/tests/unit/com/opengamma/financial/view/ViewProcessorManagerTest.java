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

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessorInternal;
import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.VersionedSource;
import com.opengamma.util.test.Timeout;

/**
 * Test the ViewProcessorManager class.
 */
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
      functions.addFunction(new MockFunction("mock", new ComputationTarget("Foo")) {

        @Override
        public void init(final FunctionCompilationContext context) {
          context.getFunctionReinitializer().reinitializeFunction(getFunctionDefinition(), UniqueId.of("Test", "Watched"));
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
    public UniqueId getUniqueId() {
      return null;
    }

    @Override
    public ViewDefinitionRepository getViewDefinitionRepository() {
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
    public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
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
    public void entityChanged(ChangeType type, UniqueId beforeId, UniqueId afterId, Instant versionInstant) {
    }

    public void notifyListenerUnwatchedIdentifier() {
      _listener.entityChanged(new ChangeEvent(ChangeType.UPDATED, UniqueId.of("Test", "Unwatched"), UniqueId.of("Test", "UnwatchedNew"), Instant.now()));
    }

    public void notifyListenerWatchedIdentifier() {
      _listener.entityChanged(new ChangeEvent(ChangeType.UPDATED, UniqueId.of("Test", "Watched"), UniqueId.of("Test", "WatchedNew"), Instant.now()));
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
  private static class MockVersionedSource implements VersionedSource {
    private final LinkedBlockingQueue<VersionCorrection> _versionCorrections = new LinkedBlockingQueue<VersionCorrection>();

    @Override
    public void setVersionCorrection(VersionCorrection versionCorrection) {
      _versionCorrections.add(versionCorrection);
    }

    public VersionCorrection getVersionCorrection() throws InterruptedException {
      return _versionCorrections.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
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
    final MockVersionedSource source = new MockVersionedSource();
    vpm.setMasterAndSource(master, source);
    // Check normal startup
    vpm.start();
    assertTrue(changeManger.hasListener());
    assertTrue(vpm.isRunning());
    assertTrue(vp.isRunning());
    Long initialId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    assertNotNull(initialId);
    VersionCorrection initialVersion = source.getVersionCorrection();
    // Notify it of a change to the master
    Thread.sleep(10);
    changeManger.notifyListenerUnwatchedIdentifier();
    assertNull(vp.isSuspended(Timeout.standardTimeoutMillis()));
    changeManger.notifyListenerWatchedIdentifier();
    assertEquals(Boolean.TRUE, vp.isSuspended(Timeout.standardTimeoutMillis()));
    VersionCorrection newVersion = source.getVersionCorrection();
    assertTrue(newVersion.getVersionAsOf().isAfter(initialVersion.getVersionAsOf()));
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
