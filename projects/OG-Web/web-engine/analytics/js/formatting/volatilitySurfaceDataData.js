/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * The data component of a detailed VolatilitySurfaceData view
 */
(function($) {
  
  /** @constructor */
  function VolatilitySurfaceDataData(_$container) {
    
    var self = this;
    
    var _dataView;
    var _fakeGrid = true;
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
    
    function formatHeader(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-header'>" + value + "</span>";
    }
    
    function getGridRows(data) {
      var rows = [];
      var xs = data.xs;
      var ys = data.ys;
      var values = data.surface;
      // work out min and max values
      var minVal = 10000;
      var maxVal = 0;
      for (var i = 0; i < values.length; i++) {
        for (var col=0; col < values[i].length; col++) {
          minVal = Math.min(minVal, values[i][col]);
          maxVal = Math.max(maxVal, values[i][col]);
        }
      }
      
      for (var i = 0; i < values.length; i++) {
        var row = new Object();
        row.name = ys[i];
        row.id = i;
        row.x0 = ys[i];
        for (var col=0; col < values[i].length; col++) {
          var value = values[i][col];
          var brightness = (value - minVal) / (maxVal - minVal); // decimal %
          var colorValue = Math.round((brightness * 64) + 127 + 64).toString(16);
          row['x' + (col + 1)] = '<div style="padding: 2px; background: #ff' + colorValue + colorValue + ';">' + value.toFixed(2) + '</div>';
        }
        rows.push(row);
      }
      return rows;
    }
    
    function lazyCreateGrid(data) {
      if (!_fakeGrid) {
        return;
      }
      _fakeGrid = false;
      var xs = data.xs;
      var columns = [];
      columns.push({
        id : 'x0',
        name : 'Swap Length \\ Expiry',
        field : 'x0',
        width : 120,
        formatter: formatHeader
      });
      for (var i=1; i<xs.length+1; i++) {
        var column = { 
          id : 'x' + i,
          name: xs[i-1],
          field : 'x' + i,
          width: 45,
          formatter: formatValue
        }
        columns.push(column);
      }
      var options = {
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
          rowHeight: 25
        };

      _grid = new Slick.Grid(_$container, _dataView.rows, columns, options);

      _gridHelper = new SlickGridHelper(_grid, _dataView, null, true);

      _grid.resizeCanvas();
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateReceived = function(update) {
      lazyCreateGrid(update);
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
    VolatilitySurfaceDataData: VolatilitySurfaceDataData
  });

}(jQuery));