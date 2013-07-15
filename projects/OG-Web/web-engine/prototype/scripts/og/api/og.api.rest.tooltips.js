/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.tooltips',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /tooltips
            root: 'tooltips',
            get: function (config) {
                config = config || {};
                var root = this.root, method = [root], data, meta, promise = new common.Promise;
                meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['id']}]});
                data = og.api.tooltips[config.id] || null;
                setTimeout(function () {
                    var result = {
                        error: !data,
                        message: !data ? config.id + ' is not available as a tooltip' : '',
                        data: data,
                        meta: {promise: promise.id}
                    };
                    config.handler(result);
                    promise.deferred.resolve(result);
                }, common.INSTANT);
                return promise;
            },
            put: common.not_available_put,
            del: common.not_available_del
        };
    }
});