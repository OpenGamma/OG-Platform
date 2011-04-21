// API for live two-way communication
$.register_module({
    name: 'og.api.live',
    dependencies: ['og.dev'],
    obj: function () {
        var module = this, live,
            last = '',
            request = function (urls) {
                if (urls === last) return;
                last = urls;
                og.dev.log('registering interest in these: ' + urls);
            };
        return live = {
            register: (function () {
                // since register sometimes gets called quickly in rapid succession, we wait a small amount of time
                // to make sure requests that will supersede each other very quickly don't all have to be sent out
                var registration_timer = null;
                return function (urls) {
                    if (registration_timer) clearTimeout(registration_timer);
                    registration_timer = setTimeout(function () {(registration_timer = null), request(urls);}, 25);
                }
            })()
        };
    }
});