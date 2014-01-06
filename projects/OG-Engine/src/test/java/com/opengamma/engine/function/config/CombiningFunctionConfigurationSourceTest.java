/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CombiningFunctionConfigurationSource} class
 */
@Test(groups = TestGroup.UNIT)
public class CombiningFunctionConfigurationSourceTest {

  public void testOfNullAndEmpty() {
    final FunctionConfigurationBundle empty = new FunctionConfigurationBundle();
    assertEquals(CombiningFunctionConfigurationSource.of().getFunctionConfiguration(Instant.now()), empty);
    assertEquals(CombiningFunctionConfigurationSource.of((FunctionConfigurationSource[]) null).getFunctionConfiguration(Instant.now()), empty);
    assertEquals(CombiningFunctionConfigurationSource.of((FunctionConfigurationSource) null).getFunctionConfiguration(Instant.now()), empty);
    assertEquals(CombiningFunctionConfigurationSource.of(null, null).getFunctionConfiguration(Instant.now()), empty);
  }

  public void testOfSingleton() {
    final FunctionConfigurationBundle foo = new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Foo")));
    final FunctionConfigurationSource source = new SimpleFunctionConfigurationSource(foo);
    assertEquals(CombiningFunctionConfigurationSource.of(null, source, null), source);
  }

  public void testOfExpanded() {
    final FunctionConfigurationBundle foo = new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Foo")));
    final FunctionConfigurationBundle bar = new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Bar")));
    final FunctionConfigurationBundle cow = new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Cow")));
    final FunctionConfigurationSource foobar = CombiningFunctionConfigurationSource.of(new SimpleFunctionConfigurationSource(foo), new SimpleFunctionConfigurationSource(bar));
    assertEquals(foobar.getFunctionConfiguration(Instant.now()).getFunctions().size(), 2);
    assertEquals(((CombiningFunctionConfigurationSource) foobar).getSources().length, 2);
    final FunctionConfigurationSource all = CombiningFunctionConfigurationSource.of(foobar, new SimpleFunctionConfigurationSource(cow));
    assertEquals(all.getFunctionConfiguration(Instant.now()).getFunctions().size(), 3);
    assertEquals(((CombiningFunctionConfigurationSource) all).getSources().length, 3);
  }

  public void testGetFunctions() {
    final FunctionConfigurationSource foo = Mockito.mock(FunctionConfigurationSource.class);
    final FunctionConfigurationSource bar = Mockito.mock(FunctionConfigurationSource.class);
    final Instant t = Instant.now();
    Mockito.when(foo.getFunctionConfiguration(t)).thenReturn(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Foo"))));
    Mockito.when(bar.getFunctionConfiguration(t)).thenReturn(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Bar"))));
    assertEquals(CombiningFunctionConfigurationSource.of(foo, bar).getFunctionConfiguration(t).getFunctions().size(), 2);
  }

  public void testChangeManager() {
    final FunctionConfigurationSource foo = Mockito.mock(FunctionConfigurationSource.class);
    final FunctionConfigurationSource bar = new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(
        Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Bar"))));
    final ChangeManager fooCM = Mockito.mock(ChangeManager.class);
    Mockito.when(foo.changeManager()).thenReturn(fooCM);
    final ChangeManager cm = CombiningFunctionConfigurationSource.of(foo, bar).changeManager();
    final ChangeListener cl = Mockito.mock(ChangeListener.class);
    cm.addChangeListener(cl);
    Mockito.verify(fooCM).addChangeListener(cl);
    cm.removeChangeListener(cl);
    Mockito.verify(fooCM).removeChangeListener(cl);
  }

  public void testChangeManager_dummy() {
    final FunctionConfigurationSource foo = new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(
        Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Foo"))));
    final FunctionConfigurationSource bar = new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(
        Collections.<FunctionConfiguration>singleton(new StaticFunctionConfiguration("Bar"))));
    final ChangeManager cm = CombiningFunctionConfigurationSource.of(foo, bar).changeManager();
    assertSame(cm, DummyChangeManager.INSTANCE);
  }

}
