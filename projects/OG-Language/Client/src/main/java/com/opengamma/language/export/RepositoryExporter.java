/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

/**
 * Provides export services for the three repository classes.
 */
public interface RepositoryExporter {

  FunctionDefinitionExporter getFunctionExporter();

  LiveDataDefinitionExporter getLiveDataExporter();

  ProcedureDefinitionExporter getProcedureExporter();

}
