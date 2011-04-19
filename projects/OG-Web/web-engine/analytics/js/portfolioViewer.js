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
  function PortfolioViewer(_$container, _portfolioDetails, _liveResultsClient, _userConfig) {
    
    var self = this;
    
    var _$portfolioGridContainer;
    var _grid;
    var _gridHelper;
    var _dataView;
    
    function init() {
      _$portfolioGridContainer = $("<div id='portfolioGrid'></div>");
      _$container.append(_$portfolioGridContainer);
      
      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      if (_portfolioDetails.rows) {
        _dataView.setItems(_portfolioDetails.rows, "rowId");
      }
      _dataView.setFilter(dataViewFilter);
      _dataView.endUpdate();
      var gridColumns = SlickGridHelper.makeGridColumns(self, "Position", "position", formatPositionName, _portfolioDetails.columns, _userConfig);
      
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
    
    this.popupManager = new PopupManager(_portfolioDetails.name, _dataView, _liveResultsClient, _userConfig);
  }
  
  $.extend(true, window, {
    PortfolioViewer : PortfolioViewer
  });
  
}(jQuery));