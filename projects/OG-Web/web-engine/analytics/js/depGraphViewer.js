/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Explains a computed value by displaying a hierarchy of its inputs.
 */
(function($) {
  
  /** @constructor */
  function DepGraphViewer(_$container, _parentGridName, rowId, colId, _liveResultsClient, _userConfig) {
    
    var self = this;
    var _logger = new Logger("DepGraphViewer", "debug");
    
    var _grid;
    var _gridHelper;
    var _dataView;
    var _columns;
    var _fullRows
    
    function init() {
      _dataView = new Slick.Data.DataView();
      
      var targetColumn = {
          id : "target",
          name : "Target",
          field : "target",
          width : 300,
          formatter : formatTargetName
        };
      _columns = [
          {
            colId: 'targetType',
            header: "Type",
            typeFormatter: PrimitiveFormatter,
            width: 40
          },
          {
            colId: 'valueName',
            header: "Value Name",
            typeFormatter: PrimitiveFormatter
          },
          {
            colId: 0,
            header: "Value",
            typeFormatter: PrimitiveFormatter,
            nullValue: "null",
            dynamic: true,
            width: 250
          },        
          {
            colId: 'function',
            header: "Function",
            typeFormatter: PrimitiveFormatter,
            nullValue: "null",
            width: 200
          },
          {
            colId: 'properties',
            header: "Properties",
            typeFormatter: PrimitiveFormatter,
            nullValue: "",
            width: 400
          }
      ];
      var gridColumns = SlickGridHelper.makeGridColumns(self, [targetColumn], _columns, 150, _userConfig);
      
      var gridOptions = {
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
        };
      var $depGraphGridContainer = $("<div class='grid'></div>").appendTo(_$container);
      _grid = new Slick.Grid($depGraphGridContainer, _dataView.rows, gridColumns, gridOptions);
      _grid.onClick = onGridClicked;

      _gridHelper = new SlickGridHelper(_grid, _dataView, _liveResultsClient.triggerImmediateUpdate, false);
      _gridHelper.afterViewportStable.subscribe(afterGridViewportStable);
      
      _liveResultsClient.beforeUpdateRequested.subscribe(beforeUpdateRequested);
      
      _userConfig.onSparklinesToggled.subscribe(onSparklinesToggled);
    }
    
    function formatValue(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-value'>" + value + "</span>";
    }
    
    //-----------------------------------------------------------------------
    // Event handling
    
    function beforeUpdateRequested(updateMetadata) {
      var gridId = _parentGridName + "-" + rowId + "-" + colId;
      updateMetadata.depGraphViewport[gridId] = {};
      _gridHelper.populateViewportData(updateMetadata.depGraphViewport[gridId]);
    }
    
    function onGridClicked(e, row, cell) {
      if ($(e.target).hasClass("toggle")) {
        var item = _dataView.rows[row];
        if (item) {
          if (!item._collapsed) {
            item._collapsed = true;
          } else {
            item._collapsed = false;
          }
          _dataView.updateItem(item.rowId, item);
          _grid.removeAllRows();
          _grid.render();
          _liveResultsClient.triggerImmediateUpdate();
        }
        return true;
      }
      return false;
    }
    
    function afterGridViewportStable() {
      _liveResultsClient.triggerImmediateUpdate();
    }
    
    function onSparklinesToggled(sparklinesEnabled) {
      _grid.reprocessAllRows();
    }
    
    //-----------------------------------------------------------------------
    
    function dataViewFilter(item) {
      var idx = _dataView.getIdxById(item.rowId);
      if (item.parentRowId != null) {
        var parent = _dataView.getItemById(item.parentRowId);
        while (parent) {
          if (parent._collapsed) {
            return false;
          }
          parent = _dataView.getItemById(parent.parentRowId);
        }
      }
      return true;
    }
    
    function formatTargetName(row, cell, value, columnDef, dataContext) {
      var rowIndex = _dataView.getIdxById(dataContext.rowId);
      if (!_fullRows) {
        return null;
      } else {
        return SlickGridHelper.formatCellWithToggle(_fullRows, rowIndex, dataContext, value);
      }
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.updateValue = function(update) {
      if (!update) {
        return;
      }
      if (!_fullRows) {
        if (!update['grid']) {
          // Cannot do anything with the update
          _logger.warn("Dependency graph update received without grid structure");
          return;
        }
        self.popupManager = new PopupManager(null, null, null, update['grid']['name'], _dataView, _liveResultsClient, _userConfig);
        _fullRows = update['grid']['rows'];
        $.each(_fullRows, function(idx, row) {
          if (row.indent >= 2) {
            row._collapsed = true;
          }
        });
        _dataView.beginUpdate();
        _dataView.setItems(_fullRows, 'rowId');
        _dataView.setFilter(dataViewFilter);
        _dataView.endUpdate();
        _gridHelper.handleUpdate(update['update'], _columns);
      } else {
        _gridHelper.handleUpdate(update, _columns);
      }
    }
    
    this.resize = function() {
      _grid.resizeCanvas();
    }

    this.destroy = function() {
      _userConfig.onSparklinesToggled.unsubscribe(onSparklinesToggled);
      
      _liveResultsClient.beforeUpdateRequested.unsubscribe(beforeUpdateRequested);
      
      _gridHelper.destroy();
      _grid.onClick = null;
      _grid.destroy();
    }

    //-----------------------------------------------------------------------
    
    init();
  }
  
  $.extend(true, window, {
    DepGraphViewer : DepGraphViewer
  });
  
}(jQuery));