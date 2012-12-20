$.register_module({
    name: 'og.analytics.Status',
    dependencies: [],
    obj: function () {
        return function (selector) {
            var status = this, interval, init = false;
            $(selector + ' button').on('click', function () {
                if (!status.status || status.status === 'paused') return status.play();
                if (status.status === 'playing') return status.pause();
            });
            status.pause = function () {
                $(selector + ' em').html('paused').removeClass('live').addClass('paused');
                $(selector + ' button').removeClass('og-icon-play').addClass('og-icon-pause');
                status.message('');
                clearInterval(interval);
                status.status = 'paused';
            };
            status.play = function () {
                if (!init) init = !!$(selector + ' button').removeClass('og-disabled');
                $(selector + ' em').html('live').removeClass('paused').addClass('live');
                $(selector + ' button').removeClass('og-icon-pause').addClass('og-icon-play');
                status.message('starting...');
                interval = setInterval(function () {
                    status.message('updated ' + (Math.random() + 1).toFixed(2) + ' seconds ago');
                }, 1000);
                status.status = 'playing';
            };
            status.message = function (message) {$(selector + ' .og-message').html(message);};
            status.status = null;
            return status;
        };
    }
})