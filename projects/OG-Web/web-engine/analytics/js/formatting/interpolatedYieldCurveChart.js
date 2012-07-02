/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * The chart component of a detailed InterpolatedYieldCurve view
 */
(function($) {
  
  /** @constructor */
  function InterpolatedYieldCurveChart(_$container) {
    
    var self = this;
    
    var _points = null;
    var _plot;
    var _$tooltip = null;
    var _previousPoint = null;
    var _zoomedRange = null;
    
    var _options = {
        series: {
          lines: {
            show: true,
            lineWidth: 1.5
          },
          shadowSize: 0
        },
        xaxis: {
	  /*
	  min: 1,
          axisLabel: 'Time (years)',
          axisLabelFontSizePixels: 11,
          axisLabelUseCanvas: true
          */
        },
        yaxis: {
	  /*
	  min: 1,
          axisLabel: 'Yield',
          axisLabelFontSizePixels: 11,
          axisLabelUseCanvas: true
          */
        },
        grid: {
          borderWidth: 1,
          clickable: true,
          hoverable: true
        },
        colors: [ 'rgb(71, 113, 135)' ],
        selection: { mode: 'xy' }
      };
    
    function init() {
      _$container.bind("plothover", onChartHover);
      _$container.bind("plotselected", onChartSelected);
    }
    
    function onChartHover(event, pos, item) {
      if (item) {
        if (_previousPoint != item.datapoint) {
          _previousPoint = item.datapoint;
          
          var x = item.datapoint[0].toFixed(4);
          var y = item.datapoint[1].toFixed(4);
          
          removeTooltip();
          showTooltip(item.pageX, item.pageY, "t = " + x + ", yield = " + y);
        }
      }
      else if (_$tooltip) {
        removeTooltip();
        _previousPoint = null;            
      }
    }
    
    function onChartSelected(event, ranges) {
      if (ranges.xaxis.to - ranges.xaxis.from < 0.00001)
          ranges.xaxis.to = ranges.xaxis.from + 0.00001;
      if (ranges.yaxis.to - ranges.yaxis.from < 0.00001)
          ranges.yaxis.to = ranges.yaxis.from + 0.00001;
      
      _zoomedRange = ranges;
      redrawChart(true);
    }
    
    function showTooltip(x, y, contents) {
      _$tooltip = $('<div>' + contents + '</div>').css( {
        position: 'absolute',
        display: 'none',
        top: y + 5,
        left: x + 20,
        padding: '2px',
        'background-color': '#d2eede',
        opacity: 0.80
      }).appendTo("body").fadeIn(200);
    }
    
    function removeTooltip() {
      if (!_$tooltip) {
        return;
      }
      _$tooltip.remove();
      _$tooltip = null;
    }
    
    function redrawChart(fullRedraw) {
      if (!_points || _$container.is(":hidden")) {
        return;
      }
      if (fullRedraw) {
        if (_zoomedRange) {
          var options = $.extend(true, {}, _options, {
            xaxis: { min: _zoomedRange.xaxis.from, max: _zoomedRange.xaxis.to },
            yaxis: { min: _zoomedRange.yaxis.from, max: _zoomedRange.yaxis.to }
          });
        } else {
          var options = _options;
        }
        _chart = $.plot(_$container, [_points], options);
      } else {
        _chart.setData([_points]);
        _chart.draw();
      }
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.setPoints = function(points) {
      var fullRedraw = !_points;
      _points = points;
      redrawChart(fullRedraw);
    }
    
    this.refresh = function() {
      redrawChart(true);
    }
    
    this.restoreState = function() {
      self.refresh();
    }
    
    //-----------------------------------------------------------------------
    
    init();
    
  }
  
  $.extend(true, window, {
    InterpolatedYieldCurveChart: InterpolatedYieldCurveChart
  });

}(jQuery));