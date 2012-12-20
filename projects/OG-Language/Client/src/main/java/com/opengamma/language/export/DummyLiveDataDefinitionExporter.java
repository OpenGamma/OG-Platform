/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.livedata.Definition;

/**
 * No-op implementation of {@link LiveDataDefinitionExporter}.
 */
public class DummyLiveDataDefinitionExporter implements LiveDataDefinitionExporter {

  @Override
  public void exportLiveData(final Definition definition) {
    // No-op
  }

}
