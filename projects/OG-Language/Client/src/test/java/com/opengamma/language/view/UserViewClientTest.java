/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link UserViewClient} class.
 */
@Test(groups = TestGroup.UNIT)
public class UserViewClientTest {

  private static class CustomData extends UserViewClientData {

    private String _value;

    public CustomData(final String value) {
      _value = value;
    }

    public String getValue() {
      return _value;
    }

  }

  private static final class Foo extends UserViewClientBinding<CustomData> {

    private int _counter;

    @Override
    protected CustomData create(final UserViewClient viewClient) {
      return new CustomData("Foo" + ++_counter);
    }
  }

  private static final class Bar extends UserViewClientBinding<CustomData> {

    private int _counter;

    @Override
    protected CustomData create(final UserViewClient viewClient) {
      return new CustomData("Bar" + ++_counter);
    }
  }

  public void testBinding() {
    final Foo foo = new Foo();
    final Bar bar = new Bar();
    UserViewClient client = new UserViewClient(null, new MockViewClient(UniqueId.of("Test", "1")), null);
    CustomData fooValue = client.getData(foo);
    assertEquals(fooValue.getValue(), "Foo1");
    CustomData barValue = client.getData(bar);
    assertEquals(barValue.getValue(), "Bar1");
    client = new UserViewClient(null, new MockViewClient(UniqueId.of("Test", "2")), null);
    fooValue = client.getData(foo);
    assertEquals(fooValue.getValue(), "Foo2");
    barValue = client.getData(bar);
    assertEquals(barValue.getValue(), "Bar2");
  }

  private static class MockViewResultListener implements ViewResultListener {

    private final StringBuilder _sb = new StringBuilder();

    @Override
    public UserPrincipal getUser() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      _sb.append("P");
    }

    @Override
    public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
      _sb.append("F");
    }

    @Override
    public void cycleStarted(ViewCycleMetadata cycleMetadata) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void processCompleted() {
      _sb.append("C");
    }

    @Override
    public void processTerminated(boolean executionInterrupted) {
      _sb.append("T");
    }

    public String getString() {
      return _sb.toString();
    }

    @Override
    public void clientShutdown(Exception e) {
    }

  }

  public void testCoreEventTracking1() {
    final MockViewClient underlying = new MockViewClient(UniqueId.of("Test", "1"));
    final UserViewClient client = new UserViewClient(null, underlying, null);
    underlying.getResultListener().viewDefinitionCompiled(null, false);
    underlying.getResultListener().processCompleted();
    final MockViewResultListener listener = new MockViewResultListener();
    client.addResultListener(listener);
    assertEquals(listener.getString(), "PC");
  }

  public void testCoreEventTracking2() {
    final MockViewClient underlying = new MockViewClient(UniqueId.of("Test", "1"));
    final UserViewClient client = new UserViewClient(null, underlying, null);
    underlying.getResultListener().viewDefinitionCompilationFailed(null, null);
    underlying.getResultListener().processTerminated(false);
    final MockViewResultListener listener = new MockViewResultListener();
    client.addResultListener(listener);
    assertEquals(listener.getString(), "FT");
  }

  public void testCoreEventTrackingReordered() {
    final MockViewClient underlying = new MockViewClient(UniqueId.of("Test", "1"));
    final UserViewClient client = new UserViewClient(null, underlying, null);
    underlying.getResultListener().viewDefinitionCompiled(null, false);
    underlying.getResultListener().viewDefinitionCompiled(null, false);
    underlying.getResultListener().viewDefinitionCompilationFailed(null, null);
    underlying.getResultListener().viewDefinitionCompiled(null, false);
    underlying.getResultListener().viewDefinitionCompilationFailed(null, null);
    underlying.getResultListener().viewDefinitionCompilationFailed(null, null);
    underlying.getResultListener().viewDefinitionCompilationFailed(null, null);
    underlying.getResultListener().processTerminated(false);
    final MockViewResultListener listener = new MockViewResultListener();
    client.addResultListener(listener);
    assertEquals(listener.getString(), "FT");
  }

}
