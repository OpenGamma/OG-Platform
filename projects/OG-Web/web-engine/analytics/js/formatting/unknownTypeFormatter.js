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
      var type = value.v.name ? value.v.name : "Unknown anonymous type"
      $cell.html("<span class='cell-value'>" + type + "</span>");
    }
    
  }
  
  $.extend(true, window, {
    UnknownTypeFormatter : new UnknownTypeFormatter()
  });

}(jQuery));