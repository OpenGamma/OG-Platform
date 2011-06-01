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
      $cell.html("<div class='volatility-surface'>" +
        "  <span>x=" + value.v['xs'] + "</span>" +
        "  <span>y=" + value.v['ys'] + "</span>" +
        "</div>");
    }
    
    this.createDetail = function($container, rowId, columnStructure, userConfig) {
      return new VolatilitySurfaceDataDetail($container, rowId, columnStructure.colId);
    }
    
  }
  
  $.extend(true, window, {
    VolatilitySurfaceDataFormatter : new VolatilitySurfaceDataFormatter()
  });

}(jQuery));