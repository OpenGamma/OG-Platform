/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.Convention;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ConfigMasterChangeProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigMasterChangeProviderTest {

  public void testSharedInstances() {
    final ConfigMaster m1 = Mockito.mock(ConfigMaster.class);
    final ConfigMaster m2 = Mockito.mock(ConfigMaster.class);
    final ConfigMasterChangeProvider p1a = ConfigMasterChangeProvider.of(m1);
    final ConfigMasterChangeProvider p2a = ConfigMasterChangeProvider.of(m2);
    final ConfigMasterChangeProvider p1b = ConfigMasterChangeProvider.of(m1);
    final ConfigMasterChangeProvider p2b = ConfigMasterChangeProvider.of(m2);
    assertSame(p1a, p1b);
    assertNotSame(p1a, p2a);
    assertSame(p2a, p2b);
    assertNotSame(p1b, p2b);
  }

  public void testChangeManagerListeners() {
    final ChangeManager cm = Mockito.mock(ChangeManager.class);
    final ConfigMaster underlying = Mockito.mock(ConfigMaster.class);
    Mockito.when(underlying.changeManager()).thenReturn(cm);
    final ConfigMasterChangeProvider cp = new ConfigMasterChangeProvider(underlying);
    final ChangeListener l1 = Mockito.mock(ChangeListener.class);
    final ChangeListener l2 = Mockito.mock(ChangeListener.class);
    Mockito.verify(cm, Mockito.never()).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    cp.changeManager().addChangeListener(l1);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    cp.changeManager().addChangeListener(l2);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    cp.changeManager().removeChangeListener(l1);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.never()).removeChangeListener(Mockito.<ChangeListener>any());
    cp.changeManager().removeChangeListener(l2);
    Mockito.verify(cm, Mockito.times(1)).addChangeListener(Mockito.<ChangeListener>any());
    Mockito.verify(cm, Mockito.times(1)).removeChangeListener(Mockito.<ChangeListener>any());
    Mockito.verifyNoMoreInteractions(cm);
  }

  private static class GatheringChangeListener implements ChangeListener {

    private final Collection<ChangeEvent> _events;

    public GatheringChangeListener(final Collection<ChangeEvent> events) {
      _events = events;
    }

    @Override
    public void entityChanged(ChangeEvent event) {
      _events.add(event);
    }

  }

  public void testAddNotification() {
    final ConfigMaster underlying = new InMemoryConfigMaster();
    final ConfigMasterChangeProvider cp = new ConfigMasterChangeProvider(underlying);
    final List<ChangeEvent> events = new ArrayList<ChangeEvent>();
    cp.changeManager().addChangeListener(new GatheringChangeListener(events));
    underlying.add(new ConfigDocument(ConfigItem.of("Foo", "Test")));
    assertEquals(events.size(), 1);
    final ChangeEvent e = events.get(0);
    assertEquals(e.getObjectId(), ObjectId.of(AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME, String.class.getName()));
    assertEquals(e.getType(), ChangeType.ADDED);
  }

  private List<ChangeEvent> testChangeNotification(final Class<?> oldType, final Class<?> newType) {
    final ConfigItem<?> oldItem = ConfigItem.of(Mockito.mock(oldType), "Old", oldType);
    final ConfigItem<?> newItem = ConfigItem.of(Mockito.mock(newType), "New", newType);
    final Instant now = Instant.now();
    final ConfigMaster underlying = Mockito.mock(ConfigMaster.class);
    Mockito.when(underlying.changeManager()).thenReturn(new BasicChangeManager());
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(now.minusNanos(1), now))).thenReturn(new ConfigDocument(oldItem));
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(now, now))).thenReturn(new ConfigDocument(newItem));
    final ConfigMasterChangeProvider cp = new ConfigMasterChangeProvider(underlying);
    final List<ChangeEvent> events = new ArrayList<ChangeEvent>();
    cp.changeManager().addChangeListener(new GatheringChangeListener(events));
    underlying.changeManager().entityChanged(ChangeType.CHANGED, ObjectId.of("Test", "Foo"), now, null, now);
    return events;
  }

  public void testChangeNotification1() {
    final List<ChangeEvent> events = testChangeNotification(Convention.class, Convention.class);
    assertEquals(events.size(), 1);
    ChangeEvent e = events.get(0);
    assertEquals(e.getObjectId(), ObjectId.of(AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME, Convention.class.getName()));
    assertEquals(e.getType(), ChangeType.CHANGED);
  }

  public void testChangeNotification2() {
    final List<ChangeEvent> events = testChangeNotification(Convention.class, ConventionBundle.class);
    assertEquals(events.size(), 2);
    ChangeEvent e = events.get(0);
    assertEquals(e.getObjectId(), ObjectId.of(AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME, Convention.class.getName()));
    assertEquals(e.getType(), ChangeType.REMOVED);
    e = events.get(1);
    assertEquals(e.getObjectId(), ObjectId.of(AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME, ConventionBundle.class.getName()));
    assertEquals(e.getType(), ChangeType.ADDED);
  }

  public void testRemoveNotification() {
    final ConfigMaster underlying = new InMemoryConfigMaster();
    final ConfigMasterChangeProvider cp = new ConfigMasterChangeProvider(underlying);
    final ConfigDocument added = underlying.add(new ConfigDocument(ConfigItem.of("Foo", "Test")));
    final List<ChangeEvent> events = new ArrayList<ChangeEvent>();
    cp.changeManager().addChangeListener(new GatheringChangeListener(events));
    // Note: we can't remove the object because InMemoryConfigMaster doesn't support versioning
    underlying.changeManager().entityChanged(ChangeType.REMOVED, added.getObjectId(), Instant.now(), null, Instant.now());
    assertEquals(events.size(), 1);
    final ChangeEvent e = events.get(0);
    assertEquals(e.getObjectId(), ObjectId.of(AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME, String.class.getName()));
    assertEquals(e.getType(), ChangeType.REMOVED);
  }

}
