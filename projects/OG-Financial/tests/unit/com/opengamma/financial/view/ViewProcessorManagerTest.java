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

import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.engine.view.ViewProcessorInternal;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.MasterChangeListener;
import com.opengamma.master.NotifyingMaster;
import com.opengamma.master.VersionedSource;
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
      _compiledFunctionService = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
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

    public boolean isSuspended(final long timeout) throws InterruptedException {
      Boolean state = _suspendState.poll(timeout, TimeUnit.MILLISECONDS);
      assertNotNull(state);
      return state;
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

    public void notifyListener() {
      _listener.added(UniqueIdentifier.of("Test", "Identifier"));
    }

  }

  private static class MockVersionedSource implements VersionedSource {

    private Instant _versionAsOf;

    @Override
    public void setVersionAsOfInstant(InstantProvider versionAsOf) {
      _versionAsOf = Instant.of(versionAsOf);
    }

    public Instant getVersionAsOf() {
      return _versionAsOf;
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
    Instant initialVersion = source.getVersionAsOf();
    // Notify it of a change to the master
    Thread.sleep(10);
    master.notifyListener();
    assertTrue(vp.isSuspended(Timeout.standardTimeoutMillis()));
    Instant newVersion = source.getVersionAsOf();
    assertTrue(newVersion.isAfter(initialVersion));
    Long newId = vp.getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    assertTrue(newId > initialId);
    assertFalse(vp.isSuspended(Timeout.standardTimeoutMillis()));
    // Shutdown
    vpm.stop();
    assertFalse(vpm.isRunning());
    assertFalse(vp.isRunning());
    assertFalse(master.hasListener());
  }

}
