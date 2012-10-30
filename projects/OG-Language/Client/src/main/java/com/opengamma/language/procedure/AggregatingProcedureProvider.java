/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import com.opengamma.language.definition.AggregatingDefinitionProvider;

/**
 * A {@link ProcedureProvider} implementation that aggregates a number of other providers.
 */
public final class AggregatingProcedureProvider extends AggregatingDefinitionProvider<MetaProcedure> implements
    ProcedureProvider {

  private AggregatingProcedureProvider(final boolean enableCache) {
    super(enableCache);
  }

  public static AggregatingProcedureProvider cachingInstance() {
    return new AggregatingProcedureProvider(true);
  }

  public static AggregatingProcedureProvider nonCachingInstance() {
    return new AggregatingProcedureProvider(false);
  }

}
