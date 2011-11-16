(function($) {
  
  var _logger = new Logger("Home", "debug");
  
  var _liveResultsClient;
  
  var _init = false;
  var _isRunning = false;
  
  var _userConfig;
  var _resultsViewer = null;
  
  //-----------------------------------------------------------------------
  // Views
  
  $.widget("ui.combobox", {
    _create: function() {
      var self = this,
        select = this.element,
        selectWidth = Math.min(250, select.width() + 15),
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
            select.val(ui.item.option.value);
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
      updateList($("#viewlist"), initData.viewNames);
    } else {
      initControls(initData);
    }
  }
  
  function initControls(initData) {
    $('#viewcontrols').show();
    
    viewList = initData.viewNames;
    var $views = $('#views');
    var $backingViewList = $("<select id='viewlist'></select>").appendTo($views);
    var $backingAggregatorsList = $("<select id='aggregatorslist'></select>");
    var $backingSnapshotList = $("<select id='snapshotlist'></select>");

    updateList($backingViewList, initData.viewNames);
    $backingViewList.combobox({
      change: function(item) {
        populateSnapshots($backingSnapshotList, initData.liveSources, initData.snapshots, $(item).val());
        sizeList($backingSnapshotList);
      }
    });
    
    $("<span class='viewlabel'>aggregated by</span>").appendTo($views);
    $backingAggregatorsList.appendTo($views);
    $backingAggregatorsList.combobox();
    populateAggregators($backingAggregatorsList, initData.aggregatorNames);

    $("<span class='viewlabel'>using</span>").appendTo($views);
    $backingSnapshotList.appendTo($views);
    $backingSnapshotList.combobox();
    populateSnapshots($backingSnapshotList, initData.liveSources, initData.snapshots, null);
    
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
    sizeList($backingAggregatorsList);
    sizeList($backingSnapshotList);
    $('#loadingviews').remove();
    _init = true;
  }
  
  function updateList($backingList, contents) {
    var existingSelection = $backingList.val();
    $backingList.empty();
    $('<option value=""></option>').appendTo($backingList);
    $.each(contents, function() {
      var $opt = $('<option value="' + this + '">' + this + '</option>');
      $opt.appendTo($backingList);
    });
    $backingList.val(existingSelection);
  }
  
  function populateAggregators($aggregatorsSelect, aggregators) {
    $aggregatorsSelect.empty();
    $('<option value=""></option>').appendTo($aggregatorsSelect);
    $('<option value="default">Default Aggregation</option>').addClass("standard-entry").addClass("autocomplete-divider")
        .appendTo($aggregatorsSelect);
    $.each(aggregators, function(idx, aggregator) {
      $('<option value="' + aggregator + '">' + aggregator + '</option>').appendTo($aggregatorsSelect);
    });
    $aggregatorsSelect.next().val($aggregatorsSelect.children()[1].text);
    $aggregatorsSelect.children()[1].selected = true;
  }
  
  function sizeList($backingList) {
    $backingList.next().width(Math.min(250, $backingList.width() + 15));
  }
  
  function populateSnapshots($snapshotSelect, liveSources, snapshots, selectedView) {
    var $input = $snapshotSelect.next();
    var currentVal = $input.val();
    var currentValExists = false;
    var selectedViewSnapshots = snapshots[selectedView];
    
    $snapshotSelect.empty();
    $('<option value=""></option>').appendTo($snapshotSelect);
    
    if (liveSources) {
      var $liveMarketData;
      $.each(liveSources, function(idx, liveSource) {
        $liveMarketData = $('<option value="' + liveSource + '">Live market data (' + liveSource + ')</option>')
          .addClass("standard-entry");
        $liveMarketData.appendTo($snapshotSelect);
      });
    }

    if (selectedView) {
      if (selectedViewSnapshots) {
        $.each(selectedViewSnapshots, function(snapshotId, snapshotName) {
          $('<option value="' + snapshotId + '">' + snapshotName + '</option>').appendTo($snapshotSelect);
          if (!currentValExists && snapshotName == currentVal) {
            currentValExists = true;
          }
        });
      }
      
      if ($snapshotSelect.children().size() > 2 && $liveMarketData) {
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
    
    if (!currentValExists && $snapshotSelect.children().size() > 1) {
      $input.val($snapshotSelect.children()[1].text);
      $snapshotSelect.children()[1].selected = true;
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
    
    var $selectedAggregator = $('#aggregatorslist option:selected')
    var aggregatorName = $selectedAggregator.attr('value');
    if (!aggregatorName || aggregatorName == "default") {
      aggregatorName = null;
    }
    
    var $selectedMarketData = $('#snapshotlist option:selected');
    var marketDataId = $selectedMarketData.attr('value');
    var isLive = $selectedMarketData.hasClass('standard-entry');
    var marketDataSpecification = {};
    if (isLive) {
      marketDataSpecification.marketDataType = "live";
      marketDataSpecification.provider = marketDataId;
    } else {
      marketDataSpecification.marketDataType = "snapshot";
      marketDataSpecification.snapshotId = marketDataId;
    }
    
    prepareChangeView();
    _liveResultsClient.changeView(view, aggregatorName, marketDataSpecification);
  }
  
  function prepareChangeView() {
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
    _isRunning = false;
    disablePauseResumeButtons();
  }
  
  function onViewInitialized(gridStructures) {
    if (_isRunning) {
      // Unsolicited re-initialization
      prepareChangeView();
    }
    _resultsViewer = new TabbedViewResultsViewer($('#resultsViewer'), gridStructures, _liveResultsClient, _userConfig);
    
    // Ask the client to start
    document.body.style.cursor = "default";
    _liveResultsClient.resume();
  }
  
  //-----------------------------------------------------------------------
  // Status
  
  function onStatusUpdateReceived(update) {
    var isRunning = update.isRunning;
    if (_isRunning == isRunning) {
      return;
    }
    _isRunning = isRunning;
    togglePauseResumeButtons(isRunning);
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
    
    // Disconnect when the page unloads (client needs to time out on the server if this call is not made) 
    $(window).unload(function() {
      _liveResultsClient.disconnect();
    });
    
    // Timeout fools Chrome into thinking the page has loaded to stop the loading indicator from spinning and to
    // prevent escape from stopping CometD. 
    setTimeout(function() { _liveResultsClient.connect(); }, 0);
  });
  
})(jQuery);
