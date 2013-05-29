$.register_module({
    name: 'og.analytics.status',
    dependencies: [],
    obj: function () {
        var module = this, status, initialize = false,
            message = '.og-js-analytics-status-message', calculation = '.og-js-analytics-status-calculation',
            message_classes = 'live disconnected paused', toggle_classes = 'og-icon-play og-icon-pause og-disabled',
            toggle = '.og-js-analytics-status-toggle', resuming = false;
        var init_click_handler = function () {
            console.log('init');
            initialize = true;
            $(message).removeClass('live disconnected paused').addClass('live').html('live');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-pause').off('click')
                .on('click', function (event) {
                    console.log('click');
                    if ($(this).hasClass('og-icon-pause'))
                        action('pause'), pause();
                    else
                        action('resume'), resume();
                    return false;
                });
        };
        var action = function (state) {
            console.log(state);
            og.analytics.grid.dataman.pools().forEach(function (val) {
                og.api.rest.views.status.pause_or_resume({view_id: val, state: state}).pipe(function (result) {
                    //console.log('result', result);
                });
            });
        };
        //pause css and markup
        var pause = function () {
            console.log('in pause');
            $(message).removeClass(message_classes).addClass('paused').html('paused');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-play');
        };
        //resume css and markup
        var resume = function () {
            console.log('in resume');
            $(message).removeClass(message_classes).addClass('paused').html('starting...');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
            resuming = true;
        };
        return status = {
            //update on every cycle
            cycle: function (ms) {
                if(resuming) $(message).html('live').addClass('live'), resuming = false;
                $(calculation)[ms ? 'html' : 'empty']('calculated in ' + ms + 'ms');
                if (!initialize) init_click_handler();
            },
            // og.api.rest.on('disconnect'...
            disconnected: function () {
                setTimeout(function () {
                    $(message).removeClass(message_classes).addClass('disconnected').html('connection lost');
                    $(toggle).removeClass(toggle_classes).addClass('og-icon-play og-disabled');
                    $(calculation).empty();
                }, 500);
            },
            //initial state for no view loaded
            nominal: function () {
                $(message).removeClass(message_classes).addClass('live').html('ready');
                $(calculation).empty();
                $(toggle).off('click').on('click', function () {return false;});
            },
            // og.api.rest.on('reconnect'...
            reconnected: function () {
                $(message).removeClass(message_classes).addClass('live').html('connected');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
                $(calculation).empty();
                initialize = false; //initialise everying on next cycle
            }
        };
    }
})