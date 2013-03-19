/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.language.context.AbstractSessionContextEventHandler;
import com.opengamma.language.context.AbstractUserContextEventHandler;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextEventHandler;
import com.opengamma.language.context.UserContextEventHandler;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ViewClients} class and its subclasses.
 */
@Test(groups = TestGroup.UNIT)
public class ViewClientsTest {

  private static final UniqueId VIEW_ID = UniqueId.of("abc", "def");

  private TestUtils createTestUtils() {
    final TestUtils testUtils = new TestUtils() {

      @Override
      protected UserContextEventHandler createUserContextEventHandler() {
        return new AbstractUserContextEventHandler(super.createUserContextEventHandler()) {

          @Override
          protected void initContextImpl(final MutableUserContext context) {
            context.setViewClients(new UserViewClients(context));
          }

          @Override
          protected void doneContextImpl(final MutableUserContext context) {
            context.getViewClients().destroyAll();
          }

        };
      }

      @Override
      protected SessionContextEventHandler createSessionContextEventHandler() {
        return new AbstractSessionContextEventHandler(super.createSessionContextEventHandler()) {

          @Override
          protected void initContextImpl(final MutableSessionContext context) {
            context.setViewClients(new SessionViewClients(context));
          }

          @Override
          protected void doneContextImpl(final MutableSessionContext context) {
            context.getViewClients().destroyAll();
          }

        };
      }

    };
    testUtils.setViewProcessor(new MockViewProcessor());
    return testUtils;
  }

  public void testLockView() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientKey vck = new ViewClientKey(ViewClientDescriptor.tickingMarketData(VIEW_ID, null), true);
    final ViewClientHandle handle1 = context.getUserContext().getViewClients().lockViewClient(vck);
    final MockViewClient viewClient1 = (MockViewClient) handle1.get().getViewClient();
    assertEquals(viewClient1.getAttachedViewDefinitionId(), VIEW_ID);
    final ViewClientHandle handle2 = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(ViewClientDescriptor.tickingMarketData(VIEW_ID, null), true));
    final MockViewClient viewClient2 = (MockViewClient) handle2.get().getViewClient();
    assertSame(viewClient2, viewClient1);
    handle1.unlock();
    assertFalse(viewClient1.isShutdown());
    handle2.unlock();
    assertTrue(viewClient1.isShutdown());
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testUnlockUnlock() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(ViewClientDescriptor.tickingMarketData(VIEW_ID, null), true));
    handle.unlock();
    handle.unlock();
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testUnlockGet() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientHandle handle = context.getUserContext().getViewClients().lockViewClient(new ViewClientKey(ViewClientDescriptor.tickingMarketData(VIEW_ID, null), true));
    handle.unlock();
    handle.get();
  }

  public void testDetachView() {
    final SessionContext context = createTestUtils().createSessionContext();
    final ViewClientKey vck = new ViewClientKey(ViewClientDescriptor.tickingMarketData(VIEW_ID, null), true);
    final ViewClientHandle handle1 = context.getUserContext().getViewClients().lockViewClient(vck);
    final MockViewClient viewClient1 = (MockViewClient) handle1.get().getViewClient();
    assertEquals(viewClient1.getAttachedViewDefinitionId(), VIEW_ID);
    final UniqueId uid = handle1.detachAndUnlock(context);
    assertFalse(viewClient1.isShutdown());
    final ViewClientHandle handle2 = context.getViewClients().lockViewClient(uid);
    final MockViewClient viewClient2 = (MockViewClient) handle2.get().getViewClient();
    assertSame(viewClient1, viewClient2);
    handle2.unlock();
    assertFalse(viewClient1.isShutdown());
    final DetachedViewClientHandle handle3 = context.getViewClients().lockViewClient(uid);
    handle3.attachAndUnlock();
    assertTrue(viewClient1.isShutdown());
    final DetachedViewClientHandle handle4 = context.getViewClients().lockViewClient(uid);
    assertNull(handle4);
  }

}
