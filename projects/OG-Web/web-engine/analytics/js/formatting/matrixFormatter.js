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
  function MatrixFormatter() {
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      $cell.html("<span class='cell-value'>Matrix</span>");
    }
    
  }
  
  $.extend(true, window, {
    MatrixFormatter : new MatrixFormatter()
  });

}(jQuery));