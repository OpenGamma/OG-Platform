/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
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
import com.opengamma.livedata.UserPrincipal;

/**
 * Tests the {@link UserViewClient} class.
 */
@Test
public class UserViewClientTest {

  private static class MockViewClient implements ViewClient {

    private final UniqueId _identifier;

    public MockViewClient(final UniqueId identifier) {
      _identifier = identifier;
    }

    @Override
    public void attachToViewProcess(String viewDefinitionName, ViewExecutionOptions executionOptions) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void attachToViewProcess(String viewDefinitionName, ViewExecutionOptions executionOptions, boolean newPrivateProcess) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void attachToViewProcess(UniqueId processId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public EngineResourceReference<? extends ViewCycle> createCycleReference(UniqueId cycleId) {
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
    public void setResultListener(ViewResultListener resultListener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setResultMode(ViewResultMode viewResultMode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setUpdatePeriod(long periodMillis) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
      throw new UnsupportedOperationException();
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

}
