/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the extension points for chaining custom information and behaviors to the contexts.
 */
@Test(groups = TestGroup.UNIT)
public class ContextTest {

  @Test
  public void testCreateSessions() {
    final SessionContextFactoryBean contextFactory = new SessionContextFactoryBean();
    final SessionContext session1 = contextFactory.createSessionContext("user1", false);
    assertNotNull(session1);
    assertNotNull(session1.getUserContext());
    assertNotNull(session1.getGlobalContext());
    final SessionContext session2 = contextFactory.createSessionContext("user2", false);
    assertNotNull(session2);
    assertNotNull(session2.getUserContext());
    assertNotNull(session2.getGlobalContext());
    assertNotSame(session1.getUserContext(), session2.getUserContext());
    assertSame(session1.getGlobalContext(), session2.getGlobalContext());
    final SessionContext session3 = contextFactory.createSessionContext("user1", false);
    assertNotNull(session3);
    assertSame(session1.getUserContext(), session3.getUserContext());
    assertSame(session1.getGlobalContext(), session3.getGlobalContext());
  }

  @Test
  public void testDestroySessions() {
    final SessionContextFactoryBean contextFactory = new SessionContextFactoryBean();
    final SessionContext session1 = contextFactory.createSessionContext("user", false);
    final SessionContext session2 = contextFactory.createSessionContext("user", false);
    assertSame(session1.getUserContext(), session2.getUserContext());
    session1.initContext(new NullSessionContextEventHandler());
    session2.initContext(new NullSessionContextEventHandler());
    session1.doneContext();
    session2.doneContext();
    final SessionContext session3 = contextFactory.createSessionContext("user", false);
    assertNotSame(session1.getUserContext(), session3.getUserContext());
    assertNotSame(session2.getUserContext(), session3.getUserContext());
  }

  @Test
  public void testGlobalEventHandler() {
    final GlobalContextFactoryBean globalContextFactory = new GlobalContextFactoryBean();
    final AtomicReference<GlobalContext> context1 = new AtomicReference<GlobalContext>();
    final AtomicInteger handler1 = new AtomicInteger();
    final AtomicReference<GlobalContext> context2 = new AtomicReference<GlobalContext>();
    final AtomicInteger handler2 = new AtomicInteger();
    // This handler should be run first
    globalContextFactory.setGlobalContextEventHandler(new AbstractGlobalContextEventHandler(globalContextFactory
        .getGlobalContextEventHandler()) {
      @Override
      protected void initContextImpl(MutableGlobalContext context) {
        assertEquals(handler2.get(), handler1.get());
        context1.set(context);
        handler1.incrementAndGet();
      }
    });
    // This handler should be run second
    globalContextFactory.setGlobalContextEventHandler(new AbstractGlobalContextEventHandler(globalContextFactory
        .getGlobalContextEventHandler()) {
      @Override
      protected void initContextImpl(MutableGlobalContext context) {
        assertEquals(handler1.get() - 1, handler2.get());
        assertSame(context1.get(), context);
        context2.set(context);
        handler2.incrementAndGet();
      }
    });
    final SessionContextFactoryBean sessionContextFactory = new SessionContextFactoryBean(new UserContextFactoryBean(
        globalContextFactory));
    SessionContext sessionContext = sessionContextFactory.createSessionContext("user1", false);
    // Both handlers to have been run
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
    // Context passed the same as the one on the session
    assertSame(context1.get(), sessionContext.getGlobalContext());
    assertSame(context2.get(), sessionContext.getGlobalContext());
    sessionContextFactory.createSessionContext("user2", false);
    // Handlers do not get run again
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
  }

  @Test
  public void testUserEventHandler() {
    final UserContextFactoryBean userContextFactory = new UserContextFactoryBean();
    final AtomicReference<UserContext> context1 = new AtomicReference<UserContext>();
    final AtomicInteger handler1 = new AtomicInteger();
    final AtomicReference<UserContext> context2 = new AtomicReference<UserContext>();
    final AtomicInteger handler2 = new AtomicInteger();
    userContextFactory.setUserContextEventHandler(new AbstractUserContextEventHandler(userContextFactory
        .getUserContextEventHandler()) {

      // This handler should be run first
      @Override
      protected void initContextImpl(MutableUserContext context) {
        assertEquals(handler2.get(), handler1.get());
        context1.set(context);
        handler1.incrementAndGet();
      }

      // This handler should be run second
      @Override
      protected void doneContextImpl(MutableUserContext context) {
        assertEquals(handler2.get() - 1, handler1.get());
        assertSame(context1.get(), context);
        assertEquals(null, context2.get());
        context1.set(null);
        handler1.incrementAndGet();
      }

    });
    userContextFactory.setUserContextEventHandler(new AbstractUserContextEventHandler(userContextFactory
        .getUserContextEventHandler()) {

      // This handler should be run second
      @Override
      protected void initContextImpl(MutableUserContext context) {
        assertEquals(handler1.get() - 1, handler2.get());
        assertSame(context1.get(), context);
        context2.set(context);
        handler2.incrementAndGet();
      }

      // This handler should be run first
      @Override
      protected void doneContextImpl(MutableUserContext context) {
        assertEquals(handler1.get(), handler2.get());
        assertSame(context2.get(), context);
        context2.set(null);
        handler2.incrementAndGet();
      }

    });
    final SessionContextFactoryBean sessionContextFactory = new SessionContextFactoryBean(userContextFactory);
    final SessionContext sessionContext1 = sessionContextFactory.createSessionContext("user", false);
    // Both handlers to have been run
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
    // Context passed the same as the one on the session
    assertSame(context1.get(), sessionContext1.getUserContext());
    assertSame(context2.get(), sessionContext1.getUserContext());
    final SessionContext sessionContext2 = sessionContextFactory.createSessionContext("user", false);
    // Handlers do not get run again
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
    sessionContext1.doneContext();
    // Handlers not run - user still active
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
    sessionContext2.doneContext();
    // Handlers now run - user no longer active
    assertEquals(2, handler1.get());
    assertEquals(2, handler2.get());
    assertNull(context1.get());
    assertNull(context2.get());
    sessionContextFactory.createSessionContext("user", false);
    // Handlers get run again on new user
    assertEquals(3, handler1.get());
    assertEquals(3, handler2.get());
  }

  @Test
  public void testSessionEventHandler() {
    final SessionContextFactoryBean contextFactory = new SessionContextFactoryBean();
    final AtomicReference<SessionContext> context1 = new AtomicReference<SessionContext>();
    final AtomicInteger handler1 = new AtomicInteger();
    final AtomicReference<SessionContext> context2 = new AtomicReference<SessionContext>();
    final AtomicInteger handler2 = new AtomicInteger();
    contextFactory.setSessionContextEventHandler(new AbstractSessionContextEventHandler(contextFactory
        .getSessionContextEventHandler()) {

      // Should be called first
      @Override
      protected void initContextImpl(MutableSessionContext context) {
        assertEquals(handler2.get(), handler1.get());
        context1.set(context);
        handler1.incrementAndGet();
      }

      // Should be called second
      @Override
      protected void doneContextImpl(MutableSessionContext context) {
        assertEquals(handler2.get() - 1, handler1.get());
        assertSame(context1.get(), context);
        context1.set(null);
        handler1.incrementAndGet();
      }

    });
    contextFactory.setSessionContextEventHandler(new AbstractSessionContextEventHandler(contextFactory
        .getSessionContextEventHandler()) {

      // Should be called second
      @Override
      protected void initContextImpl(MutableSessionContext context) {
        assertEquals(handler1.get() - 1, handler2.get());
        assertSame(context1.get(), context);
        context2.set(context);
        handler2.incrementAndGet();
      }

      // Should be called first
      @Override
      protected void doneContextImpl(MutableSessionContext context) {
        assertEquals (handler1.get (), handler2.get ());
        assertSame(context2.get(), context);
        context2.set(null);
        handler2.incrementAndGet();
      }

    });
    final SessionContext sessionContext1 = contextFactory.createSessionContext("user", false);
    // Both handlers to not have been run
    assertEquals(0, handler1.get());
    assertEquals(0, handler2.get());
    sessionContext1.initContext(new NullSessionContextEventHandler());
    // Both handlers to have been run
    assertEquals(1, handler1.get());
    assertEquals(1, handler2.get());
    // Context passed the same
    assertSame(context1.get(), sessionContext1);
    assertSame(context2.get(), sessionContext1);
    final SessionContext sessionContext2 = contextFactory.createSessionContext("user", false);
    sessionContext2.initContext(new NullSessionContextEventHandler());
    // Handlers get run again
    assertEquals(2, handler1.get());
    assertEquals(2, handler2.get());
    sessionContext2.doneContext();
    // Handlers run
    assertEquals(3, handler1.get());
    assertEquals(3, handler2.get());
    assertNull(context1.get());
    assertNull(context2.get());
  }
}
