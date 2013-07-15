/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.timeseries',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /timeseries
            root: 'timeseries',
            get: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id),
                    fields = ['identifier', 'data_source', 'data_provider', 'data_field', 'observation_time'],
                    api_fields = ['identifier', 'dataSource', 'dataProvider', 'dataField', 'observationTime'],
                    search = !id || fields.some(function (val) {return val in config;});
                meta = check({
                    bundle: {method: root + '#get', config: config},
                    empties: [{condition: search, label: 'search request', fields: ['id']}]
                });
                if (search) {
                    data = common.paginate(config);
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                } else {
                    method.push(id);
                }
                return api.request(method, {data: data, meta: meta});
            },
            put: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta, id = str(config.id),
                    fields = ['data_provider', 'data_field', 'start', 'end', 'scheme_type', 'identifier'],
                    api_fields = ['dataProvider', 'dataField', 'start', 'end', 'idscheme', 'idvalue'];
                meta = check({
                    bundle: {method: root + '#put', config: config},
                    required: [
                        {condition: !id, all_of: ['data_field', 'scheme_type', 'identifier']},
                        {condition: !!id, all_of: ['id']}
                    ]
                });
                meta.type = id ? 'PUT' : 'POST';
                fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                if (id) method = method.concat(id); else data['dataProvider'] = data['dataProvider'] || 'DEFAULT';
                return api.request(method, {data: data, meta: meta});
            },
            del: api.default_del
        };
    }
});