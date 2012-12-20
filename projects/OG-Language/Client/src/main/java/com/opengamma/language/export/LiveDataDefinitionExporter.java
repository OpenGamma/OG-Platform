/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.livedata.Definition;

/**
 * Accepts a live data definition and produces zero or more exports.
 */
public interface LiveDataDefinitionExporter {

  void exportLiveData(Definition definition);

}
