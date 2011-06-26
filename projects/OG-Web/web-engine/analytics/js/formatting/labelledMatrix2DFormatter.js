/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats matrices
 */
(function($) {
  
  /** @constructor */
  function LabelledMatrix2DFormatter() {
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      var summaryText = "Matrix (" + value.v.summary.rowCount + " x " + value.v.summary.colCount + ")";
      $cell.html("<span class='cell-value'>" + summaryText + "</span>");
    }
    
    this.createDetail = function($popup, $container, rowId, columnStructure, userConfig, currentData) {
      return new LabelledMatrix2DDetail($popup, $container, rowId, columnStructure.colId, currentData);
    }
    
  }
  
  $.extend(true, window, {
    LabelledMatrix2DFormatter : new LabelledMatrix2DFormatter()
  });

}(jQuery));