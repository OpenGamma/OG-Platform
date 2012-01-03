/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Exposes a set of primitives in a grid.
 */
(function($) {
  
  /** @constructor */
  function PrimitivesViewer(_$container, _primitivesDetails, _liveResultsClient, _userConfig) {
    
    var self = this;
    
    var _$primitivesGridContainer;
    var _grid;
    var _gridHelper;
    
    function init() {
      _$primitivesGridContainer = $("<div id='primitivesGrid'></div>");
      _$container.append(_$primitivesGridContainer);
      Common.addExportCsvButton(_$container, _liveResultsClient.getCsvGridUrl(self.getGridName()));
      
      _dataView = new Slick.Data.DataView();
      _dataView.beginUpdate();
      if (_primitivesDetails.rows) {
        _dataView.setItems(_primitivesDetails.rows, "rowId");
      }
      _dataView.endUpdate();
      var targetColumn = {
          id : "name",
          name : "Target",
          field : "name",
          width : 300,
          formatter : formatTargetName
        };
      var gridColumns = SlickGridHelper.makeGridColumns(self, [targetColumn], _primitivesDetails.columns, 150, _userConfig);
      
      var gridOptions = {
          editable: false,
          enableAddRow: false,
          enableCellNavigation: false,
          asyncEditorLoading: false,
          rowHeight: 50
        };
      _grid = new Slick.Grid(_$primitivesGridContainer, _dataView.rows, gridColumns, gridOptions);
      _grid.afterRender = afterGridRendered;
      
      _gridHelper = new SlickGridHelper(_grid, _dataView, _liveResultsClient.triggerImmediateUpdate, false);
      _gridHelper.afterViewportStable.subscribe(afterGridViewportStable);
      
      _liveResultsClient.beforeUpdateRequested.subscribe(beforeUpdateRequested);
      _liveResultsClient.setPrimitivesEventHandler(self);
      
      _userConfig.onSparklinesToggled.subscribe(onSparklinesToggled);
    }
    
    //-----------------------------------------------------------------------
    // Event handling
    
    function beforeUpdateRequested(updateMetadata) {
      _gridHelper.populateViewportData(updateMetadata.primitiveViewport);
    }
    
    function afterGridRendered(container) {
      if (_userConfig.getSparklinesEnabled()) {
        $.sparkline_display_visible();
        _$primitivesGridContainer.find('.primitive-history-sparkline.raw').toggleClass('raw').sparkline('html', { width: 50 });
      }
    }
    
    function afterGridViewportStable() {
      _liveResultsClient.triggerImmediateUpdate();
    }
    
    function onSparklinesToggled(sparklinesEnabled) {
      _grid.reprocessAllRows();
    }
    
    //-----------------------------------------------------------------------
    
    function formatTargetName(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-title'>" + value + "</span>";
    }
    
    function formatValue(row, cell, value, columnDef, dataContext) {
      var idx = _dataView.getIdxById(dataContext.rowId);
      var cellValue = dataContext.dataReceived
          ? value ? value : ""
          : "Loading...";
      return "<span class='cell-value'>" + cellValue + "</span>";
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.saveState = function() {
      _gridHelper.saveGridState(_$primitivesGridContainer);
    }

    this.restoreState = function() {
      _gridHelper.restoreGridState(_$primitivesGridContainer);
    }
    
    this.updateReceived = function(update, timestamp, latency) {
      _gridHelper.handleUpdate(update, _primitivesDetails.columns);
    }
    
    this.onContainerResized = function() {
      _gridHelper.handleContainerResized();
    }
    
    this.getGridName = function() {
      return _primitivesDetails.name;
    }

    this.destroy = function() {
      _userConfig.onSparklinesToggled.unsubscribe(onSparklinesToggled);
      
      _liveResultsClient.beforeUpdateRequested.unsubscribe(beforeUpdateRequested);
      _liveResultsClient.setPrimitivesEventHandler(null);
      
      _gridHelper.afterViewportStable.unsubscribe(afterGridViewportStable);
      _gridHelper.destroy();
      _grid.destroy();
    }

    // -----------------------------------------------------------------------
    
    init();
    
    this.popupManager = new PopupManager(null, null, null, _primitivesDetails.name, _dataView, _liveResultsClient, _userConfig);
    
  }
  
  $.extend(true, window, {
    PrimitivesViewer : PrimitivesViewer
  });
  
}(jQuery));