/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats unknown data type
 */
(function($) {
  
  /** @constructor */
  function UnknownTypeFormatter() {
    
    this.supportsHistory = false;
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      $cell.html("<span class='cell-value'>" + value.name + "</span>");
    }
    
  }
  
  $.extend(true, window, {
    UnknownTypeFormatter : new UnknownTypeFormatter()
  });

}(jQuery));