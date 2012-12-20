/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.definition.AggregatingDefinitionProvider;

/**
 * A {@link FunctionProvider} implementation that aggregates a number of other providers.
 */
public final class AggregatingFunctionProvider extends AggregatingDefinitionProvider<MetaFunction> implements
    FunctionProvider {

  private AggregatingFunctionProvider(final boolean enableCache) {
    super(enableCache);
  }

  public static AggregatingFunctionProvider cachingInstance() {
    return new AggregatingFunctionProvider(true);
  }

  public static AggregatingFunctionProvider nonCachingInstance() {
    return new AggregatingFunctionProvider(false);
  }

}
