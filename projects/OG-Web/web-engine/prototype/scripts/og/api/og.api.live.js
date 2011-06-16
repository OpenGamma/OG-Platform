/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * API for live two-way communication
 */
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
                var timer = null;
                return function (urls) {
                    if (timer) clearTimeout(timer);
                    timer = setTimeout(function () {(timer = null), request(urls.join('\n'));}, 25);
                }
            })()
        };
    }
});