/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.subscribe',
    dependencies: ['og.api.common', 'og.api.rest', 'og.common.routes'],
    obj: function () {
        var common = og.api.common;
        var api = og.api.rest;
        var subscribe;
        var RESUBSCRIBE = 10000;
        var warn = og.dev.warn;
        subscribe = api.handshake.get.partial({handler: function (result) {
            var listen, fire_updates;
            if (result.error) {
                warn(api.name + ': handshake failed\n', result.message);
                return setTimeout(subscribe, RESUBSCRIBE);
            }
            api.id = result.data['clientId'];
            fire_updates = function (reset, result) {
                var current = og.common.routes.current();
                if (reset && api.disconnected) {
                    api.disconnected = false;
                    api.fire('reconnect');
                }
                // throw out stale registrations
                api.registrations = api.registrations.filter(function (reg) { return !common.request_expired(reg, current); });
                // fire all updates if connection is reset
                if (reset) {
                    api.registrations = api.registrations.filter(
                        function (reg) {return reg.update($.extend({reset: true}, reg)) && false; }
                    );
                    return api.registrations;
                }
                result.data.updates.filter(function (update) {
                    var simple = typeof update === 'string', promise, request;
                    if (!simple && (promise = (request = api.outstanding_requests[update.id]) && request.promise)) {
                        promise.deferred.resolve({
                            error: false, data: null, meta: {id: update.message.split('/').pop()}, promise: promise.id
                        });
                        delete api.outstanding_requests[promise.id];
                    }
                    return simple;
                }).forEach(
                    function (update) {
                        var lcv, reg;
                        var len = api.registrations.length;
                        for (lcv = 0; lcv < len; lcv += 1) {
                            reg = api.registrations[lcv];
                            if (reg && reg.url === update) {
                                api.registrations[lcv] = null;
                                reg.update(reg);
                            }
                        }
                        api.registrations = api.registrations.filter(Boolean);
                    }
                );
            };
            fire_updates(true, null); // there are no registrations when subscribe() is called unless the connection's been reset
            listen = function () {
                api.updates.get({handler: function (result) {
                    if (result.error) {
                        if (!api.disconnected) {
                            api.disconnected = true;
                            api.fire('disconnect');
                            api.id = null;
                        }
                        warn(api.name + ': subscription failed\n', result.message);
                        return setTimeout(subscribe, RESUBSCRIBE);
                    }
                    if (!result.data || !result.data.updates.length) {
                        return setTimeout(listen, common.INSTANT);
                    }
                    fire_updates(false, result);
                    setTimeout(listen, common.INSTANT);
                }});
            };
            listen();
        }});
        return subscribe;
    }
});
