/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.procedure;

import com.opengamma.language.definition.AbstractDefinitionProvider;

/**
 * Partial {@link ProcedureProvider} implementation that provides caching.
 */
public abstract class AbstractProcedureProvider extends AbstractDefinitionProvider<MetaProcedure> implements
    ProcedureProvider {

  protected AbstractProcedureProvider() {
  }

}
