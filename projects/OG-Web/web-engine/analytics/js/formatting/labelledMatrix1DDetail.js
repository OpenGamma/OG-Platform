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
  function LabelledMatrix1DDetail(_$popup, _$container, _rowId, _colId) {
    
    var self = this;
    
    var _dataView;
    var _grid;
    var _gridHelper;
    
    function init() {
      // A bit nasty, but popups need some more thought (maybe there's a need for different styles of popup)
      // This solves the problem for now
      _$popup.css('padding', 0);
      _$popup.width(300);
      
      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      _dataView.setItems([], 'key');
      _dataView.endUpdate();
      
      var columns = [
        {
          id: 'key',
          name: "Label",
          field: 'key',
          width: 150,
          resizable: false,
          formatter: formatValue
        },
        {
          id: 'value',
          name: "Value",
          field: 'value',
          width: 150,
          resizable: false,
          formatter: formatValue
        }
      ];
     
      var options = {
          autoHeight: true,
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
          rowHeight: 20
        };

      var $gridContainer = $("<div class='labelled-matrix-grid'></div>").appendTo(_$container);
      _grid = new Slick.Grid($gridContainer, _dataView.rows, columns, options);
      _gridHelper = new SlickGridHelper(_grid, _dataView, null, true);
    }
    
    function formatValue(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-value'>" + value + "</span>";
    }
    
    function getGridRows(fullVector) {
      var rows = [];
      for (var key in fullVector) {
        rows.push({
          key: key,
          value: fullVector[key]
        });
      }
      return rows;
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateValue = function(update) {
      var fullVector = update.full;
      if (!fullVector) {
        return;
      }

      _dataView.beginUpdate();
      var updateRows = getGridRows(fullVector);
      $.each(updateRows, function(index, row) {
        var gridRow = _dataView.getItemById(row.key);
        if (gridRow == null) {
          _dataView.addItem(row);
        } else {
          _dataView.updateItem(row.key, row);
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
    LabelledMatrix1DDetail: LabelledMatrix1DDetail
  });

}(jQuery));