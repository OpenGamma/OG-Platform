/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Renders a detailed view of a LabelledMatrix2D
 */
(function($) {
  
  /** @constructor */
  function LabelledMatrix2DDetail(_$popup, _$container, _rowId, _colId, _data) {
    
    var self = this;
    
    var _dataView;
    var _grid;
    var _gridHelper;
    
    function init() {
      // TODO: probably want to limit the size
      if (_data.v.summary) {
        var colCount = parseInt(_data.v.summary.colCount) + 1;
        _$popup.width(colCount * 75);
      } else {
        _$popup.width(500);
      }

      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      _dataView.setItems([], 'id');
      _dataView.endUpdate(); 
    }
    
    function formatValue(row, cell, value, columnDef, dataContext) {
      var cssClass = row == 0 || cell == 0 ? 'labelled-matrix-header' : 'cell-value';
      return "<span class='" + cssClass + "'>" + value + "</span>";
    }
    
    function getGridRows(data) {
      var rows = [];
      var xs = data.x;
      var ys = data.y;
      var values = data.matrix;
      
      var header = new Object();
      header.name = '';
      header.id = 0;
      header.x0 = '';
      for (var i = 0; i < xs.length; i++) {
        header['x' + (i + 1)] = xs[i];
      }
      rows.push(header);
      for (var i = 0; i < values.length; i++) {
        var row = new Object();
        row.name = ys[i];
        row.id = i + 1;
        row.x0 = ys[i];
        for (var col=0; col < values[i].length; col++) {
          var value = values[i][col];
          if (value) {
            value = value.toFixed(4);
          }
          row['x' + (col + 1)] = value;
        }
        rows.push(row);
      }
      return rows;
    }
    
    function lazyCreateGrid(data) {
      if (_grid) {
        return;
      }
      var xs = data.x;
      var columns = [];
      columns.push({
        id : 'x0',
        name : '',
        field : 'x0',
        width : 75,
        formatter: formatValue
      });
      for (var i=0; i<xs.length; i++) {
        var column = { 
          id : 'x' + (i + 1),
          name: xs[i],
          field : 'x' + (i + 1),
          width: 75,
          formatter: formatValue
        }
        columns.push(column);
      }
      var options = {
          autoHeight: true,
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
          rowHeight: 25
        };
      _grid = new Slick.Grid(_$container, _dataView.rows, columns, options);
      _gridHelper = new SlickGridHelper(_grid, _dataView, null, true);
      _$container.find(".slick-header-columns").css('height', '0');
      _$container.find(".slick-header").css('border', 'none');
      _grid.resizeCanvas();
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateValue = function(update) {
      if (!update) {
        return;
      }
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
    
    this.destroy = function() {
      if (_gridHelper) {
        _gridHelper.destroy();
      }
      if (_grid) {
        _grid.destroy();
      }
    }
    
    //-----------------------------------------------------------------------

    init();
    
  }
  
  $.extend(true, window, {
    LabelledMatrix2DDetail : LabelledMatrix2DDetail
  });

}(jQuery));
 