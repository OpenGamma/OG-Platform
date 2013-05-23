$.register_module({
    name: 'og.analytics.status',
    dependencies: [],
    obj: function () {
        var module = this, status, initialize = false,
            message = '.og-js-analytics-status-message', calculation = '.og-js-analytics-status-calculation',
            message_classes = 'live disconnected paused', toggle_classes = 'og-icon-play og-icon-pause og-disabled',
            toggle = '.og-js-analytics-status-toggle';
        var init_click_handler = function () {
            initialize = true;
            status.unpause();
            $(toggle).removeClass(toggle_classes).addClass('og-icon-play og-disabled')
                .off('click').on('click', function (event) {
                    var rest_obj = {};
                    if ($(this).hasClass('og-icon-pause')) status.pause(), rest_obj.state = 'pause';
                    else status.unpause(), rest_obj.state = 'resume';
                    og.analytics.grid.dataman.pools().forEach(function (val) {
                        rest_obj.view_id = val;
                        og.api.rest.views.status.pause_or_resume(rest_obj).pipe(function (result) {
                            //console.log('result', result);
                        });
                    });
                    return false;
                });
        }
        return status = {
            cycle: function (ms) {
                $(message).removeClass('live disconnected paused').addClass('live').html('live');
                $(calculation)[ms ? 'html' : 'empty']('calculated in ' + ms + 'ms');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
                if (!initialize) init_click_handler();
            },
            disconnected: function () {
                setTimeout(function () {
                    $(message).removeClass(message_classes).addClass('disconnected').html('connection lost');
                    $(toggle).removeClass(toggle_classes).addClass('og-icon-play og-disabled');
                    $(calculation).empty();
                }, 500);
            },
            nominal: function () {
                $(message).removeClass(message_classes).addClass('live').html('ready');
                $(calculation).empty();
                $(toggle).off('click').on('click', function () {return false;});
            },
            reconnected: function () {
                $(message).removeClass(message_classes).addClass('live').html('connected');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
                $(calculation).empty();
            },
            pause: function () {
                $(message).removeClass(message_classes).addClass('paused').html('paused');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-play');
            },
            unpause: function () {
                $(message).removeClass(message_classes).addClass('paused').html('starting...');
                $(toggle).removeClass(toggle_classes).addClass('og-icon-pause');
            }
        };
    }
})