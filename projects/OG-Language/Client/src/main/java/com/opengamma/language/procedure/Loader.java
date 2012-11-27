/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a procedure provider.
 */
public class Loader extends ContextInitializationBean {

  private List<ProcedureProvider> _procedureProviders;

  public void setProcedureProvider(final ProcedureProvider procedureProvider) {
    ArgumentChecker.notNull(procedureProvider, "procedureProvider");
    _procedureProviders = Collections.singletonList(procedureProvider);
  }

  public ProcedureProvider getProcedureProvider() {
    if ((_procedureProviders == null) || _procedureProviders.isEmpty()) {
      return null;
    } else {
      return _procedureProviders.get(0);
    }
  }

  public void setProcedureProviders(final Collection<ProcedureProvider> procedureProviders) {
    ArgumentChecker.noNulls(procedureProviders, "procedureProviders");
    ArgumentChecker.isFalse(procedureProviders.isEmpty(), "procedureProviders");
    _procedureProviders = new ArrayList<ProcedureProvider>(procedureProviders);
  }

  public Collection<ProcedureProvider> getProcedureProviders() {
    return _procedureProviders;
  }

  protected void addProviders(final AggregatingProcedureProvider aggregator) {
    for (ProcedureProvider provider : getProcedureProviders()) {
      aggregator.addProvider(provider);
    }
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getProcedureProviders(), "procedureProviders");
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    addProviders(sessionContext.getProcedureProvider());
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    addProviders(userContext.getProcedureProvider());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    addProviders(globalContext.getProcedureProvider());
  }

}
