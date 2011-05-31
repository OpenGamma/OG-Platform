/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Top-level column formatter which may delegate to the column's specific data type formatter.
 */
(function($) {
  
  function ColumnFormatter(_viewer, _columnStructure, _userConfig) {
    
    var self = this;
    
    this.formatCell = function(row, cell, value, columnDef, dataContext) {
      return "<span class='cell-value'>Loading...</span>";
    }
    
    this.postRender = function(cellNode, row, dataContext, colDef) {
      var $cell = $(cellNode);
      if (!$cell.popupChecked && $cell.hasClass('highlighted')) {
        // Must be a cell for which there is an associated popup
        $cell.popupChecked = true;
        _viewer.popupManager.onNewCell($cell, row, colDef.id);
      }
      if (!dataContext.dataReceived) {
        return;
      }
      var value = dataContext[colDef.field];
      if (!value) {
        $cell.empty().html("<span class='cell-value'>" + _columnStructure.nullValue + "</span>");
        return;
      }
      
      var $cellContents = $cell.data("contents");
      if (!$cellContents) {
        $cellContents = $("<div></div>").addClass("cell-contents").appendTo($cell.empty());
        $cell.data("contents", $cellContents);
        if (_columnStructure.typeFormatter.createDetail) {
          $("<div class='imgbutton revealmore'></div>")
              .appendTo($cell)
              .position({
                my: "right bottom",
                at: "right bottom",
                of: $cell,
                offset: "-5 -4",
                collision: "none"
              })
              .button({ icons: { primary:'ui-icon-carat-1-s' }, text: false })
              .click(function(ui) {
                ui.stopPropagation();
                _viewer.popupManager.toggleDetail($cell, _columnStructure, row);
              });
        }
      }
      if (_columnStructure.typeFormatter.renderCell) {
        _columnStructure.typeFormatter.renderCell($cellContents, value, row, dataContext, colDef, _columnStructure, _userConfig);
      }
    }
    
  }
  
  ColumnFormatter.getTypeFormatter = function(dataType) {
    if (!dataType) {
      return UnknownTypeFormatter;
    }
    
    switch (dataType) {
      case 'PRIMITIVE':
        return PrimitiveFormatter;
      case 'DOUBLE':
        return DoubleFormatter;
      case 'YIELD_CURVE':
        return InterpolatedYieldCurveFormatter;
      case 'VOLATILITY_SURFACE_DATA':
        return VolatilitySurfaceDataFormatter;
      case 'LABELLED_MATRIX_1D':
        return LabelledMatrix1DFormatter;
      default:
        return UnknownTypeFormatter;
    }
  }
  
  $.extend(true, window, {
    ColumnFormatter : ColumnFormatter
  });

}(jQuery));