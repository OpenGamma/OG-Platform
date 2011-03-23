/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.opengamma.language.connector.Conditional;

/**
 * Tests the {@link ContextInitializationBean} class.
 */
public class ContextInitializationBeanTest {

  private static final String CONTEXT_VALUE = "TEST";

  private class Bean extends ContextInitializationBean {

    private boolean _propertyAssertion = true;

    public void setPropertyAssertion(final boolean propertyAssertion) {
      _propertyAssertion = propertyAssertion;
    }

    @Override
    protected void assertPropertiesSet() {
      if (!_propertyAssertion) {
        throw new IllegalStateException();
      }
    }

    /**
     * Initializes a session context.
     * 
     * @param context the context
     */
    protected void initContext(final MutableSessionContext context) {
      context.setValue(CONTEXT_VALUE, "woot");
    }

    /**
     * Initializes a user context.
     * 
     * @param context the context
     */
    protected void initContext(final MutableUserContext context) {
      context.setValue(CONTEXT_VALUE, "woot");
    }

    /**
     * Initializes a global context.
     * 
     * @param context the context
     */
    protected void initContext(final MutableGlobalContext context) {
      context.setValue(CONTEXT_VALUE, "woot");
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testPropertyAssertion() {
    final Bean bean = new Bean();
    bean.setSessionContextFactory(new SessionContextFactoryBean());
    bean.setPropertyAssertion(false);
    bean.afterPropertiesSet ();
  }

  private void testSessionContext(final Boolean condition) {
    final Bean bean = new Bean();
    final SessionContextFactoryBean ctxFactory = new SessionContextFactoryBean();
    bean.setSessionContextFactory(ctxFactory);
    if (condition != null) {
      bean.setCondition(new Conditional() {
        @Override
        public boolean evaluate(final SessionContext context) {
          return condition;
        }
      });
    }
    bean.afterPropertiesSet();
    final SessionContext ctx = ctxFactory.createSessionContext("Foo", true);
    ctx.initContext(new NullSessionContextEventHandler());
    if ((condition == null) || (condition == true)) {
      assertEquals("woot", ctx.getValue(CONTEXT_VALUE));
    } else {
      assertNull(ctx.getValue(CONTEXT_VALUE));
    }
  }

  @Test
  public void testSessionContextNoCondition() {
    testSessionContext(null);
  }

  @Test
  public void testSessionContextConditionHolds() {
    testSessionContext(true);
  }

  @Test
  public void testSessionContextConditionUnheld() {
    testSessionContext(false);
  }

  private void testUserContext(final Boolean condition) {
    final Bean bean = new Bean();
    final UserContextFactoryBean ctxFactory = new UserContextFactoryBean();
    bean.setUserContextFactory(ctxFactory);
    if (condition != null) {
      bean.setCondition(new Conditional() {
        @Override
        public boolean evaluate(final UserContext context) {
          return condition;
        }
      });
    }
    bean.afterPropertiesSet();
    final UserContext ctx = ctxFactory.getOrCreateUserContext("Foo");
    if ((condition == null) || (condition == true)) {
      assertEquals("woot", ctx.getValue(CONTEXT_VALUE));
    } else {
      assertNull(ctx.getValue(CONTEXT_VALUE));
    }
  }

  @Test
  public void testUserContextNoCondition() {
    testUserContext(null);
  }

  @Test
  public void testUserContextConditionHolds() {
    testUserContext(true);
  }

  @Test
  public void testUserContextConditionUnheld() {
    testUserContext(false);
  }

  private void testGlobalContext(final Boolean condition) {
    final Bean bean = new Bean();
    final GlobalContextFactoryBean ctxFactory = new GlobalContextFactoryBean();
    bean.setGlobalContextFactory(ctxFactory);
    if (condition != null) {
      bean.setCondition(new Conditional() {
        @Override
        public boolean evaluate(final GlobalContext context) {
          return condition;
        }
      });
    }
    bean.afterPropertiesSet();
    final GlobalContext ctx = ctxFactory.getOrCreateGlobalContext();
    if ((condition == null) || (condition == true)) {
      assertEquals("woot", ctx.getValue(CONTEXT_VALUE));
    } else {
      assertNull(ctx.getValue(CONTEXT_VALUE));
    }
  }

  @Test
  public void testGlobalContextNoCondition() {
    testGlobalContext(null);
  }

  @Test
  public void testGlobalContextConditionHolds() {
    testGlobalContext(true);
  }

  @Test
  public void testGlobalContextConditionUnheld() {
    testGlobalContext(false);
  }

}
