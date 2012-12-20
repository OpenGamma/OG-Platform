/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Renders a detailed time-series chart
 */
(function($) {

  /** @constructor */
  function TimeSeriesDetail() {

    var self = this;

    var _isInit = false;

    function launchTsGadget(data) {
      var iframe, key = (new Date).getTime() + (function (len, str) {
          while (len) len--, str += String.fromCharCode(65 + Math.floor(Math.random() * 26));
          return str;
      })(5, '');
      iframe = '<iframe src="/jax/bundles/fm/prototype/gadget.ftl#/timeseries/key=' + key + '"\
          frameborder="0" scrolling="no" title="Time-Series"></iframe>';
      og.api.common.cache_set(key, data);
      $(iframe).appendTo('body').dialog({
          autoOpen: true, height: 375, width: 875, modal: false,
          resizable: false, beforeClose: function () { $(this).remove(); self.beforeClosed.fire(); }
      }).css({height: '355px', width: '850px'});
    }

    //-----------------------------------------------------------------------
    // Public API

    this.updateValue = function(update) {
      if (_isInit || !update || !update.ts) {
        return;
      }

      _isInit = true
      launchTsGadget([update.ts])
    }

    this.beforeClosed = new EventManager();

    this.destroy = function() {
      self.beforeClosed = null;
    }

  }

  $.extend(true, window, {
    TimeSeriesDetail : TimeSeriesDetail
  });

}(jQuery));
