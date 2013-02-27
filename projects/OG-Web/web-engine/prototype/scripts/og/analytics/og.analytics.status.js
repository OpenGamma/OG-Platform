$.register_module({
    name: 'og.analytics.Status',
    dependencies: [],
    obj: function () {
        return function (selector) {
            var status = this, init = false;
            $(selector + ' button').on('click', function (event) {
                event.preventDefault();
                if (!status.status || status.status === 'paused') return status.play();
                if (status.status === 'playing') return status.pause();
            });
            status.pause = function () {
                $(selector + ' em').html('paused').removeClass('live').addClass('paused');
                $(selector + ' button').removeClass('og-icon-play').addClass('og-icon-pause');
                status.status = 'paused';;
            };
            status.play = function () {
                if (!init) init = !!$(selector + ' button').removeClass('og-disabled');
                $(selector + ' em').html('live').removeClass('paused').addClass('live');
                $(selector + ' button').removeClass('og-icon-pause').addClass('og-icon-play');
                status.status = 'playing';
            };
            status.status = null;
            status.play();
        };
    }
})