(function($) {
  
  var _logger = new Logger("Home", "debug");
  
  var _liveResultsClient;
  
  var _init = false;
  var _isRunning = false;
  var _statusTitle = '';
  var _resultTitle = '';
  
  var _userConfig;
  var _resultsViewer = null;
  
  //-----------------------------------------------------------------------
  // Views
  
  $.widget("ui.combobox", {
    _create: function() {
      var self = this,
        select = this.element,
        selectWidth = select.width() + 15,
        selected = select.children(":selected"),
        value = selected.val() ? selected.text() : "";
      select.hide();
      var input = $("<input style='width:" + selectWidth + "px'>")
        .insertAfter(select)
        .autocomplete({
          delay: 0,
          minLength: 0,
          source: function(request, response) {
            var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
            response(select.children("option").map(function() {
              var text = $(this).text();
              if (this.value && (!request.term || matcher.test(text)))
                return {
                  label: text.replace(
                    new RegExp(
                      "(?![^&;]+;)(?!<[^<>]*)(" +
                      $.ui.autocomplete.escapeRegex(request.term) +
                      ")(?![^<>]*>)(?![^&;]+;)", "gi"
                    ), "<strong>$1</strong>" ),
                  value: text,
                  option: this
                };
            }) );
          },
          select: function(event, ui) {
            ui.item.option.selected = true;
            self._trigger("selected", event, {
              item: ui.item.option
            });
            if (self.options.change) {
              self.options.change(ui.item.option);
            }
          },
          change: function(event, ui) {
            if (!ui.item) {
              var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex($(this).val()) + "$", "i");
              var valid = false;
              select.children("option").each(function() {
                if (this.value.match(matcher)) {
                  this.selected = valid = true;
                  if (self.options.change) {
                    self.options.change(this);
                  }
                  return false;
                }
              });
              if (!valid) {
                // remove invalid value, as it didn't match anything
                $(this).val("");
                select.val("");
                return false;
              }
            } else {
              if (self.options.change) {
                self.options.change(ui.item.option);
              }
            }
          }
        })
        .addClass("ui-widget ui-widget-content ui-corner-left");
      
      input.data("autocomplete")._renderItem = function(ul, item) {
        return $("<li></li>")
          .data("item.autocomplete", item )
          .append("<a>" + item.label + "</a>")
          .attr("class", $(item.option).attr("class"))
          .appendTo(ul);
      };

      $("<button>&nbsp;</button>")
        .attr( "tabIndex", -1 )
        .attr( "title", "Show All Items" )
        .insertAfter(input)
        .button({
          icons: {
            primary: "ui-icon-triangle-1-s"
          },
          text: false
        })
        .removeClass("ui-corner-all")
        .addClass("ui-corner-right ui-button-icon")
        .addClass("ui-autocomplete-button")
        .click(function() {
          // close if already visible
          if (input.autocomplete("widget").is(":visible")) {
            input.autocomplete("close");
            return;
          }

          // pass empty string as value to search for, displaying all results
          input.autocomplete("search", "");
          input.focus();
        });
    }
  });
  
  function onInitDataReceived(initData) {
    if (_init) {
      return;
    }
    
    $('#viewcontrols').show();
    
    viewList = initData.viewNames;
    var $views = $('#views');
    var $backingViewList = $("<select id='viewlist'></select>").appendTo($views);
    var $backingSnapshotList = $("<select id='snapshotlist'></select>")
    $('<option value=""></option>').appendTo($backingViewList);
    $.each(viewList, function() {
      var $opt = $('<option value="' + this + '">' + this + '</option>');
      $opt.appendTo($backingViewList);
    });
    $backingViewList.combobox({
      change: function(item) { populateSnapshots($backingSnapshotList, initData.snapshots, $(item).val()); }
    });

    $("<span class='viewlabel'>using</span>").appendTo($views);
    $backingSnapshotList.appendTo($views);
    $backingSnapshotList.combobox();
    populateSnapshots($backingSnapshotList, initData.snapshots, null);
    
    $views.find('.ui-autocomplete-input')
      .css('z-index', 10)
      .css('position', 'relative')
      .keydown(function(e) {
        if (e.keyCode === 13) {
          this.blur();
          $('#changeView').focus();
          
          // The error dialog fails from the event handler
          setTimeout(function() { initializeView(); }, 0);
        }
      });
    $backingViewList.next().focus();
    
    $('#changeView').button({ label: 'Load View' })
    .click(function(event) {
        initializeView();
      });
    
    $('#sparklines').click(function(event) {
      var sparklinesEnabled = !_userConfig.getSparklinesEnabled();
      toggleSparklines(sparklinesEnabled);
      _userConfig.setSparklinesEnabled(sparklinesEnabled);
    });
    
    $('#viewcontrols').hide().show(500);
    $('#loadingviews').remove();
    _init = true;
  }
  
  function populateSnapshots($snapshotSelect, snapshots, selectedView) {
    var $input = $snapshotSelect.next();
    var currentVal = $input.val();
    var currentValExists = false;
    var selectedViewSnapshots = snapshots[selectedView];
    
    $snapshotSelect.empty();
    $('<option value=""></option>').appendTo($snapshotSelect);
    var $liveMarketData = $('<option value="live">Live market data</option>')
        .addClass("live-market-data");
    $liveMarketData.appendTo($snapshotSelect);

    if (selectedView) {
      if (selectedViewSnapshots) {
        $.each(selectedViewSnapshots, function(snapshotId, snapshotName) {
          $('<option value="' + snapshotId + '">' + snapshotName + '</option>').appendTo($snapshotSelect);
          if (!currentValExists && snapshotName == currentVal) {
            currentValExists = true;
          }
        });
      }
      
      if ($snapshotSelect.children().size() > 2) {
        $liveMarketData.addClass("autocomplete-divider");
      }

      var isFirst = true;
      $.each(snapshots, function(viewName, viewSnapshots) {
        if (viewName == selectedView) {
          return;
        }
        $.each(viewSnapshots, function(snapshotId, snapshotName) {
          if (isFirst) {
            $snapshotSelect.children().last().addClass("autocomplete-divider");
            isFirst = false;
          }
          $('<option value="' + snapshotId + '">' + snapshotName + '</option>').appendTo($snapshotSelect);
        });
      });
    }
        
    $input.width($snapshotSelect.width() + 15);
    if (!currentValExists) {
      $input.val("Live market data");
    }
  }
  
  function initializeView() {
    if (!_liveResultsClient) {
      return;
    }
    
    var view = $('#viewlist option:selected').attr('value');
    
    if (!view || view == "") {
      $("<div title='Error'><div style='margin-top:10px'><span class='ui-icon ui-icon-alert' style='float:left; margin:0 7px 50px 0;'></span>A view must be chosen from the list before it can be loaded.</div></div>").dialog({
        modal: true,
        buttons: {
          Ok: function() {
            $(this).dialog("close");
            $('#viewlist').next().focus();
          }
        },
        resizable: false
      });
      return;
    }
    
    var snapshotId = $('#snapshotlist option:selected').attr('value');
    if (!snapshotId || snapshotId == "" || snapshotId == "live") {
      snapshotId = null;
    }
    
    document.body.style.cursor = "wait";
    if (_resultsViewer) {
      _resultsViewer.destroy();
      _resultsViewer = null;
    }
    var $resultsViewerContainer = $('#resultsViewer');
    $resultsViewerContainer.empty();
    if ($.support.opacity) {  // It's just more effort than it's worth trying to get this working in IE
      var $loadingDots = $("<div id='loading'></div>").jdCrazyDots({
        speed : 60,
        size : 30,
        dotWidth : "12%",
        dotHeight : "24%",
        empty : false
      });
      $resultsViewerContainer.append($loadingDots);
    }
    
    setStatusTitle('Loading');
    setResultTitle('initializing ' + view);
    updateStatusText();
    _isRunning = false;
    disablePauseResumeButtons();
    
    _liveResultsClient.changeView(view, snapshotId);
  }
  
  function onViewInitialized(gridStructures) {
    _resultsViewer = new TabbedViewResultsViewer($('#resultsViewer'), gridStructures, _liveResultsClient, _userConfig);
    
    // Ask the client to start
    document.body.style.cursor = "default";
    _liveResultsClient.resume();
  }
  
  //-----------------------------------------------------------------------
  // Status
  
  function onStatusUpdateReceived(update) {
    setStatusTitle(update.status);
    updateStatusText();

    var isRunning = update.isRunning;
    if (_isRunning == isRunning) {
      return;
    }
    _isRunning = isRunning;
    togglePauseResumeButtons(isRunning);
  }
  
  function afterUpdateReceived(update) {
    var resultDate = new Date(update.timestamp);
    var resultTitle = "calculated " + resultDate.toUTCString() + " in " + update.latency + " ms";
    setResultTitle(resultTitle);
    updateStatusText();
  }
  
  function setStatusTitle(statusTitle) {
    _statusTitle = statusTitle;
  }
  
  function getStatusTitle() {
    return _statusTitle;
  }
  
  function setResultTitle(resultTitle) {
    _resultTitle = resultTitle;
  }
  
  function getResultTitle() {
    return _resultTitle;
  }
  
  function updateStatusText() {
    $('#viewstatus').html("<p><span class='statustitle'>" + getStatusTitle() + ": </span> " + getResultTitle() + "</p>");
  }
  
  //-----------------------------------------------------------------------
  // User interaction
  
  function setButtonState(id, newState) {
    var button = $('#' + id);
    button.removeClass();
    button.addClass("imgbutton " + id + " " + newState);
  }
  
  function toggleSparklines(sparklinesEnabled) {
    setButtonState('sparklines', sparklinesEnabled ? 'on' : 'off');
    PrimitiveFormatter.sparklinesEnabled = sparklinesEnabled;
  }
  
  function disablePauseResumeButtons() {
    setButtonState('pause', 'off');
    setButtonState('resume', 'off');
  }
  
  function togglePauseResumeButtons(isRunning) {
    setButtonState('pause', isRunning ? 'on' : 'off');
    setButtonState('resume', isRunning ? 'off' : 'on');
  }
  
  //-----------------------------------------------------------------------
  // Messaging connection
  
  function onConnected() {    
    _liveResultsClient.getInitData();
  }
  
  function onDisconnected() {
    // TODO: more work around broken connections and unsubscribing
    $('#body').empty().html('Disconnected from server');
  }
  
  //-------------------------------------------------------------------------
  
  $(document).ready(function() {    
    $('#pause').click(function(event) {
      if (_liveResultsClient) {
        _liveResultsClient.pause();
      }
    });
    
    $('#resume').click(function(event) {
      if (_liveResultsClient) {
        _liveResultsClient.resume();
      }
    });
   
    _userConfig = new UserConfig();
    toggleSparklines(_userConfig.getSparklinesEnabled());
    disablePauseResumeButtons();
    
    _liveResultsClient = new LiveResultsClient();
    _liveResultsClient.onConnected.subscribe(onConnected);
    _liveResultsClient.onDisconnected.subscribe(onDisconnected);
    _liveResultsClient.onInitDataReceived.subscribe(onInitDataReceived);
    _liveResultsClient.onViewInitialized.subscribe(onViewInitialized);
    _liveResultsClient.onStatusUpdateReceived.subscribe(onStatusUpdateReceived);
    _liveResultsClient.afterUpdateReceived.subscribe(afterUpdateReceived);
    
    // Disconnect when the page unloads (client needs to time out on the server if this call is not made) 
    $(window).unload(function() {
      _liveResultsClient.disconnect();
    });
    
    // Timeout fools Chrome into thinking the page has loaded to stop the loading indicator from spinning and to
    // prevent escape from stopping CometD. 
    setTimeout(function() { _liveResultsClient.connect(); }, 0);
  });
  
})(jQuery);
