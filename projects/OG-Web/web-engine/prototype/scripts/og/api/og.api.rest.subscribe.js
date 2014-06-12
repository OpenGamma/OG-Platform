/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.subscribe',
    dependencies: ['og.api.common', 'og.api.rest', 'og.common.routes'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check,
            subscribe, RESUBSCRIBE = 10000 /* 10s */, warn = og.dev.warn;
        return subscribe = api.handshake.get.partial({handler: function (result) {
            var listen, fire_updates;
            if (result.error) {
                warn(api.name + ': handshake failed\n', result.message);
                return setTimeout(subscribe, RESUBSCRIBE);
            }
            api.id = result.data['clientId'];
            (fire_updates = function (reset, result) {
                var current = og.common.routes.current();
                if (reset && api.disconnected) {
                    api.disconnected = false;
                    api.fire('reconnect');
                }
                api.registrations = api.registrations.filter(function (reg) { // throw out stale registrations
                    return !common.request_expired(reg, current);
                });
                if (reset) {
                    return api.registrations = api.registrations // fire all updates if connection is reset
                        .filter(function (reg) {return reg.update($.extend({reset: true}, reg)) && false; });
                }
                result.data.updates.filter(function (update) {
                    // simple updates are plain urls, non-simple are objects with an ID and url
                    var simple = typeof update === 'string', promise, request;
                    if (!simple && (promise = (request = api.outstanding_requests[update.id]) && request.promise)) {
                        promise.deferred.resolve({ error: false, data: null,
                                meta: {id: update.message.split('/').pop()}, promise: promise.id
                            });
                        delete api.outstanding_requests[promise.id];
                    } else if (simple) {
                        if (~update.indexOf('error')) {
                            og.common.error.fire(update);
                        }
                    }
                    return simple;
                }).forEach(function (update) {
                    var lcv, len = api.registrations.length, reg;
                    for (lcv = 0; lcv < len; lcv += 1) {
                        if ((reg = api.registrations[lcv]) && reg.url === update) {
                            api.registrations[lcv] = null;
                            reg.update(reg);
                        }
                    }
                    api.registrations = api.registrations.filter(Boolean);
                });
            })(true, null); // there are no registrations when subscribe() is called unless the connection's been reset
            (listen = function () {
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
            })();
        }});
    }
});
