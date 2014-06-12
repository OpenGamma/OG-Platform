/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.views',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check, STALL = common.STALL;
        return { // all requests that begin with /views
            root: 'views',
            get: common.not_available_get,
            put: function (config, promise) {
                config = config || {};
                var promise = promise || new common.Promise, root = this.root, method = [root], data = {}, meta,
                    fields = [
                        'viewdefinition', 'aggregators', 'providers', 'valuation', 'version', 'correction', 'blotter'
                    ],
                    api_fields = [
                        'viewDefinitionId', 'aggregators', 'marketDataProviders', 'valuationTime',
                        'portfolioVersionTime', 'portfolioCorrectionTime', 'blotter'
                    ];
                if (!api.id) return setTimeout((function (context) {                    // if handshake isn't
                    return function () {api.views.put.call(context, config, promise); }; // complete, return a
                })(this), STALL), promise;                                              // promise and try again
                meta = check({
                    bundle: {method: root + '#put', config: config},
                    required: [{all_of: ['viewdefinition', 'providers']}]
                });
                meta.type = 'POST';
                promise.ignore = true; // this request will be answered in fire_updates NOT in ajax handler
                fields.forEach(function (val, idx) {
                    var is_object = typeof config[val] === 'object';
                    if (is_object ? val = JSON.stringify(config[val]) : val = str(config[val])) // is truthy
                        data[api_fields[idx]] = val;
                });
                data['requestId'] = promise.id;
                data['aggregators'] = config.aggregators; // send traditional form array and not JSON
                data['clientId'] = api.id;
                return api.request(method, {data: data, meta: meta}, promise);
            },
            del: function (config) {
                config = config || {};
                var root = this.root, method = [root], meta, view_id = config.view_id;
                meta = check({
                    bundle: {method: root + '#del', config: config},
                    required: [{all_of: ['view_id']}]
                });
                meta.type = 'DELETE';
                method = method.concat(view_id);
                return api.request(method, {data: {}, meta: meta});
            },
            error : {
                root: 'views/{{view_id}}/errors',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = root.split('/'), data = {}, meta;
                    meta = check({
                    bundle: {method: root + '#get', config: config},
                    required: [{all_of: ['view_id']}]
                    });
                    method[1] = config.view_id;
                    return api.request(method, {data: data, meta: meta});
                },
                put: common.not_available_put,
                del: common.not_available_del
            },
            status: {
                root: 'views/{{view_id}}/pauseOrResume',
                pause_or_resume: function (config) {
                    config = config || {};
                    var root = this.root, method = root.split('/'), data = {state: config.state}, meta;
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        required: [{all_of: ['state']}]
                    });
                    method[1] = config.view_id;
                    meta.type = 'PUT';
                    return api.request(method, {data: data, meta: meta});
                }
            },
            csv: function (config) {
                    window.open('/jax/views/' + config.view_id + '/' + config.grid_type + '/data', '_blank');
            },
            grid: {
                depgraphs: {
                    root: 'views/{{view_id}}/{{grid_type}}/depgraphs',
                    del: function (config) {
                        config = config || {};
                        var root = this.root, method = root.split('/'), data = {}, meta;
                        meta = check({
                            bundle: {method: root + '#del', config: config},
                            required: [{all_of: ['view_id', 'grid_type', 'graph_id']}]
                        });
                        meta.type = 'DELETE';
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        method.push(config.graph_id);
                        return api.request(method, {data: {}, meta: meta});
                    },
                    get: common.not_available_get,
                    structure: {
                        root: 'views/{{view_id}}/{{grid_type}}/depgraphs/{{graph_id}}',
                        get: function (config) {
                            config = config || {};
                            var root = this.root, method = root.split('/'), data = {}, meta;
                            meta = check({
                                bundle: {method: root + '#get', config: config},
                                required: [{all_of: ['view_id', 'grid_type', 'graph_id']}]
                            });
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.graph_id;
                            return api.request(method, {data: data, meta: meta});
                        },
                        put: common.not_available_put,
                        del: common.not_available_del
                    },
                    put: function (config) {
                        config = config || {};
                        var promise = new common.Promise, root = this.root, method = root.split('/'),
                            data = {}, meta, fields = ['view_id', 'grid_type', 'colset', 'req'],
                        meta = check({
                            bundle: {method: root + '#put', config: config}, required: [{all_of: fields}]
                        });
                        meta.type = 'POST';
                        promise.ignore = true; // this request will be answered in fire_updates NOT in ajax handler
                        data['requestId'] = promise.id;
                        data['clientId'] = api.id;
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        fields.forEach(function (val, idx) {
                            if (val = str(config[val])) {
                                data[fields[idx]] = val;
                            }
                        });
                        return api.request(method, {data: data, meta: meta}, promise);
                    },
                    viewports: {
                        structure: {
                            root: 'views/{{view_id}}/{{grid_type}}/depgraphs/{{graph_id}}/viewports/{{viewport_id}}/structure',
                            get: function (config) {
                                config = config || {};
                                var root = this.root, method = root.split('/'), data = {}, meta;
                                meta = check({
                                    bundle: {method: root + '#get', config: config},
                                    required: [{all_of: ['view_id', 'grid_type', 'viewport_id', 'graph_id']}]
                                });
                                method[1] = config.view_id;
                                method[2] = config.grid_type;
                                method[4] = config.graph_id;
                                method[6] = config.viewport_id;
                                return api.request(method, {data: data, meta: meta});
                            },
                            put: common.not_available_put,
                            del: common.not_available_del
                        },
                        root: 'views/{{view_id}}/{{grid_type}}/depgraphs/{{graph_id}}/viewports',
                        get: function (config) {
                            config = config || {};
                            var root = this.root, method = root.split('/'), data = {}, meta;
                            meta = check({
                                bundle: {method: root + '#get', config: config},
                                required: [{all_of: ['view_id', 'grid_type', 'graph_id', 'viewport_id']}]
                            });
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.graph_id;
                            method.push(config.viewport_id);
                            return api.request(method, {data: data, meta: meta});
                        },
                        put: function (config) {
                            config = config || {};
                            var promise = new common.Promise, root = this.root, method = root.split('/'),
                                data = {}, meta, fields = ['cells', 'rows', 'cols', 'format', 'log'],
                                api_fields = ['cells', 'rows', 'columns', 'format', 'enableLogging'];
                            meta = check({
                                bundle: {method: root + '#put', config: config},
                                required: [
                                    {all_of: ['view_id', 'graph_id']},
                                    {either: ['rows', 'cols'], or: ['cells']}
                                ]
                            });
                            meta.type = 'POST';
                            fields.forEach(function (key, idx) {data[api_fields[idx]] = config[key];});
                            data['clientId'] = api.id;
                            data['enableLogging'] = !!data['enableLogging'];
                            data.version = promise.id;
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.graph_id;
                            if (config.viewport_id) // use the promise id as viewport_version
                                (meta.type = 'PUT'), method.push(config.viewport_id);
                            else // the response will come back in fire_updates
                                (promise.ignore = true), (data['requestId'] = promise.id);
                            return api.request(method, {data: data, meta: meta}, promise);
                        },
                        del: function (config) {
                            config = config || {};
                            var root = this.root, method = root.split('/'), data = {}, meta;
                            meta = check({
                                bundle: {method: root + '#del', config: config},
                                required: [{all_of: ['view_id', 'grid_type', 'graph_id', 'viewport_id']}]
                            });
                            meta.type = 'DELETE';
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.graph_id;
                            method.push(config.viewport_id);
                            return api.request(method, {data: {}, meta: meta});
                        }
                    }
                },
                structure: {
                    root: 'views/{{view_id}}/{{grid_type}}',
                    get: function (config) {
                        config = config || {};
                        var root = this.root, method = root.split('/'), data = {}, meta;
                        meta = check({
                            bundle: {method: root + '#get', config: config},
                            required: [{all_of: ['view_id', 'grid_type']}]
                        });
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        return api.request(method, {data: data, meta: meta});
                    },
                    put: common.not_available_put,
                    del: common.not_available_del
                },
                viewports: {
                    structure: {
                        root: 'views/{{view_id}}/{{grid_type}}/viewports/{{viewport_id}}/structure',
                        get: function (config) {
                            config = config || {};
                            var root = this.root, method = root.split('/'), data = {}, meta;
                            meta = check({
                                 bundle: {method: root + '#get', config: config},
                                 required: [{all_of: ['view_id', 'grid_type', 'viewport_id']}]
                            });
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.viewport_id;
                            return api.request(method, {data: data, meta: meta});
                        },
                        put: common.not_available_put,
                        del: common.not_available_del
                    },
                    valuereq: {
                        root: 'views/{{view_id}}/{{grid_type}}/viewports/{{viewport_id}}/valuereq/{{row}}/{{col}}',
                        get: function (config) {
                            config = config || {};
                            var root = this.root, method = root.split('/'), data = {}, meta;
                            meta = check({
                                bundle: {method: root + '#get', config: config},
                                required: [{all_of: ['view_id', 'grid_type', 'viewport_id', 'row', 'col']}]
                            });
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            method[4] = config.viewport_id;
                            method[6] = config.row;
                            method[7] = config.col;
                            return api.request(method, {data: data, meta: meta});
                        },
                        put: common.not_available_put,
                        del: common.not_available_del
                    },
                    root: 'views/{{view_id}}/{{grid_type}}/viewports',
                    get: function (config) {
                        config = config || {};
                        var root = this.root, method = root.split('/'), data = {}, meta;
                        meta = check({
                            bundle: {method: root + '#get', config: config},
                            required: [{all_of: ['view_id', 'grid_type', 'viewport_id']}]
                        });
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        method.push(config.viewport_id);
                        return api.request(method, {data: data, meta: meta});
                    },
                    put: function (config) {
                        config = config || {};
                        var promise = new common.Promise, root = this.root, method = root.split('/'),
                            data = {}, meta, fields = ['cells', 'rows', 'cols', 'format', 'log'],
                            api_fields = ['cells', 'rows', 'columns', 'format', 'enableLogging'];
                        meta = check({
                            bundle: {method: root + '#put', config: config},
                            required: [
                                {all_of: ['view_id']},
                                {either: ['rows', 'cols'], or: ['cells']}
                            ]
                        });
                        meta.type = 'POST';
                        fields.forEach(function (key, idx) {data[api_fields[idx]] = config[key];});
                        data['clientId'] = api.id;
                        data['enableLogging'] = !!data['enableLogging'];
                        data.version = promise.id;
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        if (config.viewport_id) {// use the promise id as viewport_version
                            meta.type = 'PUT';
                            method.push(config.viewport_id);
                        }
                        else {// the response will come back in fire_updates
                            promise.ignore = true;
                            data['requestId'] = promise.id;
                        }
                        return api.request(method, {data: data, meta: meta}, promise);
                    },
                    del: function (config) {
                        config = config || {};
                        var root = this.root, method = root.split('/'), data = {}, meta;
                        meta = check({
                            bundle: {method: root + '#del', config: config},
                            required: [{all_of: ['view_id', 'grid_type', 'viewport_id']}]
                        });
                        meta.type = 'DELETE';
                        method[1] = config.view_id;
                        method[2] = config.grid_type;
                        method.push(config.viewport_id);
                        return api.request(method, {data: {}, meta: meta});
                    }
                }
            }
        };
    }
});
