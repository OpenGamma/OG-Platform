/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.valuerequirementnames',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /valuerequirementnames
            root: 'valuerequirementnames',
            get: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta, meta_request = config.meta;
                meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['meta']}]});
                data = common.paginate(config);
                if (meta_request) method.push('metaData');
                return api.request(method, {data: data, meta: meta});
            },
            put: common.not_implemented_put,
            del: common.not_implemented_del
        };
    }
});