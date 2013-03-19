/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.language.connector.Conditional;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ContextInitializationBean} class.
 */
@Test(groups = TestGroup.UNIT)
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

  @Test(expectedExceptions = IllegalStateException.class)
  public void testPropertyAssertion() {
    final Bean bean = new Bean();
    bean.setSessionContextFactory(new SessionContextFactoryBean());
    bean.setPropertyAssertion(false);
    bean.afterPropertiesSet ();
  }

  //-------------------------------------------------------------------------
  private void assertSessionContext(final Boolean condition) {
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
    assertSessionContext(null);
  }

  @Test
  public void testSessionContextConditionHolds() {
    assertSessionContext(true);
  }

  @Test
  public void testSessionContextConditionUnheld() {
    assertSessionContext(false);
  }

  //-------------------------------------------------------------------------
  private void assertUserContext(final Boolean condition) {
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
    assertUserContext(null);
  }

  @Test
  public void testUserContextConditionHolds() {
    assertUserContext(true);
  }

  @Test
  public void testUserContextConditionUnheld() {
    assertUserContext(false);
  }

  //-------------------------------------------------------------------------
  private void assertGlobalContext(final Boolean condition) {
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
    assertGlobalContext(null);
  }

  @Test
  public void testGlobalContextConditionHolds() {
    assertGlobalContext(true);
  }

  @Test
  public void testGlobalContextConditionUnheld() {
    assertGlobalContext(false);
  }

}
