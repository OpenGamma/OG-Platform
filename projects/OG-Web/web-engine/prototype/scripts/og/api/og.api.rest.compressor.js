/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.compressor',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /compressor
            root: 'compressor',
            get: function (config) {
                var root = this.root, method = [root, 'decompress'], data = {}, meta;
                meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['content']}]});
                meta.type = 'POST';
                data.content = config.content.replace(/\-/g, '=').replace(/\_/g, '\/').replace(/\./g, '+');
                return api.request(method, {data: data, meta: meta});
            },
            put: function (config) {
                var root = this.root, method = [root, 'compress'], data = {}, meta;
                meta = check({bundle: {method: root + '#put', config: config}, required: [{all_of: ['content']}]});
                meta.type = 'POST';
                data.content = JSON.stringify(config.content);
                return api.request(method, {data: data, meta: meta});
            },
            del: common.not_available_del
        };
    }
});