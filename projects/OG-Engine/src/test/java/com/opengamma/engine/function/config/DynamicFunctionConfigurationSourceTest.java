/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DynamicFunctionConfigurationSource} class.
 */
@Test(groups = TestGroup.UNIT)
public class DynamicFunctionConfigurationSourceTest {

  private static class Configuration extends VersionedFunctionConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(MockFunction.class, getVersionCorrection().toString()));
    }

  }

  public void testCreate() {
    final DynamicFunctionConfigurationSource source = new BeanDynamicFunctionConfigurationSource(DummyChangeManager.INSTANCE) {

      @Override
      protected boolean isPropogateEvent(ChangeEvent event) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        return new Configuration();
      }

    };
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final FunctionConfigurationBundle bundle1 = source.getFunctionConfiguration(t1);
    assertEquals(((ParameterizedFunctionConfiguration) bundle1.getFunctions().get(0)).getParameter().get(0), VersionCorrection.of(t1, t1).toString());
    final FunctionConfigurationBundle bundle2 = source.getFunctionConfiguration(t2);
    assertEquals(((ParameterizedFunctionConfiguration) bundle2.getFunctions().get(0)).getParameter().get(0), VersionCorrection.of(t2, t2).toString());
  }

  public void testChangeManagerListeners() {
    final ChangeManager cm = Mockito.mock(ChangeManager.class);
    final DynamicFunctionConfigurationSource source = new BeanDynamicFunctionConfigurationSource(cm) {

      @Override
      protected boolean isPropogateEvent(ChangeEvent event) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        throw new UnsupportedOperationException();
      }

    };
    final ChangeListener l1 = Mockito.mock(ChangeListener.class);
    final ChangeListener l2 = Mockito.mock(ChangeListener.class);
    Mockito.verify(cm, Mockito.never()).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    source.changeManager().addChangeListener(l1);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    source.changeManager().addChangeListener(l2);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    source.changeManager().removeChangeListener(l1);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    source.changeManager().removeChangeListener(l2);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.times(1)).removeChangeListener(Mockito.<ChangeListener>any());
    Mockito.verifyNoMoreInteractions(cm);
  }

  public void testChangeManagerNotifications() {
    final ChangeManager cm = new BasicChangeManager();
    final DynamicFunctionConfigurationSource source = new BeanDynamicFunctionConfigurationSource(cm) {

      @Override
      protected boolean isPropogateEvent(ChangeEvent event) {
        return event.getObjectId().getValue().equals("Bar");
      }

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        throw new UnsupportedOperationException();
      }

    };
    final ChangeListener l = Mockito.mock(ChangeListener.class);
    source.changeManager().addChangeListener(l);
    Mockito.verifyZeroInteractions(l);
    final Instant now = Instant.now();
    cm.entityChanged(ChangeType.ADDED, ObjectId.of("Test", "Foo"), now, now, now);
    cm.entityChanged(ChangeType.CHANGED, ObjectId.of("Test", "Foo"), now, now, now);
    cm.entityChanged(ChangeType.REMOVED, ObjectId.of("Test", "Foo"), now, now, now);
    Mockito.verifyZeroInteractions(l);
    cm.entityChanged(ChangeType.ADDED, ObjectId.of("Test", "Bar"), now, now, now);
    Mockito.verify(l, Mockito.times(1)).entityChanged(new ChangeEvent(ChangeType.CHANGED, FunctionConfigurationSource.OBJECT_ID, now, now, now));
    cm.entityChanged(ChangeType.CHANGED, ObjectId.of("Test", "Bar"), now, now, now);
    Mockito.verify(l, Mockito.times(2)).entityChanged(new ChangeEvent(ChangeType.CHANGED, FunctionConfigurationSource.OBJECT_ID, now, now, now));
    cm.entityChanged(ChangeType.REMOVED, ObjectId.of("Test", "Bar"), now, now, now);
    Mockito.verify(l, Mockito.times(3)).entityChanged(new ChangeEvent(ChangeType.CHANGED, FunctionConfigurationSource.OBJECT_ID, now, now, now));
  }

}
