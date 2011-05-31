/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Top-level wrapper encapsulating the UI components which provide access to the live results of an entire view.
 */
(function($) {
  
  /** @constructor */
  function TabbedViewResultsViewer(_$container, _gridStructures, _liveResultsClient, _userConfig) {
    
    var self = this;
    
    var _logger = new Logger("TabbedViewResultsViewer", "debug");
    var _$tabsContainer;
    var _$popupList;
    var _$layout;
    var _tabManager;
    
    //-----------------------------------------------------------------------
    // Initialization
    
    function init() {
      var $mainContentContainer = $("<div class='ui-layout-center'></div>");
      var $popupContainer = $("<div class='ui-layout-east'></div>");
      _$popupList = $("<ul class='popup-list'></ul>");
      _$popupList.sortable({
        connectWith: _$popupList,
        appendTo: _$popupList,
        handle: '.popup-head',
        cursor: 'move',
        placeholder: 'popup-placeholder',
        helper: function (evt, ui) { return $(ui).clone().appendTo('#resultsViewer').show(); },
        opacity: 0.8
      });

      $popupContainer.append(_$popupList);
      _$container.empty().append($mainContentContainer).append($popupContainer);
      _$layout = _$container.layout({
        defaults: {
          fxName: "slide"
        },
        center: {
          onresize: handleCenterResized
        },
        east: {
          size: 600,
          initClosed: true
        }
      });
      
      _tabManager = new TabManager();
      _$tabsContainer = $("<div id='tabs'></div>")
        .append("<ul></ul>");
      $mainContentContainer.append(_$tabsContainer);
      _$tabsContainer.tabs({
        select : _tabManager.onSelectTab,
        show : _tabManager.onShowTab
      });
      
      var portfolioDetails = _gridStructures.portfolio;
      if (portfolioDetails) {
        var $portfolioContainer = $("<div id='portfolio'></div>");
        _$tabsContainer.append($portfolioContainer);
        _portfolio = new PortfolioViewer($portfolioContainer, _$layout, _$popupList, portfolioDetails, _liveResultsClient, _userConfig);
        _$tabsContainer.tabs("add", "#portfolio", "Portfolio");
        _tabManager.registerTab("portfolio", _portfolio);
      }
      
      var primitivesDetails = _gridStructures.primitives;
      if (primitivesDetails) {
        var $primitivesContainer = $("<div id='primitives'></div>");
        _$tabsContainer.append($primitivesContainer);
        _primitives = new PrimitivesViewer($primitivesContainer, primitivesDetails, _liveResultsClient, _userConfig);
        _$tabsContainer.tabs("add", "#primitives", "Primitives");
        _tabManager.registerTab("primitives", _primitives);
      }

    }
    
    function handleCenterResized() {
      _tabManager.onTabContainerResized();
    }

    //-----------------------------------------------------------------------
    // Public API
    
    this.destroy = function() {
      $.each(_tabManager.getTabs(), function(index, tab) {
        tab.destroy();
      })
      _tabManager = null;
      _$tabsContainer.empty();
      _liveResultsClient.setPrimitivesEventHandler(null);
      _liveResultsClient.setPortfolioEventHandler(null);
    }
    
    //-----------------------------------------------------------------------
    
    init();
    
  }
  
  $.extend(true, window, {
      TabbedViewResultsViewer : TabbedViewResultsViewer
  });
  
}(jQuery));