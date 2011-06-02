/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats primitive values
 */
(function($) {
  
  /** @constructor */
  function PrimitiveFormatter() {
    
    var self = this;
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      $cell.html("<span class='cell-value'>" + value.v + "</span>");
    }
    
  }
  
  $.extend(true, window, {
    PrimitiveFormatter : new PrimitiveFormatter()
  });

}(jQuery));