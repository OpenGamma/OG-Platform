/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.handshake',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, check = common.check;
        return { // all requests that begin with /handshake
            root: 'handshake',
            get: function (config) {
                config = config || {};
                if (api.id) warn(api.name + ': handshake has already been called');
                var root = this.root, data = {}, meta;
                meta = check({bundle: {method: root + '#get', config: config}});
                return api.request(null, {url: '/handshake', data: data, meta: meta});
            },
            put: common.not_available_put,
            del: common.not_available_del
        };
    }
});