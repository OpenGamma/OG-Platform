/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.definition.AggregatingDefinitionProvider;

/**
 * A {@link LiveDataProvider} implementation that aggregates a number of other providers.
 */
public final class AggregatingLiveDataProvider extends AggregatingDefinitionProvider<MetaLiveData> implements
    LiveDataProvider {

  private AggregatingLiveDataProvider(final boolean enableCache) {
    super(enableCache);
  }

  public static AggregatingLiveDataProvider cachingInstance() {
    return new AggregatingLiveDataProvider(true);
  }

  public static AggregatingLiveDataProvider nonCachingInstance() {
    return new AggregatingLiveDataProvider(false);
  }

}
