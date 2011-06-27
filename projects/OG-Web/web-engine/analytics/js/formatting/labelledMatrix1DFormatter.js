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
  function LabelledMatrix1DFormatter() {
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      var summaryText = value.v.summary == 0 ? "N/A" : "Vector (" + value.v.summary + ")";
      $cell.html("<span class='cell-value'>" + summaryText + "</span>");
    }
    
    this.createDetail = function($popup, $container, rowId, columnStructure, userConfig, currentData) {
      return new LabelledMatrix1DDetail($popup, $container, rowId, columnStructure.colId);
    }
    
  }
  
  $.extend(true, window, {
    LabelledMatrix1DFormatter : new LabelledMatrix1DFormatter()
  });

}(jQuery));