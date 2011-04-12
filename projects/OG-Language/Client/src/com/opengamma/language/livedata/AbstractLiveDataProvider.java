/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.definition.AbstractDefinitionProvider;

/**
 * Partial {@link LiveDataProvider} implementation that provides caching.
 */
public abstract class AbstractLiveDataProvider extends AbstractDefinitionProvider<MetaLiveData> implements
    LiveDataProvider {

  protected AbstractLiveDataProvider() {
  }

}
