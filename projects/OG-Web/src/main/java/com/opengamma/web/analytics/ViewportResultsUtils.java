/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

/**
 * This class is for debugging.
 */
/* package */ class ViewportResultsUtils {

  /* package */ static String dumpResults(ViewportResults viewportResults, int startRow, int rowCount) {
    ViewportDefinition viewportDefinition = viewportResults.getViewportDefinition();
    if (!(viewportDefinition instanceof RectangularViewportDefinition)) {
      return "dumpResults only implemented for RectangularViewportDefinition";
    }
    RectangularViewportDefinition def = (RectangularViewportDefinition) viewportDefinition;
    int colCount = def.getColumns().size();
    int startIndex = startRow * colCount;
    int endIndex = startIndex + (rowCount * colCount);
    List<ResultsCell> results = viewportResults.getResults().subList(startIndex, endIndex);
    StringBuilder sb = new StringBuilder();
    for (ResultsCell cell : results) {
      Object value = cell.getValue();
      if (value instanceof RowTarget) {
        sb.append("\n").append(((RowTarget) value).getName());
      } else {
        sb.append(value);
      }
      sb.append(", ");
    }
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

}
