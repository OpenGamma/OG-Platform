/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Formats a time-series
 */
(function($) {
  
  /** @constructor */
  function TimeSeriesFormatter() {
    
    this.disableDefaultPopup = true
    this.customDetailIcon = 'ui-icon-image'
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      var summaryText = "Time-series";
      if (!value.v.summary) {
        summaryText += " (empty)";
      } else {
        summaryText += " (" + value.v.summary.from + " to " + value.v.summary.to + ")";
      }
      $cell.html("<span class='cell-value'>" + summaryText + "</span>");
    }
    
    this.createDetail = function($popup, $container, rowId, columnStructure, userConfig, currentData) {
      return new TimeSeriesDetail(rowId, columnStructure.colId, currentData);
    }
    
  }
  
  $.extend(true, window, {
    TimeSeriesFormatter : new TimeSeriesFormatter()
  });

}(jQuery));