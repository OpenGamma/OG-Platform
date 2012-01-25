/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.definition.Definition;

/**
 * Callback interface for exporting a definition text.
 */
public interface DefinitionExporter {

  void export(Definition definition, String text);

}
