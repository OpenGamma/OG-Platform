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
  function DoubleFormatter() {
    
    var self = this;
    
    this.supportsHistory = true;
    
    this.renderCell = function($cell, value, row, dataContext, colDef, columnStructure, userConfig) {
      if (!value) {
        return;
      }
      $cell.empty();
      var tickIndicatorClass;
      var displayValue = value ? value.v : null;
      var lastValue = value ? value.h[value.h.length - 1] : null;
      var lastButOneValue = value ? value.h[value.h.length - 2] : null;
      
      if (lastValue && lastButOneValue) {
        var diff = lastValue - lastButOneValue;
        if (diff == 0.0) {
          tickIndicatorClass = "tickindicator same";
        } else if (diff > 0) {
          tickIndicatorClass = "tickindicator up";
        } else {
          tickIndicatorClass = "tickindicator down";
        }
      } else {
        tickIndicatorClass = "tickindicator same";
      }
      
      var displayValueHtml;
      if (lastValue && lastValue < 0) {
        displayValueHtml = "<span class='negative'>" + displayValue + "</span>";
      } else {
        displayValueHtml = displayValue;
      }

      if (value && userConfig.getSparklinesEnabled()) {
        $("<span class='primitive-history-sparkline'></span>")
            .appendTo($cell)
            .sparkline(value.h, $.extend(true, {}, Common.sparklineDefaults, {width: 50, lineColor: 'rgb(71, 113, 135)'}));
      }
      $cell.append("<span class='cell-value right'>" + displayValueHtml + "</span><span class='" + tickIndicatorClass + "' />");
    }
    
  }
  
  $.extend(true, window, {
    DoubleFormatter : new DoubleFormatter()
  });

}(jQuery));