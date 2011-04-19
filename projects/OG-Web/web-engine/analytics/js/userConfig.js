(function($) {
  
  function UserConfig() {
    
    var self = this;
    
    var _sparklinesEnabled;
    
    function init() {
      _sparklinesEnabled = $.cookie('sparklines') == 'true';
    }
    
    //-----------------------------------------------------------------------
    // Public API
    
    this.getSparklinesEnabled = function() {
      return _sparklinesEnabled;
    }
    
    this.setSparklinesEnabled = function(enabled) {
      _sparklinesEnabled = enabled;
      $.cookie('sparklines', enabled);
      self.onSparklinesToggled.fire(enabled);
    }
    
    this.onSparklinesToggled = new EventManager();
    
    //-----------------------------------------------------------------------
    
    init();
  }
  
  $.extend(true, window, {
      UserConfig : UserConfig
  });

}(jQuery));