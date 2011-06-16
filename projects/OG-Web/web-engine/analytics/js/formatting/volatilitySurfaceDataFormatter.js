/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats Volatility Surface Data bundles
 */
(function($) {
  
  /** @constructor */
  function VolatilitySurfaceDataFormatter() {
    
    var self = this;
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      if (!value || !value.v) {
        return;
      }
      $cell.html("<span class='cell-value'>Volatility Surface (" + value.v['xCount'] + " x " + value.v['yCount'] + ")</span>");
    }
    
    this.createDetail = function($container, rowId, columnStructure, userConfig) {
      return new VolatilitySurfaceDataDetail($container, rowId, columnStructure.colId);
    }
    
  }
  
  $.extend(true, window, {
    VolatilitySurfaceDataFormatter : new VolatilitySurfaceDataFormatter()
  });

}(jQuery));