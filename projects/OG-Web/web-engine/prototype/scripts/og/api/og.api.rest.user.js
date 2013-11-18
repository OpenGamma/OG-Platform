/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.user',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, check = common.check;
        return { // all requests that begin with /user
            root: 'user',
            get: common.not_implemented_get,
            put: common.not_implemented_put,
            del: common.not_implemented_del,
            logout: {
                root: 'user/logout',
                put: common.not_implemented_put,
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = root.split('/'), data = {}, meta;
                    meta = check({
                        bundle: {method: root + '#put', config: config}
                    });
                    meta.type = 'GET';
                    return api.request(method, {data: data, meta: meta});
                },
                del: common.not_implemented_del
            }
        };
    }
});