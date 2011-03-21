/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a procedure provider.
 */
public class Loader extends ContextInitializationBean {

  private ProcedureProvider _procedures;

  public void setProcedures(final ProcedureProvider procedures) {
    ArgumentChecker.notNull(procedures, "procedures");
    _procedures = procedures;
  }

  public ProcedureProvider getProcedures() {
    return _procedures;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getProcedures(), "procedures");
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    sessionContext.getProcedureProvider().addProvider(getProcedures());
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.getProcedureProvider().addProvider(getProcedures());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    globalContext.getProcedureProvider().addProvider(getProcedures());
  }

}
