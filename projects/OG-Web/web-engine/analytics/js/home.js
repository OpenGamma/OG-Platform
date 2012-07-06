(function($) {
  
  var _logger = new Logger("Home", "debug");
  
  var _liveResultsClient;
  
  var _init = false;
  var _isRunning = false;
  
  var _userConfig;
  var _resultsViewer = null;
  
  var _versionDateTime = null;
  var _versionFromSnapshot = null;
  
  //-----------------------------------------------------------------------
  // Views
  
  $.widget( "ui.combobox", {
    _create: function() {
      var input,
        self = this,
        select = this.element,
        selected = select.children(":selected"),
        value = selected.val() ? selected.text() : "",
        wrapper = $("<span>")
          .addClass("ui-combobox")
          .insertAfter(select);
        select.hide();

      input = $("<input>")
        .appendTo(wrapper)
        .val(value)
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
                    ), "<strong>$1</strong>"),
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
              var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex($(this).val()) + "$", "i"),
                valid = false;
              select.children("option").each(function() {
                if ($(this).text().match(matcher)) {
                  this.selected = valid = true;
                  if (self.options.change) {
                    self.options.change(this);
                  }
                  return false;
                }
              });
              if ( !valid ) {
                // remove invalid value, as it didn't match anything
                $(this).val("");
                select.val("");
                input.data("autocomplete").term = "";
                return false;
              }
            }
          }
        })
        .addClass("ui-widget ui-widget-content ui-corner-left");

        input.data("autocomplete")._renderItem = function(ul, item) {
          return $("<li></li>")
            .data("item.autocomplete", item)
            .append("<a>" + item.label + "</a>")
            .appendTo(ul);
        };
      
        input.data("autocomplete")._renderItem = function(ul, item) {
          return $("<li></li>")
            .data("item.autocomplete", item )
            .append("<a>" + item.label + "</a>")
            .attr("class", $(item.option).attr("class"))
            .appendTo(ul);
        };

        $("<a>")
        .attr("tabIndex", -1)
        .attr("title", "Show All Items")
        .appendTo(wrapper)
        .button({
          icons: {
            primary: "ui-icon-triangle-1-s"
          },
          text: false
        })
        .removeClass("ui-corner-all")
        .addClass("ui-corner-right ui-button-icon")
        .click(function() {
          // close if already visible
          if (input.autocomplete("widget").is(":visible")) {
            input.autocomplete("close");
            return;
          }

          // work around a bug (likely same cause as #5265)
          $(this).blur();

          // pass empty string as value to search for, displaying all results
          input.autocomplete( "search", "" );
          input.focus();
        });
    },

    destroy: function() {
      this.element.show();
      $.Widget.prototype.destroy.call(this);
    }
  });
  
  function onInitDataReceived(initData) {
    if (_init) {
      updateViewDefinitions($("#viewlist"), initData.viewDefinitions);
    } else {
      initControls(initData);
    }
  }
  
  function initControls(initData) {
    $('#viewcontrols').show();

    var $views = $('#views');
    var $backingViewList = $("<select id='viewlist'></select>").appendTo($views);
    var $backingAggregatorsList = $("<select id='aggregatorslist'></select>");
    var $backingSnapshotList = $("<select id='snapshotlist'></select>");
    // check the URL of the iframe for an ID to preload
    updateViewDefinitions($backingViewList, initData.viewDefinitions, window.location.search.substring(1));
    $backingViewList.combobox({
      change: function(item) {
        populateSnapshots($backingSnapshotList, initData.specifications, initData.snapshots, $(item).val());
        window.parent.location.hash = '/' + $(item).val(); // set the parent window's hash to have ID for preloading
      }
    });
    getInput($backingViewList).width(200);
    
    $("<span class='viewlabel'>aggregated by</span>").appendTo($views);
    $backingAggregatorsList.appendTo($views);
    populateAggregators($backingAggregatorsList, initData.aggregatorNames);
    $backingAggregatorsList.combobox();
    getInput($backingAggregatorsList).width(130);

    $("<span class='viewlabel'>using</span>").appendTo($views);
    $backingSnapshotList.appendTo($views);
    $backingSnapshotList.combobox({
      change: function() {
        if (_versionFromSnapshot) {
          _versionFromSnapshot = null;
          $('#datenow').attr('checked', 'checked');
          $('#datesource').buttonset('refresh');
        }
      }
    });
    getInput($backingSnapshotList).width(220);
    populateSnapshots($backingSnapshotList, initData.specifications, initData.snapshots, null);
    
    $("<span class='viewlabel'>at time</span>").appendTo($views);
    
    $('#datesource').buttonset();
    $('#dateInput').datepicker({
      dateFormat: "yy-mm-dd"
    });
    $('#dateSetButton')
      .button( { label: 'OK' })
      .click(function() {
        var d = $('#dateInput').datepicker('getDate');
        var timeStr = $('#timeInput').val();
        if (!timeStr) {
          showError("A time must be specified");
          return;
        }
        var timeParts = timeStr.split(':');
        if (timeParts.length != 3) {
          showError("Invalid time format. Expected 'hh:mm:ss'");
          return;
        }
        var hours = parseInt(timeParts[0]);
        var minutes = parseInt(timeParts[1]);
        var seconds = parseInt(timeParts[2]);
        if (isNaN(hours) || isNaN(minutes) || isNaN(seconds)) {
          showError("Unable to interpret time '" + timeStr + "'");
          return;
        }
        d.setHours(hours);
        d.setMinutes(minutes);
        d.setSeconds(seconds);
        _versionDateTime = d;
        _versionFromSnapshot = null;
        $('#snapshotversionscontrols').empty();
        $('#versionDateTime').dialog('close');
      });
    var dialogCancel = function() {
      if (!_versionDateTime && !_versionFromSnapshot) {
        $('#datenow').attr('checked', 'checked');
        $('#datesource').buttonset('refresh');
      }
    }
    $('#versionDateTime').dialog({
      autoOpen: false,
      buttons: {
        Cancel : function() {
          dialogCancel();
          $(this).dialog("close");
        }
      },
      modal: true,
      focusSelector: null,
      resizable: false,
      width: 340,
      close: dialogCancel
    });
    $('#datenow').click(function() {
      _versionDateTime = null;
      _versionFromSnapshot = null;
    });
    $('#datecustom').click(function() { showVersionDateTimeDialog(); });
    
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
    getInput($backingViewList).focus();
    
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
  
  function showVersionDateTimeDialog() {
    var $versionControls = $('#snapshotversionscontrols');
    var $versionMsg = $('<p>');
    $versionControls.empty().append($versionMsg);
    
    var $selectedMarketData = $('#snapshotlist option:selected');
    var marketDataId = $selectedMarketData.attr('value');
    if (marketDataId && !$selectedMarketData.hasClass('standard-entry')) {
      _liveResultsClient.getSnapshotVersions(marketDataId);
      $versionMsg.text('Loading snapshot versions...');
    } else {
      $versionMsg.text('No snapshot selected');
    }
    
    var dialogDate = _versionDateTime == null ? new Date() : new Date(_versionDateTime);
    setDialogCustomDateTime(dialogDate);
    $('#versionDateTime').dialog('open');
  }
  
  function parseDateTimeString(dtStr) {
    var dtParts = dtStr.split(' ');
    var dParts = dtParts[0].split('-');
    var tParts = dtParts[1].split(':');
    return new Date(dParts[0], dParts[1] - 1, dParts[2], tParts[0], tParts[1], tParts[2], 0)
  }
  
  function setDialogCustomDateTime(d) {
    $('#timeInput').val(formatTime(d));
    $('#dateInput').datepicker('setDate', d);
  }
  
  function formatTime(d) {
    var hours = d.getHours() < 10 ? "0" + d.getHours(): d.getHours();
    var minutes = d.getMinutes() < 10 ? "0" + d.getMinutes() : d.getMinutes();
    var seconds = d.getSeconds() < 10 ? "0" + d.getSeconds() : d.getSeconds();
    return hours + ":" + minutes + ":" + seconds;
  }
  
  function updateViewDefinitions($backingList, contents, selection) {
    var existingSelection = selection || $backingList.val(); // if a preloaded value has been given, favor that
    $backingList.empty();
    $('<option value=""></option>').appendTo($backingList);
    $.each(contents, function(idx, viewDef) {
      var $opt = $('<option value="' + viewDef.id + '">' + viewDef.name + '</option>');
      $opt.appendTo($backingList);
    });
    $backingList.val(existingSelection);
    if (selection) setTimeout(initializeView, 200); // if a preloaded value was given, launch the viewer immediately
  }
  
  function populateAggregators($aggregatorsSelect, aggregators) {
    $aggregatorsSelect.empty();
    $('<option value=""></option>').appendTo($aggregatorsSelect);
    $('<option value="default">Default Aggregation</option>').addClass("standard-entry").addClass("autocomplete-divider")
        .appendTo($aggregatorsSelect);
    $.each(aggregators, function(idx, aggregator) {
      $('<option value="' + aggregator + '">' + aggregator + '</option>').appendTo($aggregatorsSelect);
    });
    getInput($aggregatorsSelect).val($aggregatorsSelect.children()[1].text);
    $aggregatorsSelect.children()[1].selected = true;
  }
  
  function getInput($backingList) {
    return $backingList.next().children(':first');
  }
  
  function populateSnapshots($snapshotSelect, specifications, snapshots, selectedView) {
    var $input = getInput($snapshotSelect)
    var previousVal = $input.val();
    var selectedViewSnapshots = snapshots[selectedView];
    
    $snapshotSelect.empty();
    $('<option value=""></option>').appendTo($snapshotSelect);
    
    if (specifications) {
      var $marketDataSpec;
      $.each(specifications, function(idx, specificationName) {
        $marketDataSpec = $('<option value="' + specificationName + '">' + specificationName + '</option>')
          .addClass("standard-entry");
        $marketDataSpec.appendTo($snapshotSelect);
      });
      $marketDataSpec.appendTo($snapshotSelect);
    }

    if (selectedView) {
      if (selectedViewSnapshots) {
        $.each(selectedViewSnapshots, function(snapshotId, snapshotName) {
          $('<option value="' + snapshotId + '">' + snapshotName + '</option>').appendTo($snapshotSelect);
        });
      }
      
      if ($snapshotSelect.children().size() > 2 && $marketDataSpec) {
        $marketDataSpec.addClass("autocomplete-divider");
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
  
    $input.val(previousVal);
    if (!$input.val() && $snapshotSelect.children().size() > 1) {
      $input.val($snapshotSelect.children()[1].text);
    }
    $snapshotSelect.val($input.val());
  }
  
  function initializeView() {
    if (!_liveResultsClient) {
      return;
    }
    
    var viewId = $('#viewlist option:selected').attr('value');
    var viewText = $('#viewlist option:selected').attr('text');
    var viewInputText = getInput($('#viewlist')).val();
    
    if (!viewId || viewText != viewInputText) {
      // Assume the user has manually entered an ID
      viewId = viewInputText;
    }
    
    if (!viewId || viewId == "") {
      showViewError("A view must be chosen from the list before it can be loaded.");
      return;
    }
    
    var $selectedAggregator = $('#aggregatorslist option:selected')
    var aggregatorName = $selectedAggregator.attr('value');
    if (!aggregatorName || aggregatorName == "default") {
      aggregatorName = null;
    }
    
    var $selectedMarketData = $('#snapshotlist option:selected');
    var marketDataId = $selectedMarketData.attr('value');
    if (!marketDataId) {
      showViewError("Invalid market data source");
      return;
    }
    var isLive = $selectedMarketData.hasClass('standard-entry');
    var marketDataSpecification = {};
    if (isLive) {
      marketDataSpecification.marketDataType = "live";
      marketDataSpecification.provider = marketDataId;
    } else {
      marketDataSpecification.marketDataType = "snapshot";
      if (_versionFromSnapshot) {
        marketDataSpecification.snapshotId = _versionFromSnapshot;
      } else {
        marketDataSpecification.snapshotId = marketDataId;
      }
    }
    
    prepareChangeView();
    _liveResultsClient.changeView(viewId, aggregatorName, marketDataSpecification, _versionDateTime);
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
  
  function onViewChanged(gridStructures) {
    if (_isRunning) {
      // Unsolicited re-initialization
      prepareChangeView();
    }
    _resultsViewer = new TabbedViewResultsViewer($('#resultsViewer'), gridStructures, _liveResultsClient, _userConfig);
    
    // Ask the client to start
    document.body.style.cursor = "default";
    _liveResultsClient.resume();
  }
  
  function onViewChangeFailed(errorMessage) {
    if (_isRunning) {
      // Unsolicited re-initialization
      prepareChangeView();
    }
    $('#resultsViewer').empty();
    document.body.style.cursor = "default";
    showViewError(errorMessage);
  }
  
  function showViewError(errorMessage) {
    $("<div title='Error'><div style='margin-top:10px'><span class='ui-icon ui-icon-alert' style='float:left; margin:0 7px 50px 0;'></span>" + errorMessage + "</div></div>").dialog({
      modal: true,
      buttons: {
        Ok: function() {
          $(this).dialog("close");
          getInput($('#viewlist')).focus();
        }
      },
      resizable: false
    });    
  }
  
  function showError(errorMessage) {
    $("<div title='Error'><div style='margin-top:10px'><span class='ui-icon ui-icon-alert' style='float:left; margin:0 7px 50px 0;'></span>" + errorMessage + "</div></div>").dialog({
      modal: true,
      buttons: {
        Ok: function() {
          $(this).dialog("close");
        }
      },
      resizable: false
    });
  }
  
  function onSnapshotVersionsReceived(message) {
    var $versionControls = $('#snapshotversionscontrols');
    $versionControls.empty();
    
    var $versionsSelect = $('<select>').appendTo($versionControls);
    var $versionsSetButton = $('<span style="margin-left:0.3em">').appendTo($versionControls);
    $versionsSetButton
      .button({ label: 'OK' })
      .click(function() {
        var versionedSnapshotId = $versionsSelect.val();
        if (versionedSnapshotId) {
          _versionFromSnapshot = versionedSnapshotId;
          _versionDateTime = null;
        }
        $('#versionDateTime').dialog('close');
      });
    
    var $snapshotVersion = $("#snapshotversion");
    $snapshotVersion.empty();
    $.each(message.versions, function(idx, version) {
      $versionsSelect.append($("<option />").val(version[0]).text(version[1]));
    });
    $versionsSelect.combobox();
    getInput($versionsSelect).val($versionsSelect.children()[0].text);
    $versionsSelect.children()[0].selected = true;
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
    var positionFrame;
    $('.og-js-positiongadget').live('click', function() {
      var id = $(this).attr('data-posid').split('~').map(function (v) {return v.split('-')[1];}).join('~');
      var iframe = '<iframe src="/jax/bundles/fm/prototype/gadget.ftl#/positions/' + id + '/trades=true"\
          frameborder="0" scrolling="no" title="Position"></iframe>';
      if (positionFrame) {
        positionFrame.dialog('close');
        positionFrame = null;
      }
      positionFrame = $(iframe).appendTo('body').dialog({
          autoOpen: true, height: 345, width: 875, modal: false, resizable: false,
          beforeClose: function () { $(this).remove(); positionFrame = null; }
      }).css({height: '400px', width: '850px'});
    });
    
    _userConfig = new UserConfig();
    toggleSparklines(_userConfig.getSparklinesEnabled());
    disablePauseResumeButtons();
    
    _liveResultsClient = new LiveResultsClient();
    _liveResultsClient.onConnected.subscribe(onConnected);
    _liveResultsClient.onDisconnected.subscribe(onDisconnected);
    _liveResultsClient.onInitDataReceived.subscribe(onInitDataReceived);
    _liveResultsClient.onSnapshotVersionsReceived.subscribe(onSnapshotVersionsReceived);
    _liveResultsClient.onViewChanged.subscribe(onViewChanged);
    _liveResultsClient.onViewChangeFailed.subscribe(onViewChangeFailed);
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
