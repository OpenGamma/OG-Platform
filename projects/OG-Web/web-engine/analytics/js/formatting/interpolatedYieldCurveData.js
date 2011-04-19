/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * The data component of a detailed InterpolatedYieldCurve view
 */
(function($) {
  
  /** @constructor */
  function InterpolatedYieldCurveData(_$container) {
    
    var self = this;
    
    var _dataView;
    var _grid;
    var _gridHelper;
    
    function init() {
      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      _dataView.setItems([], 'id');
      _dataView.endUpdate();
      
      var columns = [
          {
            id: 'x',
            name: "t (years)",
            field: 'x',
            width: 150,
            formatter: formatValue
          },
          {
            id: 'y',
            name: "Yield",
            field: 'y',
            width: 150,
            formatter: formatValue
          }
      ];
      
      var options = {
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
          rowHeight: 25
        };

      _grid = new Slick.Grid(_$container, _dataView.rows, columns, options);
      _gridHelper = new SlickGridHelper(_grid, _dataView, null, true);
    }
    
    function formatValue(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-value'>" + value + "</span>";
    }
    
    function getGridRows(data) {
      var rows = [];
      for (i = 0; i < data.length; i++) {
        rows.push({
          id: i,
          x: data[i][0],
          y: data[i][1]
        });
      }
      return rows;
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateReceived = function(update) {
      _dataView.beginUpdate();
      var updateRows = getGridRows(update);
      $.each(updateRows, function(index, row) {
        var gridRow = _dataView.getItemById(row.id);
        if (gridRow == null) {
          _dataView.addItem(row);
        } else {
          _dataView.updateItem(row.id, row);
        }
      });
      _dataView.endUpdate();
    }
    
    this.refresh = function() {
      _grid.resizeCanvas();
    }
    
    this.saveState = function() {
      _gridHelper.saveGridState(_$container);
    }
    
    this.restoreState = function() {
      _gridHelper.restoreGridState(_$container);
    }
    
    this.destroy = function() {
      _gridHelper.destroy();
      _grid.destroy();
    }
    
    //-----------------------------------------------------------------------
    
    init();
    
  }
  
  $.extend(true, window, {
    InterpolatedYieldCurveData: InterpolatedYieldCurveData
  });

}(jQuery));