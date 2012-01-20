/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.function.Definition;

/**
 * Accepts a function definition and produces zero or more exports.
 */
public interface FunctionDefinitionExporter {

  void exportFunction(Definition definition);

}
