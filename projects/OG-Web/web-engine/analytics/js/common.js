(function($) {
  
  //-------------------------------------------------------------------------
  // Events

  function EventManager() {
    var _handlers = [];
    
    this.subscribe = function(handler) {
      _handlers.push(handler);
    }
    
    this.unsubscribe = function(handler) {
      for (var i = 0; i < _handlers.length; i++)
      {
         if (_handlers[i] == handler) {
             _handlers.splice(i,1);
             return;
         }
       }
    }
    
    this.fire = function(args) {
      $.each(_handlers, function(index, handler) {
        handler(args);
      });
    }
  }
  
  //-------------------------------------------------------------------------
  // Logging
  
  function Logger(_name, _minLevel) {
    
    function log(level, args) {
      if (typeof(console) == 'undefined') {
        // IE
        return;
      }
      
      console.log(getTimeString() + " [" + _name + "] " + level + " - " + args);      
    }
    
    function getTimeString() {
      var date = new Date();
      var hours = date.getHours();
      if (hours < 10) hours = "0" + hours;
      var minutes = date.getMinutes();
      if (minutes < 10) minutes = "0" + minutes;
      var seconds = date.getSeconds();
      if (seconds < 10) seconds = "0" + seconds;
      var milliseconds = date.getMilliseconds();
      if (milliseconds < 100) milliseconds = "0" + milliseconds;
      if (milliseconds < 10) milliseconds = "0" + milliseconds;
      return hours + ":" + minutes + ":" + seconds + "." + milliseconds;
    }
    
    this.debug = function(args) {
      if (_minLevel == 'debug') {
        log('DEBUG', args);
      }
    }
    
    this.info = function(args) {
      if (_minLevel != 'warn') {
        log('INFO', args);
      }
    }
    
    this.warn = function(args) {
      log('WARN', args);
    }
    
  }
  
  //-------------------------------------------------------------------------
  
  function TabManager() {
    
    var self = this;
    
    var _componentsByTabId = {};
    var _activeComponent;
    
    this.registerTab = function(id, component) {
      _componentsByTabId[id] = component;
    }
    
    this.getTabs = function() {
      return _componentsByTabId;
    }
    
    this.getActiveComponent = function() {
      return _activeComponent;
    }
    
    this.onSelectTab = function(event, ui) {
      // Only just selected a new tab, so the old one is still the visible one
      var oldTab = $(ui.panel).parent().children(".ui-tabs-panel:not(.ui-tabs-hide)");
      var component = _componentsByTabId[oldTab.attr('id')];
      if (component && component.saveState) {
        component.saveState();
      }
    }
    
    this.onShowTab = function(event, ui) {
      var tab = ui.panel;
      var component = _componentsByTabId[tab.id];
      if (component && component.restoreState) { 
        component.restoreState();
      }
      _activeComponent = component;
    }
    
    this.onTabContainerResized = function() {
      $.each(_componentsByTabId, function(index, component) {
        if (component.onContainerResized) {
          component.onContainerResized();
        }
      });
    }
    
  }
  
  //-------------------------------------------------------------------------
  
  function Common() {
  }
  
  Common.sparklineDefaults = {
    lineColor: 'rgb(71, 113, 135)',
    spotColor: 'rgb(71, 113, 135)',
    minSpotColor: 'rgb(176, 54, 29)',
    maxSpotColor: 'rgb(29, 176, 54)',
  }
  
  Common.addExportCsvButton = function($container, url) {
    var $exportCsvButtonContainer = $("<div class='export-button'></div>").appendTo($container);
    $("<a href='" + url + "' target='_blank'>CSV</a>").appendTo($exportCsvButtonContainer).button();
  }
  
  $.extend(true, window, {
      Logger: Logger,
      EventManager: EventManager,
      TabManager: TabManager,
      Common: Common
  });
  
}(jQuery));