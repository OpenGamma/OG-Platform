/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Adds common functionality to a SlickGrid that is backed by a DataView.
 */
(function($) {
  
  /** @constructor */
  function SlickGridHelper(_grid, _dataView, _triggerRemoteUpdate, _fullRenderAfterUpdate) {

    var self = this;
    
    var _logger = new Logger("SlickGridHelper", "debug");
    
    var _dataView;
    var _grid;
    var _scrollUpdateRequestTimerId = -1;
    
    var _scrollPosition;
    
    function init() {
      _grid.onViewportChanged = onGridViewportChanged;
      _grid.onColumnsReordered = onColumnsReordered;
      _grid.onColumnsResized = onColumnsResized;
      _dataView.onRowCountChanged.subscribe(onDataViewRowCountChanged);
      _dataView.onRowsChanged.subscribe(onDataViewRowsChanged);
    }
    
    //-----------------------------------------------------------------------
    // Grid events
    
    function onDataViewRowCountChanged(args) {
      _grid.updateRowCount();
      _grid.render();
    }
    
    function onDataViewRowsChanged(rows) {
      if (_fullRenderAfterUpdate) {
        _grid.removeRows(rows);
        _grid.render();
      } else {
        _grid.reprocessRows(rows);
      }
    }
    
    //-----------------------------------------------------------------------
    // Scrolling
    
    function onGridViewportChanged() {
      startScrollUpdateRequestTimer();
      self.onViewportChanged.fire();
    }
    
    function onColumnsReordered() {
      reloadViewportData();
    }
    
    function onColumnsResized() {
      reloadViewportData();
    }
    
    function cancelScrollUpdateRequestTimer() {
      clearTimeout(_scrollUpdateRequestTimerId);
      _scrollUpdateRequestTimerId = -1;
    }
    
    function startScrollUpdateRequestTimer() {
      // Keep resetting the timer as the user scrolls so that 100 ms after they stop scrolling we request an
      // immediate update
      cancelScrollUpdateRequestTimer();
      _scrollUpdateRequestTimerId = setTimeout(handleScrollUpdateRequestTimer, 100);
    }
    
    function handleScrollUpdateRequestTimer() {
      _scrollUpdateRequestTimerId = -1;
      self.afterViewportStable.fire();
    }

    //-----------------------------------------------------------------------
    // Public API
    
    this.populateViewportData = function(viewportData) {
      if (!viewportData.rowIds) {
        viewportData.rowIds = new Array();
      }
      if (!viewportData.lastTimestamps) {
        viewportData.lastTimestamps = new Array();
      }

      var vp = _grid.getViewport();
      var bottom = Math.min(vp.bottom, _dataView.rows.length - 1);
      for (var current = vp.top; current <= bottom; current++) {
        var row = _dataView.rows[current]; // remember we're interested in VISIBLE rows, post filtering, here.
        viewportData.rowIds.push(row.rowId);
        viewportData.lastTimestamps.push(row.latestTimeStamp);
      }
    }
    
    this.onViewportChanged = new EventManager();
    this.afterViewportStable = new EventManager();
    
    this.saveGridState = function($gridContainer) {
      _scrollPosition = $gridContainer.children(".slick-viewport").scrollTop()
    }
    
    this.handleUpdate = function(update, columns) {
      _dataView.beginUpdate();
      $.each(update, function(index, row) {
        var gridRow = _dataView.getItemById(row.rowId);
        if (gridRow == null) {
          return;
        }
        gridRow.dataReceived = true;
        var detailComponents = gridRow.detailComponents;
        var explainComponents = gridRow.explainComponents;
        for (var columnIdx in columns) {
          var column = columns[columnIdx];
          var latestValue = row[column.colId];
          if (latestValue === null) {
            // Null column value means no result expected
            gridRow[column.colId] = false;
            continue;
          }
          if (latestValue == undefined) {
            continue;
          }
          if (detailComponents && detailComponents[column.colId]) {
            detailComponents[column.colId].updateValue(latestValue.v);
          }
          if (explainComponents && explainComponents[column.colId] && latestValue.dg) {
            explainComponents[column.colId].updateValue(latestValue.dg);
          }
          if (latestValue.dg && !latestValue.v) {
            // Row is out of viewport
            continue;
          }
          var formatter = ColumnFormatter.getFormatterForCell(column, latestValue.t);
          if (!gridRow[column.colId]) {
            gridRow[column.colId] = {};
          }
          if (latestValue.t) {
            gridRow[column.colId].t = latestValue.t;
          }
          gridRow[column.colId].v = latestValue.v;
          if (formatter.supportsHistory) {
            // Push the history in
            if (!gridRow[column.colId].h) {
              gridRow[column.colId].h = new Array();
            }
            if (latestValue.h) {
              gridRow[column.colId].h = gridRow[column.colId].h.concat(latestValue.h);
              var historyCount = gridRow[column.colId].h.length;
              if (gridRow[column.colId].h.length > 20) {
                gridRow[column.colId].h = gridRow[column.colId].h.slice(historyCount - 20);
              }
            }
          }
        }
        _dataView.updateItem(row.rowId, gridRow);
      });
      _dataView.endUpdate();
    }
    
    this.restoreGridState = function($gridContainer) {
      _grid.resizeCanvas();
      $gridContainer.children(".slick-viewport").scrollTop(_scrollPosition);
    
      reloadViewportData();
    }
    
    this.handleContainerResized = function() {
      _grid.resizeCanvas();
      self.afterViewportStable.fire();
    }
    
    this.destroy = function() {
      _grid.onViewportChanged = null;
      _grid.onColumnsReordered = null;
      _grid.onColumnsResized = null;
      self.onViewportChanged = null;
      self.afterViewportStable = null;
    }
    
    //-----------------------------------------------------------------------
    
    function reloadViewportData() {
      _grid.removeAllRows();
      _grid.render();
      if (_triggerRemoteUpdate) {
        _triggerRemoteUpdate();
      }
    }
    
    //-----------------------------------------------------------------------
    
    init();
    
  }
  
  //-----------------------------------------------------------------------
  // Static API
    
  SlickGridHelper.makeGridColumns = function(viewer, titleColumn, columns, userConfig) {
    var gridColumns = [];
    gridColumns.push(titleColumn);
    if (columns) {
      for (colId in columns) {
        var column = columns[colId];
        var formatter = new ColumnFormatter(viewer, column, userConfig);
        gridColumns.push({
          id : column.colId,
          name : column.header,
          toolTip: column.description,
          field : column.colId,
          width : column.width ? column.width : 150, 
          formatter: formatter.formatCell,
          asyncPostRender: formatter.postRender
        });
      }
    }
    return gridColumns;
  }
  
  SlickGridHelper.formatCellWithToggle = function(rows, rowIndex, dataContext, value) {
    var indent = dataContext["indent"];
    var isCollapsed = dataContext._collapsed;
    var html = "<span style='display:inline-block;height:1px;width:" + (15 * indent) + "px'></span>";
    if (rows[rowIndex + 1] && rows[rowIndex + 1].indent > rows[rowIndex].indent) {
      if (isCollapsed) {
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

  $.extend(true, window, {
    SlickGridHelper : SlickGridHelper
  });

}(jQuery));