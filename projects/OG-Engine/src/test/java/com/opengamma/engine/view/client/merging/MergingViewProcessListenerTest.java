/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.base.Function;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.resource.EngineResource;
import com.opengamma.engine.resource.EngineResourceManagerImpl;
import com.opengamma.engine.resource.EngineResourceManagerInternal;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MergingViewProcessListener} class.
 */
@Test(groups = TestGroup.UNIT)
public class MergingViewProcessListenerTest {

  private static final Logger s_logger = LoggerFactory.getLogger(MergingViewProcessListenerTest.class);

  private Instant _nowish;

  private Instant now() {
    if (_nowish != null) {
      _nowish = _nowish.plusMillis(1);
    } else {
      _nowish = Instant.now();
    }
    return _nowish;
  }

  public void testPassthroughFlag() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    // Default is pass-through
    assertTrue(listener.isPassThrough());
    listener.setPassThrough(false);
    assertFalse(listener.isPassThrough());
    listener.setPassThrough(true);
    assertTrue(listener.isPassThrough());
  }

  public void testCycleRetainedFlag() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    // Default is off
    assertFalse(listener.isLatestResultCycleRetained());
    listener.setLatestResultCycleRetained(true);
    assertTrue(listener.isLatestResultCycleRetained());
    listener.setLatestResultCycleRetained(false);
    assertFalse(listener.isLatestResultCycleRetained());
  }

  public void testGetUser() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final UserPrincipal user = Mockito.mock(UserPrincipal.class);
    Mockito.when(underlying.getUser()).thenReturn(user);
    assertSame(listener.getUser(), user);
  }

  public void testViewDefinitionCompiled_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final CompiledViewDefinition compiledViewDefinition = Mockito.mock(CompiledViewDefinition.class);
    listener.viewDefinitionCompiled(compiledViewDefinition, true);
    Mockito.verify(underlying).viewDefinitionCompiled(compiledViewDefinition, true);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testViewDefinitionCompiled_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final CompiledViewDefinition compiledViewDefinition = Mockito.mock(CompiledViewDefinition.class);
    listener.viewDefinitionCompiled(compiledViewDefinition, true);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).viewDefinitionCompiled(compiledViewDefinition, true);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testViewDefinitionFailed_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final Instant now = now();
    final Exception e = new OpenGammaRuntimeException("Test");
    listener.viewDefinitionCompilationFailed(now, e);
    Mockito.verify(underlying).viewDefinitionCompilationFailed(now, e);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testViewDefinitionFailed_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final Instant now = now();
    final Exception e = new OpenGammaRuntimeException("Test");
    listener.viewDefinitionCompilationFailed(now, e);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).viewDefinitionCompilationFailed(now, e);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testCycleStarted_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final ViewCycleMetadata viewCycleMetadata = Mockito.mock(ViewCycleMetadata.class);
    listener.cycleStarted(viewCycleMetadata);
    Mockito.verify(underlying).cycleStarted(viewCycleMetadata);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testCycleStarted_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final ViewCycleMetadata viewCycleMetadata = Mockito.mock(ViewCycleMetadata.class);
    listener.cycleStarted(viewCycleMetadata);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).cycleStarted(viewCycleMetadata);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testCycleCompleted_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final ViewComputationResultModel fullResult = Mockito.mock(ViewComputationResultModel.class);
    final ViewDeltaResultModel deltaResult = Mockito.mock(ViewDeltaResultModel.class);
    listener.cycleCompleted(fullResult, deltaResult);
    Mockito.verify(underlying).cycleCompleted(fullResult, deltaResult);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testCycleCompleted_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final ViewComputationResultModel fullResult = Mockito.mock(ViewComputationResultModel.class);
    final ViewDeltaResultModel deltaResult = Mockito.mock(ViewDeltaResultModel.class);
    listener.cycleCompleted(fullResult, deltaResult);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).cycleCompleted(fullResult, deltaResult);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testCycleFragmentCompleted_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final ViewComputationResultModel fullFragment = Mockito.mock(ViewComputationResultModel.class);
    final ViewDeltaResultModel deltaFragment = Mockito.mock(ViewDeltaResultModel.class);
    listener.cycleFragmentCompleted(fullFragment, deltaFragment);
    Mockito.verify(underlying).cycleFragmentCompleted(fullFragment, deltaFragment);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testCycleFragmentCompleted_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final ViewComputationResultModel fullFragment = Mockito.mock(ViewComputationResultModel.class);
    final ViewDeltaResultModel deltaFragment = Mockito.mock(ViewDeltaResultModel.class);
    listener.cycleFragmentCompleted(fullFragment, deltaFragment);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).cycleFragmentCompleted(fullFragment, deltaFragment);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testCycleExecutionFailed_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final ViewCycleExecutionOptions viewCycleExecutionOptions = Mockito.mock(ViewCycleExecutionOptions.class);
    final Exception exception = new OpenGammaRuntimeException("Test");
    listener.cycleExecutionFailed(viewCycleExecutionOptions, exception);
    Mockito.verify(underlying).cycleExecutionFailed(viewCycleExecutionOptions, exception);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testCycleExecutionFailed_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final ViewCycleExecutionOptions viewCycleExecutionOptions = Mockito.mock(ViewCycleExecutionOptions.class);
    final Exception exception = new OpenGammaRuntimeException("Test");
    listener.cycleExecutionFailed(viewCycleExecutionOptions, exception);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).cycleExecutionFailed(viewCycleExecutionOptions, exception);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testProcessCompleted_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.processCompleted();
    Mockito.verify(underlying).processCompleted();
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testProcessCompleted_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    listener.processCompleted();
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).processCompleted();
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testProcessTerminated_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.processTerminated(false);
    Mockito.verify(underlying).processTerminated(false);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testProcessTerminated_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    listener.processTerminated(false);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).processTerminated(false);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testClientShutdown_passThrough() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    final Exception e = new OpenGammaRuntimeException("Test");
    listener.clientShutdown(e);
    Mockito.verify(underlying).clientShutdown(e);
    Mockito.verifyNoMoreInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
  }

  public void testClientShutdown_queued() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final Exception e = new OpenGammaRuntimeException("Test");
    listener.clientShutdown(e);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.drain();
    Mockito.verify(underlying).clientShutdown(e);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  private ComputedValueResult result(final int n, final String v) {
    return new ComputedValueResult(new ValueSpecification(Integer.toString(n), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get()), v,
        AggregatedExecutionLog.EMPTY);
  }

  private ViewComputationResultModel fullFragment(final int n, final String v) {
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setCalculationTime(now());
    result.addValue("Default", result(n, v));
    return result;
  }

  private ViewDeltaResultModel deltaFragment(final int n, final String v) {
    final InMemoryViewDeltaResultModel result = new InMemoryViewDeltaResultModel();
    result.setCalculationTime(now());
    result.addValue("Default", result(n, v));
    return result;
  }

  private ViewComputationResultModel fullResult(final String v) {
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setCalculationTime(now());
    for (int n = 0; n < 3; n++) {
      result.addValue("Default", result(n, v));
    }
    return result;
  }

  private ViewDeltaResultModel deltaResult(final String v) {
    final InMemoryViewDeltaResultModel result = new InMemoryViewDeltaResultModel();
    result.setCalculationTime(now());
    for (int n = 0; n < 3; n++) {
      result.addValue("Default", result(n, v));
    }
    return result;
  }

  private void assertResult(final ViewResultModel model, final String v) {
    assertEquals(model.getAllResults().iterator().next().getComputedValue().getValue(), v);
  }

  public void testReset() {
    final ViewResultListener underlying = Mockito.mock(ViewResultListener.class);
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    assertEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.setPassThrough(false);
    final CompiledViewDefinition compiledViewDefinition = Mockito.mock(CompiledViewDefinition.class);
    listener.viewDefinitionCompiled(compiledViewDefinition, true);
    Mockito.verifyZeroInteractions(underlying);
    assertNotEquals(listener.getLastUpdateTimeMillis(), 0L);
    listener.reset();
    Mockito.verifyNoMoreInteractions(underlying);
  }

  @SuppressWarnings("unchecked")
  private Function<ViewResultListener, ?>[] events() {
    return new Function[] {/*0*/new ViewDefinitionCompiledCall(Mockito.mock(CompiledViewDefinition.class), true), /*1*/new CycleStartedCall(Mockito.mock(ViewCycleMetadata.class)),
    /*2*/new CycleFragmentCompletedCall(fullFragment(0, "A"), deltaFragment(0, "A")), /*3*/new CycleFragmentCompletedCall(fullFragment(1, "A"), deltaFragment(1, "A")),
    /*4*/new CycleFragmentCompletedCall(fullFragment(2, "A"), deltaFragment(2, "A")), /*5*/new CycleCompletedCall(fullResult("A"), deltaResult("A")),
    /*6*/new CycleStartedCall(Mockito.mock(ViewCycleMetadata.class)), /*7*/new CycleFragmentCompletedCall(fullFragment(0, "B"), deltaFragment(0, "B")),
    /*8*/new CycleFragmentCompletedCall(fullFragment(1, "B"), deltaFragment(1, "B")), /*9*/new CycleFragmentCompletedCall(fullFragment(2, "B"), deltaFragment(2, "B")),
    /*10*/new CycleCompletedCall(fullResult("B"), deltaResult("B")), /*11*/new ViewDefinitionCompiledCall(Mockito.mock(CompiledViewDefinition.class), true),
    /*12*/new CycleStartedCall(Mockito.mock(ViewCycleMetadata.class)), /*13*/new CycleFragmentCompletedCall(fullFragment(0, "C"), deltaFragment(0, "C")),
    /*14*/new CycleFragmentCompletedCall(fullFragment(1, "C"), deltaFragment(1, "C")), /*15*/new CycleFragmentCompletedCall(fullFragment(2, "C"), deltaFragment(2, "C")),
    /*16*/new CycleCompletedCall(fullResult("C"), deltaResult("C")), /*17*/new CycleStartedCall(Mockito.mock(ViewCycleMetadata.class)),
    /*18*/new CycleFragmentCompletedCall(fullFragment(0, "D"), deltaFragment(0, "D")),
    /*19*/new CycleExecutionFailedCall(Mockito.mock(ViewCycleExecutionOptions.class), new OpenGammaRuntimeException("Test")),
    /*20*/new ViewDefinitionCompilationFailedCall(now(), new OpenGammaRuntimeException("Test")),
    /*21*/new ViewDefinitionCompilationFailedCall(now(), new OpenGammaRuntimeException("Test")),
    /*22*/new ViewDefinitionCompilationFailedCall(now(), new OpenGammaRuntimeException("Test")),
    /*23*/new ViewDefinitionCompilationFailedCall(now(), new OpenGammaRuntimeException("Test")),
    /*24*/new ViewDefinitionCompiledCall(Mockito.mock(CompiledViewDefinition.class), true), /*25*/new CycleStartedCall(Mockito.mock(ViewCycleMetadata.class)),
    /*26*/new CycleFragmentCompletedCall(fullFragment(0, "E"), deltaFragment(0, "E")), /*27*/new CycleFragmentCompletedCall(fullFragment(1, "E"), deltaFragment(1, "E")),
    /*28*/new CycleFragmentCompletedCall(fullFragment(2, "E"), deltaFragment(2, "E")), /*29*/new CycleCompletedCall(fullResult("E"), deltaResult("E")), /*30*/new ProcessCompletedCall() };
  }

  private void testCalls(final MergingViewProcessListener listener, final Function<ViewResultListener, ?>[] calls, final int start, final int end) {
    s_logger.debug("Apply calls {} to {}", start, end);
    listener.setPassThrough(false);
    for (int i = start; i < end; i++) {
      calls[i].apply(listener);
    }
    listener.invoke(listener.setPassThrough(true));
  }

  public void testMerge1() throws InterruptedException {
    final TestViewResultListener underlying = new TestViewResultListener();
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final Function<ViewResultListener, ?>[] events = events();
    // Fragments from first cycle merged, partial data from second cycle available
    testCalls(listener, events, 0, 10);
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "A");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "A");
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "B");
    underlying.assertNoCalls();
    // As above, but without the initial view compilation and cycle started notifications
    testCalls(listener, events, 3, 10);
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "A");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "A");
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "B");
    underlying.assertNoCalls();
  }

  public void testMerge2() throws InterruptedException {
    final TestViewResultListener underlying = new TestViewResultListener();
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final Function<ViewResultListener, ?>[] events = events();
    // First cycle discarded when second completes
    testCalls(listener, events, 0, 11);
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "B");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "B");
    underlying.assertNoCalls();
    // As above, but without the initial view compilation and cycle A started notifications
    testCalls(listener, events, 3, 11);
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "B");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "B");
    underlying.assertNoCalls();
  }

  public void testMerge3() throws InterruptedException {
    final TestViewResultListener underlying = new TestViewResultListener();
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final Function<ViewResultListener, ?>[] events = events();
    // Full result from the first compilation, partial data from the second compilation
    testCalls(listener, events, 5, 16);
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "B");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "B");
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "C");
    underlying.assertNoCalls();
    // First compilation and result discarded, full data from the second compilation
    testCalls(listener, events, 5, 17);
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "C");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "C");
    underlying.assertNoCalls();
    underlying.assertNoCalls();
  }

  public void testMerge4() throws InterruptedException {
    final TestViewResultListener underlying = new TestViewResultListener();
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final Function<ViewResultListener, ?>[] events = events();
    // Everything discarded except for the final view cycle
    testCalls(listener, events, 0, 31);
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "E");
    assertResult(underlying.getCycleCompleted(0).getViewComputationResultModel(), "E");
    underlying.assertProcessCompleted();
    underlying.assertNoCalls();
  }

  public void testMerge5() throws InterruptedException {
    final TestViewResultListener underlying = new TestViewResultListener();
    final EngineResourceManagerInternal<?> cycleManager = new EngineResourceManagerImpl<EngineResource>();
    final MergingViewProcessListener listener = new MergingViewProcessListener(underlying, cycleManager);
    final Function<ViewResultListener, ?>[] events = events();
    // Partial execution of fourth cycle
    testCalls(listener, events, 16, 20);
    underlying.assertCycleStarted();
    assertResult(underlying.getCycleFragmentCompleted(0).getViewComputationResultModel(), "D");
    underlying.assertCycleExecutionFailed();
    underlying.assertNoCalls();
    // Compilation failure discards any previous results
    testCalls(listener, events, 7, 23);
    underlying.assertViewDefinitionCompilationFailed();
    underlying.assertNoCalls();
    // Successful compilation clears any error
    testCalls(listener, events, 21, 26);
    underlying.assertViewDefinitionCompiled();
    underlying.assertCycleStarted();
    underlying.assertNoCalls();
  }

}
