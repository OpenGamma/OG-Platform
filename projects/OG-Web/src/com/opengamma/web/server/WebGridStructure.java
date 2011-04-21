/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.Collection;
import java.util.List;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * Represents the structure of a grid - i.e. the rows and the columns.
 */
public class WebGridStructure {
  
  private final List<ComputationTargetSpecification> _rows;
  private final Collection<String> _columns;
  
  public WebGridStructure(List<ComputationTargetSpecification> rows, Collection<String> columns) {
    _rows = rows;
    _columns = columns;
  }

  public List<ComputationTargetSpecification> getRows() {
    return _rows;
  }

  public Collection<String> getColumns() {
    return _columns;
  } 

}
