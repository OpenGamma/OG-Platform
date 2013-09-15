/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.blotter',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check,
            blotter = { // all requests that begin with /blotter
                root: 'blotter',
                get: common.not_available_get,
                put: common.not_available_put,
                del: common.not_available_del,
                trades: {root: 'blotter/trades'},
                securities: {root: 'blotter/securities'},
                positions: {root: 'blotter/positions'},
                nodes: {positions: {root: 'blotter/nodes'}}
            };
        [ // blotter/lookup/* endpoints
            'barrierdirections', 'barriertypes', 'businessdayconventions', 'daycountconventions',
            'exercisetypes', 'floatingratetypes', 'frequencies', 'idschemes', 'interpolationmethods', 'longshort', 'monitoringtype',
            'samplingfrequencies', 'regions', 'debtseniority', 'restructuringclause', 'stubtype'
        ].forEach(function (key) {
            blotter[key] = {
                root: 'blotter/lookup/' + key,
                get: api.simple_get,
                put: common.not_available_put,
                del: common.not_available_del
            };
        });
        blotter.securities.get = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta;
            meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['id']}]});
            return api.request(method.concat(config.id), {data: {}, meta: meta});
        };
        blotter.trades.get = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta;
            meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['id']}]});
            return api.request(method.concat(config.id), {data: {}, meta: meta});
        };
        blotter.trades.put = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta, data = {trade: {}}, id = config.id;
            meta = check({bundle: {method: root + '#put', config: config}, required: [{all_of: ['trade', 'nodeId']}]});
            data.trade = str({
                trade: config.trade, security: config.security, underlying: config.underlying, nodeId: config.nodeId
            });
            meta.type = id ? 'PUT' : 'POST';
            if (id) method.push(id);
            return api.request(method, {data: data, meta: meta});
        };
        blotter.positions.get = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta;
            meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['id']}]});
            return api.request(method.concat(config.id), {data: {}, meta: meta});
        };
        blotter.positions.put = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta, data = {trade: {}}, id = config.nodeId;
            meta = check({bundle: {method: root + '#put', config: config},
                        required: [{all_of: ['trade', 'nodeId']}]});
            data.trade = str({
                trade: config.trade, security: config.security, underlying: config.underlying, nodeId: config.nodeId
            });
            meta.type = id ? 'PUT' : 'POST';
            if (id) method.push(id);
            return api.request(method, {data: data, meta: meta});
        };
        blotter.positions.del = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta;
            meta = check({bundle: {method: root + '#del', config: config}, required: [{all_of: ['id']}]});
            meta.type = 'DELETE';
            return api.request(method.concat(config.id), {data: {}, meta: meta});
        };
        blotter.trades.del = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta, id = str(config.id);
            meta = check({
                bundle: {method: root + '#del', config: config},
                required: [{all_of: ['id']}]
            });
            meta.type = 'DELETE';
            method.push(id);
            return api.request(method, {data: {}, meta: meta});
        };
        blotter.nodes.positions.del = function (config) {
            config = config || {};
            var root = this.root, method = root.split('/'), meta, id = str(config.id), position = str(config.position);
            meta = check({
                bundle: {method: root + '#del', config: config},
                required: [{all_of: ['id']}]
            });
            meta.type = 'DELETE';
            method.push(id);
            if (position) method.push('positions', position);
            return api.request(method, {data: {}, meta: meta});
        };
        return blotter;
    }
});
