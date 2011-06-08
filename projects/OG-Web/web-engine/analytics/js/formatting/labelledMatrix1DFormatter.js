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
      $cell.html("<span class='cell-value'>Vector (" + value.v.summary + ")</span>");
    }
    
    this.createDetail = function($container, rowId, columnStructure, userConfig) {
      return new LabelledMatrix1DDetail($container, rowId, columnStructure.colId);
    }
    
  }
  
  $.extend(true, window, {
    LabelledMatrix1DFormatter : new LabelledMatrix1DFormatter()
  });

}(jQuery));