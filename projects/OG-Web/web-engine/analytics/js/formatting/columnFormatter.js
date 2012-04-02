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
      if (!$cell.data('popupChecked')) {
        $cell.data('popupChecked', true);
        _viewer.popupManager.onNewCell($cell, dataContext.rowId, colDef.id);
      }
      if (!dataContext.dataReceived) {
        return;
      }
      var value = dataContext[colDef.field];
      if (!value || !value.v) {
        if (value == false) {
          $cell.empty();
        } else {
          $cell.empty().html("<span class='cell-value'>" + _columnStructure.nullValue + "</span>");
        }
        return;
      }
      
      var $cellContents = $cell.data("contents");
      var formatter = ColumnFormatter.getFormatterForCell(_columnStructure, value.t);
      if (!$cellContents) {
        $cellContents = $("<div></div>").addClass("cell-contents").appendTo($cell.empty());
        $cell.data("contents", $cellContents);
        if (formatter.createDetail) {
          var rowId = dataContext['rowId'];
          var $revealButton = $("<div class='imgbutton revealmore'></div>")
              .appendTo($cell)
              .position({
                my: "right bottom",
                at: "right bottom",
                of: $cell,
                offset: "-5 -4",
                collision: "none"
              })
              .button({ icons: { primary: formatter.customDetailIcon ? formatter.customDetailIcon : 'ui-icon-carat-1-s' }, text: false })
              .click(function(ui) {
                ui.stopPropagation();
                _viewer.popupManager.toggleDetail($cell, _columnStructure, formatter, rowId);
              })
              .hide();
          var handleCellHoverIn = function(e) { $revealButton.fadeTo(200, 1) };
          var handleCellHoverOut = function(e) { $revealButton.fadeTo(200, 0) };
          $cell.hover(handleCellHoverIn, handleCellHoverOut);
        }
      }
      if (formatter.renderCell) {
        formatter.renderCell($cellContents, value, row, dataContext, colDef, _columnStructure, _userConfig);
      }
    }
    
  }
  
  ColumnFormatter.getFormatterForCell = function(columnStructure, dynamicDataType) {
    if (columnStructure.dynamic && dynamicDataType) {
      return ColumnFormatter.getFormatterForType(dynamicDataType);
    } else {
      // Use cached lookup
      return columnStructure.typeFormatter;
    }
  }
  
  ColumnFormatter.getFormatterForType = function(dataType) {
    if (!dataType) {
      return UnknownTypeFormatter;
    }
    
    switch (dataType) {
      case 'PRIMITIVE':
        return PrimitiveFormatter;
      case 'DOUBLE':
        return DoubleFormatter;
      case 'CURVE':
        return InterpolatedYieldCurveFormatter;
      case 'SURFACE_DATA':
        return VolatilitySurfaceDataFormatter;
      case 'LABELLED_MATRIX_1D':
        return LabelledMatrix1DFormatter;
      case 'LABELLED_MATRIX_2D':
        return LabelledMatrix2DFormatter;
      case 'TIME_SERIES':
        return TimeSeriesFormatter;
      default:
        return UnknownTypeFormatter;
    }    
  }
  
  $.extend(true, window, {
    ColumnFormatter : ColumnFormatter
  });

}(jQuery));