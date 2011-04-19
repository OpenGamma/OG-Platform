/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats Interpolated Yield Curves
 */
(function($) {
  
  /** @constructor */
  function InterpolatedYieldCurveFormatter() {
    
    var self = this;
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      if (!value) {
        return;
      }
      $cell.empty();
      
      var points = value.summary;

      $("<span class='interpolated-yield-curve'></div>")
          .height($cell.height() - 10)
          .width($cell.width() - 10)
          .appendTo($cell)
          .sparkline(points, $.extend(true, {}, Common.sparklineDefaults, { width: '100%', height: '100%' , fillColor: false }));
    }
    
    this.createDetail = function($container, rowId, columnStructure, userConfig) {
      return new InterpolatedYieldCurveDetail($container, rowId, columnStructure.colId);
    }
    
  }
  
  $.extend(true, window, {
    InterpolatedYieldCurveFormatter : new InterpolatedYieldCurveFormatter()
  });

}(jQuery));