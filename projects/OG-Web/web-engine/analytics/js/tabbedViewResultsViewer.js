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
    var _tabManager;
    
    //-----------------------------------------------------------------------
    // Initialization
    
    function init() {
      // Set up UI components
      _tabManager = new TabManager();
      _$tabsContainer = $("<div id='tabs'></div>")
        .append("<ul></ul>");
      _$container.empty().append(_$tabsContainer);
      _$tabsContainer.tabs({
        select : _tabManager.onSelectTab,
        show : _tabManager.onShowTab
      });
      
      var portfolioDetails = _gridStructures.portfolio;
      if (portfolioDetails) {
        var $portfolioContainer = $("<div id='portfolio'></div>");
        _$tabsContainer.append($portfolioContainer);
        var portfolio = new PortfolioViewer($portfolioContainer, portfolioDetails, _liveResultsClient, _userConfig);
        _$tabsContainer.tabs("add", "#portfolio", "Portfolio");
        _tabManager.registerTab("portfolio", portfolio);
      }
      
      var primitivesDetails = _gridStructures.primitives;
      if (primitivesDetails) {
        var $primitivesContainer = $("<div id='primitives'></div>");
        _$tabsContainer.append($primitivesContainer);
        var primitives = new PrimitivesViewer($primitivesContainer, primitivesDetails, _liveResultsClient, _userConfig);
        _$tabsContainer.tabs("add", "#primitives", "Primitives");
        _tabManager.registerTab("primitives", primitives);
      }

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