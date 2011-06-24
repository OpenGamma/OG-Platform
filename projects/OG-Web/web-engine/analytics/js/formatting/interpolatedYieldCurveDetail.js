/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Renders a detailed view of an InterpolatedYieldCurve
 */
(function($) {
  
  /** @constructor */
  function InterpolatedYieldCurveDetail(_$popup, _$container, _rowId, _colId) {
    
    var self = this;
    
    var _chart;
    var _data;
    
    function init() {
      // Set some defaults
      _$popup.width(400);
      _$popup.height(300);
      _$popup.addClass('tabs');
      
      var tabManager = new TabManager();
      var $tabsContainer = $("<div class='interpolated-yield-curve-detail-tabs'></div>")
        .append("<ul></ul>");
      _$container.empty().append($tabsContainer);
      $tabsContainer.tabs({
        select : tabManager.onSelectTab,
        show : tabManager.onShowTab
      });
      
      var curveDivId = _rowId + "-" + _colId + "-curve";
      var $curvePage= $("<div id='" + curveDivId + "' class='curve'></div>")
          .appendTo($tabsContainer);
      _$container.data("curvePage", $curvePage);
      var $curveContainer = $("<div class='chart'></div>")
        .appendTo($curvePage);
      _chart = new InterpolatedYieldCurveChart($curveContainer);
      $tabsContainer.tabs("add", "#" + curveDivId, "Curve");
      tabManager.registerTab(curveDivId, _chart);
      
      var dataDivId = _rowId + "-" + _colId + "-data";
      var $dataPage = $("<div id='" + dataDivId + "' class='data'></div>")
          .appendTo($tabsContainer);
      _data = new InterpolatedYieldCurveData($dataPage);
      $tabsContainer.tabs("add", "#" + dataDivId, "Data for Curve");
      tabManager.registerTab(dataDivId, _data);
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateValue = function(value) {
      if (!value) {
        return;
      }
      
      _chart.setPoints(value.detailed);
      _data.updateReceived(value.summary);
    }
    
    this.resize = function() {
      if (_chart) {
        _chart.refresh();
      }
      
      if (_data) {
        _data.refresh();
      }
    }
    
    this.destroy = function() {
      _data.destroy();
    }
    
    //-----------------------------------------------------------------------
    
    init();
    
  }
  
  $.extend(true, window, {
    InterpolatedYieldCurveDetail : InterpolatedYieldCurveDetail
  });

}(jQuery));
 