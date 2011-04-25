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
    
    this.supportsHistory = true;
    
    this.renderCell = function($cell, valueHistory, row, dataContext, colDef, columnStructure, userConfig) {
      if (!valueHistory) {
        return;
      }
      $cell.empty();
      var spanclass;
      var lastValue = valueHistory ? valueHistory[valueHistory.length - 1] : null;
      var lastButOneValue = valueHistory ? valueHistory[valueHistory.length - 2] : null;
      if (lastValue && lastButOneValue) {
        var diff = lastValue - lastButOneValue;
        if (diff == 0.0) {
          spanclass = "tickindicator same";
        } else if (diff > 0) {
          spanclass = "tickindicator up";
        } else {
          spanclass = "tickindicator down";
        }
      } else {
        spanclass = "tickindicator same";
      }
      if (valueHistory && userConfig.getSparklinesEnabled()) {
        $("<span class='primitive-history-sparkline'></span>")
            .appendTo($cell)
            .sparkline(valueHistory, $.extend(true, {}, Common.sparklineDefaults, {width: 50, lineColor: 'rgb(71, 113, 135)'}));
      }
      $cell.append("<span class='" + spanclass + "'></span><span class='cell-value'>" + lastValue + "</span>");
    }
    
  }
  
  $.extend(true, window, {
    PrimitiveFormatter : new PrimitiveFormatter()
  });

}(jQuery));