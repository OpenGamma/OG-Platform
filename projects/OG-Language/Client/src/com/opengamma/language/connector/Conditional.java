/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;

/**
 * Allows conditional behavior to be expressed within the Spring configuration files. If a bean
 * is given one or more conditionals, it should evaluate them and then take or suppress action
 * as appropriate to its documentation.
 */
public abstract class Conditional {

  /**
   * Boolean constant condition.
   * 
   * @param constant conditional result
   * @return a {@link Conditional} instance
   */
  public static Conditional booleanConstant(final boolean constant) {
    return new Conditional() {
      @Override
      public boolean evaluate() {
        return constant;
      }
    };
  }

  /**
   * Always TRUE condition.
   * 
   * @return a {@link Conditional} instance
   */
  public static Conditional isTrue() {
    return booleanConstant(true);
  }

  /**
   * Always FALSE condition.
   * 
   * @return a {@link Conditional} instance
   */
  public static Conditional isFalse() {
    return booleanConstant(false);
  }

  /**
   * Tests if it is debug mode for the either the client session or infrastructure stack.
   * 
   * @return a {@link Conditional} instance 
   */
  public static Conditional isDebug() {
    return new Conditional() {

      @Override
      public boolean evaluate(final SessionContext sessionContext) {
        return sessionContext.isDebug() || evaluate();
      }

      @Override
      public boolean evaluate() {
        return GlobalContext.isDebug();
      }

    };
  }

  /**
   * Tests if it is debug mode for the client session.
   * 
   * @return a {@link Conditional} instance
   */
  public static Conditional isDebugClient() {
    return new Conditional() {
      @Override
      public boolean evaluate(final SessionContext sessionContext) {
        return sessionContext.isDebug();
      }
    };
  }

  /**
   * Tests if it is debug mode for the infrastructure stack.
   * 
   * @return a {@link Conditional} instance
   */
  public static Conditional isDebugStack() {
    return new Conditional() {
      @Override
      public boolean evaluate() {
        return GlobalContext.isDebug();
      }
    };
  }

  public boolean evaluate(final SessionContext sessionContext) {
    return evaluate(sessionContext.getUserContext());
  }

  public boolean evaluate(final UserContext userContext) {
    return evaluate(userContext.getGlobalContext());
  }

  public boolean evaluate(final GlobalContext globalContext) {
    return evaluate();
  }

  public boolean evaluate() {
    throw new UnsupportedOperationException();
  }

  public static boolean holds(final Conditional condition) {
    return (condition != null) ? condition.evaluate() : false;
  }

  public static boolean holds(final Conditional condition, final SessionContext sessionContext) {
    return (condition != null) ? condition.evaluate(sessionContext) : true;
  }

  public static boolean holds(final Conditional condition, final UserContext userContext) {
    return (condition != null) ? condition.evaluate(userContext) : true;
  }

  public static boolean holds(final Conditional condition, final GlobalContext globalContext) {
    return (condition != null) ? condition.evaluate(globalContext) : true;
  }

}
