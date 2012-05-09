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
  function PopupManager(_$layout, _$popupList, _grid, _gridName, _dataView, _liveResultsClient, _userConfig) {

    var self = this;
    var _logger = new Logger("PopupManager", "debug");    
    var _$details = [];
    var _$explains = {};
    
    function getPopup(className, $cell, rowId, colId) {
      var $popup = $("<div class='" + className + " ui-widget ui-widget-content'></div>");
      $popup.appendTo($cell.parent().parent());
      return $popup;
    }
    
    function revealPopup($cell, $popup, component) {
      $popup.position({
        my: "right top",
        at: "right bottom",
        of: $cell,
        offset: "0 -1",
        collision: "none"
      }).show('blind');
    }
    
    function removePopup($popup, component) {
      if (component && component.destroy) {
        component.destroy();
      }
      if ($popup) {
        $popup.remove();
        $popup = null;
      }
    }
    
    function removeDetail(rowId, colId) {
      for (var idx = 0; idx < _$details.length; idx++) {
        var details = _$details[idx];
        var existingRowId = details[2];
        var existingColId = details[3];
        if (existingRowId != rowId || existingColId != colId) {
          continue;
        }
        var component = details[0]
        var $popup = details[1]
        removePopup($popup, component);
        _$details.splice(idx, 1);
        _liveResultsClient.stopDetailedCellUpdates(_gridName, rowId, colId);
        var row = _dataView.getItemById(rowId);
        delete row.detailComponents[colId];
        return true;
      };
      return false;
    }
    
    function closeExplain($popup, $cell, rowId, colId) {
      delete _$explains[rowId][colId];
      if ($.isEmptyObject(_$explains[rowId])) {
        delete _$explains[rowId];
      }
      removePopup($popup);
      
      _liveResultsClient.stopDepGraphExplain(_gridName, rowId, colId);
      var row = _dataView.getItemById(rowId);
      delete row.explainComponents[colId];
      $cell.unbind('mouseenter', handleExplainCellHoverIn);
      $cell.unbind('mouseleave', handleExplainCellHoverOut);
      $cell.removeClass("explain");
      $cell.removeClass("explain-hover");
      
      if (_$popupList.children().size() == 0) {
        _$layout.close('south');
      }
      
      return true;
    }
    
    function handleCloseClick(e) {
      var $popup = $(e.target).closest('.popup');
      var rowId = $popup.data("rowId");
      var colId = $popup.data("colId");
      var $cell = $popup.data("cell");
      closeExplain($popup, $cell, rowId, colId);
    }
    
    function handleExplainPopupHoverIn(e) {
      var $popup = $(e.target).closest(".popup");
      $popup.addClass("popup-hover");
      var $cell = $popup.data("cell");
      $cell.addClass("explain-hover");
    }
    
    function handleExplainPopupHoverOut(e) {
      var $popup = $(e.target).closest(".popup");
      $popup.removeClass("popup-hover");
      var $cell = $popup.data("cell");
      $cell.removeClass("explain-hover");      
    }
    
    function handleExplainCellHoverIn(e) {
      var $cell = $(e.target).closest('.slick-cell');
      $cell.addClass('explain-hover');
      
      var rowId = $cell.data("rowId");
      var colId = $cell.data("colId");
      var $popup = _$explains[rowId][colId];
      $popup.addClass('popup-hover');
    }
    
    function handleExplainCellHoverOut(e) {
      var $cell = $(e.target).closest('.slick-cell');
      $cell.removeClass('explain-hover');
      
      var rowId = $cell.data("rowId");
      var colId = $cell.data("colId");
      var $popup = _$explains[rowId][colId].removeClass("popup-hover");
    }
    
    function associatePopupWithNewCell($cell, $popup, rowId, colId) {
      $popup.data('cell', $cell);
      $cell.hover(handleExplainCellHoverIn, handleExplainCellHoverOut);
      $cell.data('rowId', rowId);
      $cell.data('colId', colId);
      $cell.addClass("explain");
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.toggleDetail = function($cell, columnStructure, formatter, rowId) { 
      var colId = columnStructure.colId;
      if (removeDetail(rowId, colId)) {
        return;
      }
      
      var row = _dataView.getItemById(rowId);
      if (!row.detailComponents) {
        row.detailComponents = {};
      }

      if (!formatter.disableDefaultPopup) {
        var $popup = getPopup('detail-popup', $cell, rowId, colId);
        var $content = $("<div class='detail-content'></div>").appendTo($popup);
      }
      var detailComponent = formatter.createDetail($popup, $content, rowId, columnStructure, _userConfig, row[columnStructure.colId]);
      
      if (detailComponent.resize) {
        var afterResized = function() {
          var detailComponent = $popup.data("component");
          if (detailComponent && detailComponent.resize) {
            detailComponent.resize();
          }
        };
        $popup.resizable({
          handles: 'se',
          helper: 'ui-state-highlight ui-corner-bottom',
          minWidth: 300,
          minHeight: 200,
          stop: afterResized});
      }
      
      if (detailComponent.beforeClosed) {
        // Component has its own close mechanism
        detailComponent.beforeClosed.subscribe(function() {
          self.toggleDetail($cell, columnStructure, formatter, rowId)
        });
      }
      
      if (!formatter.disableDefaultPopup) {
        revealPopup($cell, $popup, detailComponent);
      }
      row.detailComponents[colId] = detailComponent;
      _$details.push([detailComponent, $popup, rowId, colId]);

      _liveResultsClient.startDetailedCellUpdates(_gridName, rowId, colId);
      _liveResultsClient.triggerImmediateUpdate();
    }
    
    this.openExplain = function($cell, colId, rowId, popupTitle) {
      if (_dataView.getItemById(rowId)[colId] == false) {
        // Not in dep graph
        return;
      }
      if (_$explains[rowId] && _$explains[rowId][colId]) {
        // Already exists - find it
        var $popup = _$explains[rowId][colId];
      } else {
        // Create it
        var row = _dataView.getItemById(rowId);
        if (!row.explainComponents) {
          row.explainComponents = {};
        }
        
        _$layout.open('south', true, 'none');
        var $closeText = $("<span class='close'>Close</span>").click(handleCloseClick);
        var $popupHead = $("<div class='popup-head ui-widget-header'></div>").append($closeText).append("<span class='title'>Dependency Graph for " + popupTitle + "</span></div>");
        var $popupContent = $("<div class='popup-content explain'></div>").height(250).width("100%");
        var $popup = $("<li class='popup ui-widget ui-widget-content ui-corner-top'></li>").append($popupHead).append($popupContent).hover(handleExplainPopupHoverIn, handleExplainPopupHoverOut);
        $popup.data('rowId', rowId);
        $popup.data('colId', colId);
        _$popupList.append($popup);
        var depGraphViewer = new DepGraphViewer($popupContent, _gridName, rowId, colId, _liveResultsClient, _userConfig);
        $popupContent.resizable({
          handles: 'se',
          helper: 'ui-state-highlight ui-corner-bottom',
          minHeight: 150,
          resize: function(event, ui) { ui.size.width = "100%"; },
          stop: function() { depGraphViewer.resize(); }
        });
        
        associatePopupWithNewCell($cell, $popup, rowId, colId);
        
        row.explainComponents[colId] = depGraphViewer;
        if (!_$explains[rowId]) {
          _$explains[rowId] = {};
        }
        _$explains[rowId][colId] = $popup;
        
        _liveResultsClient.startDepGraphExplain(_gridName, rowId, colId);
        _liveResultsClient.triggerImmediateUpdate();
      }
      
      // Scroll into view   
      _$popupList.parent().scrollTo($popup, 800);
    }
    
    this.onNewCell = function($cell, rowId, colId) {
      if (!_$explains[rowId]) {
        return;
      }
      var $popup = _$explains[rowId][colId];
      if (!$popup) {
        return;
      }
      associatePopupWithNewCell($cell, $popup, rowId, colId);
    }
    
  }
  
  //-------------------------------------------------------------------------

  $.extend(true, window, {
    PopupManager : PopupManager
  });

}(jQuery));