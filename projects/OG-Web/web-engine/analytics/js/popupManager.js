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
  function PopupManager(_gridName, _dataView, _liveResultsClient, _userConfig) {

    var self = this;
    var _logger = new Logger("PopupManager", "debug");    
    var _$popup;
    
    function getPopup($cell, rowId, colId) {
      var popup = $("<div class='detail-popup ui-widget ui-widget-content'></div>");
      popup.data("rowId", rowId);
      popup.data("colId", colId);
      popup.appendTo($cell.parent().parent());
      return popup;
    }
    
    function revealPopup($cell, $popup) {
      $popup.position({
        my: "right top",
        at: "right bottom",
        of: $cell,
        offset: "0 -1",
        collision: "none"
      }).show('blind');
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.togglePopup = function($cell, columnStructure, rowId) {
      var colId = columnStructure.colId;
           
      // Only allow one popup at a time
      if (_$popup) {
        var oldRowId = _$popup.data("rowId");
        var oldColId = _$popup.data("colId");
        
        var component = _$popup.data("component");
        if (component && component.destroy) {
          component.destroy();
        }
        _$popup.remove();
        _$popup = null;
        
        _liveResultsClient.stopDetailedCellUpdates(_gridName, oldRowId, oldColId);
        var row = _dataView.getItemById(oldRowId);
        delete row.detailComponents[oldColId];
        
        if (rowId == oldRowId && colId == oldColId) {
          return;
        }
      }
      
      var row = _dataView.getItemById(rowId);
      if (!row.detailComponents) {
        row.detailComponents = {};
      }

      _$popup = getPopup($cell, rowId, colId);
      var $content = $("<div class='detail-content'></div>").appendTo(_$popup);
      var detailComponent = columnStructure.typeFormatter.createDetail(_$popup, $content, rowId, columnStructure, _userConfig);
      
      if (detailComponent.resize) {
        var afterResized = function() {
          var popupComponent = _$popup.data("component");
          if (popupComponent && popupComponent.resize) {
            popupComponent.resize();
          }
        };
        _$popup.addClass('ui-corner-bottom');
        _$popup.resizable({
          handles: 'se',
          helper: 'ui-state-highlight ui-corner-bottom',
          minWidth: 300,
          minHeight: 200,
          stop: afterResized});
      }
      
      revealPopup($cell, _$popup);
      _$popup.data("component", detailComponent);
      _$popup.data("rowId", rowId);
      _$popup.data("colId", columnStructure.colId);
      row.detailComponents[colId] = detailComponent;

      _liveResultsClient.startDetailedCellUpdates(_gridName, rowId, colId);
      _liveResultsClient.triggerImmediateUpdate();
    }
    
  }
  
  //-------------------------------------------------------------------------

  $.extend(true, window, {
    PopupManager : PopupManager
  });

}(jQuery));