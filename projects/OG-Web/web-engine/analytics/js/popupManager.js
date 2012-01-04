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
      $popup.data("rowId", rowId);
      $popup.data("colId", colId);
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
      $popup.data("component", component);
    }
    
    function removePopup($popup) {
      var component = $popup.data("component");
      if (component && component.destroy) {
        component.destroy();
      }
      $popup.remove();
      $popup = null;      
    }
    
    function removeDetail(rowId, colId) {
      for (var idx = 0; idx < _$details.length; idx++) {
        var $popup = _$details[idx];
        var existingRowId = $popup.data("rowId");
        var existingColId = $popup.data("colId");
        if (existingRowId != rowId || existingColId != colId) {
          continue;
        }
        removePopup($popup);
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
      
      _liveResultsClient.stopDepGraphExplain(rowId, colId);
      var row = _dataView.getItemById(rowId);
      delete row.explainComponents[colId];
      $cell.unbind('mouseenter', handleExplainCellHoverIn);
      $cell.unbind('mouseleave', handleExplainCellHoverOut);
      $cell.removeClass("explain");
      $cell.removeClass("explain-hover");
      
      if ($.isEmptyObject(_$explains)) {
        _$layout.close('east');
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

      var $popup = getPopup('detail-popup', $cell, rowId, colId);
      var $content = $("<div class='detail-content'></div>").appendTo($popup);
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
      
      revealPopup($cell, $popup, detailComponent);
      row.detailComponents[colId] = detailComponent;
      _$details.push($popup);

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
        
        _$layout.open('east', true, 'none');
        var $closeText = $("<span class='close'>Close</span>").click(handleCloseClick);
        var $popupHead = $("<div class='popup-head ui-widget-header'></div>").append($closeText).append("<span class='title'>Dependency Graph for " + popupTitle + "</span></div>");
        var $popupContent = $("<div class='popup-content explain'></div>").height(300).width("100%");
        var $popup = $("<li class='popup ui-widget ui-widget-content ui-corner-top'></li>").append($popupHead).append($popupContent).hover(handleExplainPopupHoverIn, handleExplainPopupHoverOut);
        $popup.data('rowId', rowId);
        $popup.data('colId', colId);
        _$popupList.append($popup);
        var depGraphViewer = new DepGraphViewer($popupContent, rowId, colId, _liveResultsClient, _userConfig);
        $popupContent.resizable({
          handles: 'se',
          helper: 'ui-state-highlight ui-corner-bottom',
          minHeight: 200,
          resize: function(event, ui) { ui.size.width = "100%"; },
          stop: function() { depGraphViewer.resize(); }
        });
        
        associatePopupWithNewCell($cell, $popup, rowId, colId);
        
        row.explainComponents[colId] = depGraphViewer;
        if (!_$explains[rowId]) {
          _$explains[rowId] = {};
        }
        _$explains[rowId][colId] = $popup;
        
        _liveResultsClient.startDepGraphExplain(rowId, colId);
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