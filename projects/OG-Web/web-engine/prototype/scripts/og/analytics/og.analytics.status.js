$.register_module({
    name: 'og.analytics.status',
    dependencies: [],
    obj: function () {
        var module = this, status, initialize = false,
            message = '.og-js-analytics-status-message', calculation = '.og-js-analytics-status-calculation',
            message_classes = 'live disconnected paused', toggle_classes = 'og-icon-play og-icon-pause og-disabled',
            toggle = '.og-js-analytics-status-toggle', resuming = false;
        var init_click_handler = function () {
            initialize = true;
            $(message).removeClass('live disconnected paused').addClass('live').html('live');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-pause').off('click')
                .on('click', function (event) {
                    if ($(this).hasClass('og-icon-pause')) {
                        action('pause');
                    } else {
                        action('resume');
                    }
                    return false;
                });
        };
        var action = function (state) {
            if (!og.analytics.grid) {
                return;
            }//resume is called on form load (og.analytics.form)
            if (state === 'pause') {
                markup_pause();
            } else {
                markup_resume();
            }
            og.analytics.grid.dataman.pools().forEach(function (val) {
                og.api.rest.views.status.pause_or_resume({view_id: val, state: state});
            });
        };
        //pause css and markup
        var markup_pause = function () {
            $(message).removeClass(message_classes).addClass('paused').html('paused');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-play');
        };
        //resume css and markup
        var markup_resume = function () {
            $(message).removeClass(message_classes).addClass('paused').html('starting...');
            $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
            resuming = true;
        };
        status = {
            resume: function () {
                if ($(toggle).hasClass('og-icon-pause') || $(toggle).hasClass('og-disabled')) {
                    return;
                }
                action('resume');
                initialize = false;
            },
            //update on every cycle
            cycle: function (ms) {
                if (resuming) {
                    $(message).html('live').addClass('live');
                    resuming = false;
                }
                $(calculation)[ms ? 'html' : 'empty']('calculated in ' + ms + 'ms');
                if (!initialize) {
                    init_click_handler();
                }
            },
            // og.api.rest.on('disconnect'...
            disconnected: function () {
                setTimeout(function () {
                    $(message).removeClass(message_classes).addClass('disconnected').html('connection lost');
                    $(toggle).removeClass(toggle_classes).addClass('og-icon-play og-disabled');
                    $(calculation).empty();
                }, 500);
            },
            //initial state when form loaded
            nominal: function () {
                $(message).removeClass(message_classes).addClass('live').html('ready');
                $(calculation).empty();
                initialize = false;
                $(toggle).off('click').on('click', function () {return false; });

            },
            // og.api.rest.on('reconnect'...
            reconnected: function () {
                $(message).removeClass(message_classes).addClass('live').html('connected');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
                $(calculation).empty();
                initialize = false; //initialise everying on next cycle
            }
        };
        return status;
    }
});