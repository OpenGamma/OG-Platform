/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.engine.view.ViewProcessorInternal;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.test.Timeout;

/**
 * Test the ViewProcessorManager class.
 */
public class ViewProcessorManagerTest {

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
          context.getFunctionReinitializer().reinitializeFunction(getFunctionDefinition(), UniqueIdentifier.of("Test", "Watched"));
        }

      });
    }

    @Override
    public ViewComputationCacheSource getComputationCacheSource() {
      return null;
    }

    @Override
    public JobDispatcher getComputationJobDispatcher() {
      return null;
    }

    @Override
    public DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory() {
      return null;
    }

    @Override
    public CompiledFunctionService getFunctionCompilationService() {
      return _compiledFunctionService;
    }

    @Override
    public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
      return null;
    }

    @Override
    public LiveDataClient getLiveDataClient() {
      return null;
    }

    @Override
    public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
      return null;
    }

    @Override
    public PositionSource getPositionSource() {
      return null;
    }

    @Override
    public SecuritySource getSecuritySource() {
      return null;
    }

    @Override
    public ViewInternal getView(String name, UserPrincipal credentials) {
      return null;
    }

    @Override
    public ViewPermissionProvider getViewPermissionProvider() {
      return null;
    }

    @Override
    public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
      return null;
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
    public Set<String> getViewNames() {
      return null;
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

  }

  private static class MockNotifyingMaster implements NotifyingMaster {

    private MasterChangeListener _listener;

    @Override
    public void addChangeListener(MasterChangeListener listener) {
      assertNull(_listener);
      _listener = listener;
    }

    @Override
    public void removeChangeListener(MasterChangeListener listener) {
      assertEquals(listener, _listener);
      _listener = null;
    }

    public boolean hasListener() {
      return _listener != null;
    }

    public void notifyListenerUnwatchedIdentifier() {
      _listener.added(UniqueIdentifier.of("Test", "Unwatched"));
    }

    public void notifyListenerWatchedIdentifier() {
      _listener.added(UniqueIdentifier.of("Test", "Watched"));
    }

  }

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

  @Test
  public void testBasicOperation() throws InterruptedException {
    final ViewProcessorManager vpm = new ViewProcessorManager();
    final MockViewProcessor vp = new MockViewProcessor();
    vpm.setViewProcessor(vp);
    final MockNotifyingMaster master = new MockNotifyingMaster();
    final MockVersionedSource source = new MockVersionedSource();
    vpm.setMasterAndSource(master, source);
    // Check normal startup
    vpm.start();
    assertTrue(master.hasListener());
    assertTrue(vpm.isRunning());
    assertTrue(vp.isRunning());
    Long initialId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    assertNotNull(initialId);
    VersionCorrection initialVersion = source.getVersionCorrection();
    // Notify it of a change to the master
    Thread.sleep(10);
    master.notifyListenerUnwatchedIdentifier();
    assertNull(vp.isSuspended(Timeout.standardTimeoutMillis()));
    master.notifyListenerWatchedIdentifier();
    assertTrue(vp.isSuspended(Timeout.standardTimeoutMillis()));
    VersionCorrection newVersion = source.getVersionCorrection();
    assertTrue(newVersion.getVersionAsOf().isAfter(initialVersion.getVersionAsOf()));
    Long newId = 0L;
    for (int i = 0; i < 10; i++) {
      Thread.sleep(Timeout.standardTimeoutMillis() / 10);
      newId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    }
    assertTrue(newId > initialId);
    assertFalse(vp.isSuspended(Timeout.standardTimeoutMillis()));
    // Shutdown
    vpm.stop();
    assertFalse(vpm.isRunning());
    assertFalse(vp.isRunning());
    assertFalse(master.hasListener());
  }

}
