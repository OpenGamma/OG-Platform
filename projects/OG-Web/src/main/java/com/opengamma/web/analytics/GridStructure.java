/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * The row and column structure of a grid that displays analytics data.
 */
public interface GridStructure {

  /**
   * @return The number of rows in the grid
   */
  int getRowCount();

  /**
   * @return The number of columns in the grid
   */
  int getColumnCount();

  /**
   * @return Meta data for the grid's columns
   */
  GridColumnGroups getColumnStructure();

  /**
   * @return Meta data for the grid's fixed columns
   */
  GridColumnGroup getFixedColumns();

  /**
   * @return Meta data for the grid's non fixed columns
   */
  GridColumnGroups getNonFixedColumns();

  /**
  * @param row The row index
  * @param col The column index
  * @return Pair of value spec and calculation config name
  * by the engine
  */
  Pair<String, ValueSpecification> getValueSpecificationForCell(int row, int col);

  /**
   * @param row The row index
   * @param col The column index
   * @return Pair of value req and calculation config name
   */
  Pair<String, ValueRequirement> getValueRequirementForCell(int row, int col);

}
