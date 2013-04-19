/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.positions',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /positions
            root: 'positions',
            get: api.default_get.partial(['min_quantity', 'max_quantity', 'identifier'],
                    ['minquantity', 'maxquantity', 'identifier']),
            put: function (config) {
                config = config || {};
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id), version = str(config.version),
                    fields = ['identifier', 'quantity', 'scheme_type', 'trades'],
                    api_fields = ['idvalue', 'quantity', 'idscheme', 'tradesJson'];
                meta = check({
                    bundle: {method: root + '#put', config: config},
                    dependencies: [{fields: ['version'], require: 'id'}],
                    required: [
                        {condition: !id, all_of:  ['identifier', 'quantity', 'scheme_type']},
                        {condition: !!id, all_of: ['quantity']}
                    ]
                });
                meta.type = id ? 'PUT' : 'POST';
                fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                if (config['trades']) // the trades data structure needs to be serialized and sent as a string
                    data['tradesJson'] = JSON.stringify({trades: config['trades']});
                if (id) method = method.concat(version ? [id, 'versions', version] : id);
                return api.request(method, {data: data, meta: meta});
            },
            del: api.default_del
        };
    }
});