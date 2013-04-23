/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.updates',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /updates
            root: 'updates',
            get: function (config) {
                config = config || {};
                var root = this.root, data = {}, meta;
                meta = check({bundle: {method: root + '#get', config: config}});
                meta.is_update = true;
                return api.request(null, {url: ['', root, api.id].join('/'), data: data, meta: meta});
            },
            put: common.not_available_put,
            del: common.not_available_del
        };
    }
});