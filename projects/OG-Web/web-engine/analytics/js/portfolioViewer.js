/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Exposes a portfolio as a hierarchical grid of positions.
 */
(function($) {
  
  /** @constructor */
  function PortfolioViewer(_$container, _$layout, _$popupList, _portfolioDetails, _liveResultsClient, _userConfig) {
    
    var self = this;
    
    var _$portfolioGridContainer;
    var _grid;
    var _gridHelper;
    var _dataView;
    
    function init() {
      _$portfolioGridContainer = $("<div id='portfolioGrid'></div>");
      _$container.append(_$portfolioGridContainer);
      Common.addExportCsvButton(_$container, _liveResultsClient.getCsvGridUrl(self.getGridName()));
      
      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      if (_portfolioDetails.rows) {
        _dataView.setItems(_portfolioDetails.rows, "rowId");
      }
      _dataView.setFilter(dataViewFilter);
      _dataView.endUpdate();
      var leadingColumns = [
          {
            id : "position",
            name : "Position",
            field : "position",
            width : 300,
            formatter : formatPositionName
          },
          {
            id : "quantity",
            name : "Quantity",
            field : "quantity",
            width : 60,
            formatter : formatQuantity
          }
        ];
      var gridColumns = SlickGridHelper.makeGridColumns(self, leadingColumns, _portfolioDetails.columns, 110, _userConfig);
      
      var gridOptions = {
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
        };
      _grid = new Slick.Grid(_$portfolioGridContainer, _dataView.rows, gridColumns, gridOptions);
      _grid.onClick = onGridClicked;

      _gridHelper = new SlickGridHelper(_grid, _dataView, _liveResultsClient.triggerImmediateUpdate, false);
      _gridHelper.afterViewportStable.subscribe(afterGridViewportStable);
      
      _liveResultsClient.beforeUpdateRequested.subscribe(beforeUpdateRequested);
      _liveResultsClient.setPortfolioEventHandler(self);
      
      _userConfig.onSparklinesToggled.subscribe(onSparklinesToggled);
    }
    
    //-----------------------------------------------------------------------
    // Event handling
    
    function beforeUpdateRequested(updateMetadata) {
      _gridHelper.populateViewportData(updateMetadata.portfolioViewport);
    }
    
    function onGridClicked(e, rowIdx, colIdx) {
      if ($(e.target).hasClass("toggle") && $(e.target).closest(".depgraph-popup").length == 0) {
        var row = _dataView.rows[rowIdx];
        if (row) {
          if (!row._collapsed) {
            row._collapsed = true;
          } else {
            row._collapsed = false;
          }
          _dataView.updateItem(row.rowId, row);
          _grid.removeAllRows();
          _grid.render();
          _liveResultsClient.triggerImmediateUpdate();
        }
        return true;
      }
      
      var col = _grid.getColumns()[colIdx];
      if (col.id != "position" && col.id != "quantity") {
        var row = _dataView.rows[rowIdx];
        var $cell = $(e.target).closest(".slick-cell");
        var popupTitle = col.name + " on " + row['position'];
        self.popupManager.openExplain($cell, col.id, row.rowId, popupTitle);
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
    
    function formatPositionName(row, cell, value, columnDef, dataContext) {
      var html = "<span style='display:inline-block;height:1px;width:" + (15 * dataContext["indent"]) + "px'></span>";
      var idx = _dataView.getIdxById(dataContext.rowId);
      var rows = _portfolioDetails.rows;
      if (rows[idx + 1] && rows[idx + 1].indent > rows[idx].indent) {
        if (dataContext._collapsed) {
          html += "<span class='toggle expand'></span>";
        } else {
          html += "<span class='toggle collapse'></span>";
        }
      } else {
        html += "<span class='toggle'></span>";
      }
      html += "<span class='cell-title'>" + value + "</span>";
      return html;
    }
    
    function formatQuantity(row, cell, value, columnDef, dataContext) {
      if (value) {
        var cssClass = "cell-title right";
        if (value < 0) {
          cssClass += " negative";
        }
        return "<div class='" + cssClass + "'>" + value + "</div>";
      } else {
        return "";
      }
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.saveState = function() {
      _gridHelper.saveGridState(_$portfolioGridContainer);
    }

    this.restoreState = function() {
      _gridHelper.restoreGridState(_$portfolioGridContainer);
    }
    
    this.updateReceived = function(update, timestamp, latency) {
      _gridHelper.handleUpdate(update, _portfolioDetails.columns);
    }
    
    this.onContainerResized = function() {
      _gridHelper.handleContainerResized();
    }
    
    this.getGridName = function() {
      return _portfolioDetails.name;
    }

    this.destroy = function() {
      _userConfig.onSparklinesToggled.unsubscribe(onSparklinesToggled);
      
      _liveResultsClient.beforeUpdateRequested.unsubscribe(beforeUpdateRequested);
      _liveResultsClient.setPortfolioEventHandler(null);
      
      _gridHelper.afterViewportStable.unsubscribe(afterGridViewportStable);
      _gridHelper.destroy();
      _grid.onClick = null;
      _grid.destroy();
    }

    //-----------------------------------------------------------------------
    
    init();
    
    this.popupManager = new PopupManager(_$layout, _$popupList, _grid, _portfolioDetails.name, _dataView, _liveResultsClient, _userConfig);
  }
  
  $.extend(true, window, {
    PortfolioViewer : PortfolioViewer
  });
  
}(jQuery));