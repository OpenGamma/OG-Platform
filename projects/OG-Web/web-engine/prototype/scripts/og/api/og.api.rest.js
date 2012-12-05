/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * provides wrappers for the REST API
 */
$.register_module({
    name: 'og.api.rest',
    dependencies: ['og.dev', 'og.api.common', 'og.common.events', 'og.common.routes'],
    obj: function () {
        jQuery.ajaxSettings.traditional = true; // instead of arr[]=1&arr[]=2 we want arr=1&arr=2
        var module = this, live_data_root = module.live_data_root, api, warn = og.dev.warn,
            common = og.api.common, routes = og.common.routes, loading_start = common.loading_start,
            loading_end = common.loading_end, encode = window['encodeURIComponent'],
            // convert all incoming params into strings (so for example, the value 0 ought to be truthy, not falsey)
            str = common.str,
            outstanding_requests = {}, registrations = [], subscribe, post_processors = {},
            meta_data = {configs: null, holidays: null, securities: null, viewrequirementnames: null},
            singular = {
                configs: 'config', exchanges: 'exchange', holidays: 'holiday',
                portfolios: 'portfolio', positions: 'position', regions: 'region', securities: 'security',
                timeseries: 'timeseries'
            },
            has_id_search = {
                configs: true, exchanges: true, holidays: true, portfolios: true,
                positions: true, regions: true, securities: true, timeseries: false
            },
            request_id = 1,
            MAX_INT = Math.pow(2, 31) - 1, PAGE_SIZE = 50, PAGE = 1, STALL = 500 /* 500ms */,
            INSTANT = 0 /* 0ms */, RESUBSCRIBE = 10000 /* 10s */,
            TIMEOUTSOON = 120000 /* 2m */, TIMEOUTFOREVER = 7200000 /* 2h */
        var cache_get = function (key) {return common.cache_get(module.name + key);};
        var cache_set = function (key, value) {return common.cache_set(module.name + key, value);};
        var cache_del = function (key) {return common.cache_del(module.name + key);};
        var check = function (params) {
            common.check(params);
            if (typeof params.bundle.config.handler !== 'function') params.bundle.config.handler = $.noop;
            if (params.bundle.config.page && (params.bundle.config.from || params.bundle.config.to))
                throw new TypeError(params.bundle.method + ': config.page + config.from/to is ambiguous');
            if (str(params.bundle.config.to) && !str(params.bundle.config.from))
                throw new TypeError(params.bundle.method + ': config.to requires config.from');
            if (params.bundle.config.page_size === '*' || params.bundle.config.page === '*')
                params.bundle.config.page_size = MAX_INT, params.bundle.config.page = PAGE;
            return ['handler', 'loading', 'update', 'dependencies', 'cache_for', 'dry'].reduce(function (acc, val) {
                return (val in params.bundle.config) && (acc[val] = params.bundle.config[val]), acc;
            }, {type: 'GET'});
        };
        var default_del = function (config) {
            config = config || {};
            var root = this.root, method = [root], meta, id = str(config.id), version = str(config.version);
            meta = check({
                bundle: {method: root + '#del', config: config},
                required: [{all_of: ['id']}],
                dependencies: [{fields: ['version'], require: 'id'}]
            });
            meta.type = 'DELETE';
            return request(method.concat(version ? [id, 'versions', version] : id), {data: {}, meta: meta});
        };
        var default_get = function (fields, api_fields, config) {
            config = config || {};
            var root = this.root, method = [root], data = {}, meta,
                all = fields.concat('id', 'version', 'page_size', 'page', 'from', 'to'),
                id = str(config.id), version = str(config.version), version_search = version === '*',
                field_search = fields.some(function (val) {return val in config;}),
                ids = config.ids, id_search = ids && $.isArray(ids) && ids.length,
                search = field_search || id_search || version_search || !id,
                has_meta = root in meta_data, meta_request = has_meta && config.meta;
            meta = check({
                bundle: {method: root + '#get', config: config},
                dependencies: [{fields: ['version'], require: 'id'}],
                empties: [
                    {condition: field_search || id_search, label: 'search request', fields: ['version', 'id']},
                    {condition: !has_meta, label: 'meta data unavailable for /' + root, fields: ['meta']},
                    {condition: meta_request, label: 'meta data request', fields: all},
                    {condition: !has_id_search[root], label: 'id search unavailable for ' + root, fields: ['ids']}
                ]
            });
            if (meta_request) method.push('metaData');
            if (search) data = paginate(config);
            if (field_search) fields.forEach(function (val, idx) {
                if (val = str(config[val])) data[(api_fields || fields)[idx]] = val;
            });
            if (id_search) data[singular[root] + 'Id'] = ids;
            version = version ? [id, 'versions', version_search ? false : version].filter(Boolean) : id;
            if (id) method = method.concat(version);
            return request(method, {data: data, meta: meta});
        };
        var not_available = function (method) {
            throw new Error(this.root + '#' + method + ' does not exist in the REST API');
        };
        var not_implemented = function (method) {
            throw new Error(this.root + '#' + method + ' exists in the REST API, but does not have a JS version');
        };
        var paginate = function (config) {
            var from = str(config.from), to = str(config.to);
            return from ? {'pgIdx': from, 'pgSze': to ? +to - +from : str(config.page_size) || PAGE_SIZE}
                : {'pgSze': str(config.page_size) || PAGE_SIZE, 'pgNum': str(config.page) || PAGE};
        }
        var post_process = function (data, url) {return post_processors[url] ? post_processors[url](data) : data;};
        post_processors[live_data_root + 'compressor/compress'] = function (data) {
            return (data.data = data.data.replace(/\=/g, '-').replace(/\//g, '_').replace(/\+/g, '.')), data;
        };
        var Promise = function () {
            var deferred = new $.Deferred, promise = deferred.promise();
            promise.deferred = deferred;
            promise.id = ++request_id;
            return promise;
        };
        var register = function (req) {
            if (!req.config.meta.update) return true;
            if (!api.id) return false;
            if (registrations.reduce(function (acc, val) { // do not add duplicates
                return val === null ? false
                    : acc || val.method.join('/') === req.method.join('/') && val.update === req.config.meta.update;
            }, false)) return true;
            return !!registrations.push({
                id: req.id, dependencies: req.config.meta.dependencies || [], config: req.config, url: req.url,
                method: req.method, update: req.config.meta.update, current: req.current
            });
        };
        var request = function (method, config, promise) {
            var no_post_body = {GET: 0, DELETE: 0}, current = routes.current(),
                is_get = config.meta.type === 'GET',
                is_delete = config.meta.type === 'DELETE',
                // build GET/DELETE URLs instead of letting $.ajax do it
                url = config.url || (config.meta.type in no_post_body ?
                    [live_data_root + method.map(encode).join('/'), $.param(config.data, true)]
                        .filter(Boolean).join('?')
                            : live_data_root + method.map(encode).join('/')),
            promise = promise || new Promise;
            /** @ignore */
            var send = function () {
                // GETs are being POSTed with method=GET so they do not cache. TODO: change this
                outstanding_requests[promise.id].ajax = $.ajax({
                    url: url,
                    type: is_get ? 'POST' : config.meta.type,
                    data: is_get ? $.extend(config.data, {method: 'GET'}) : config.data,
                    headers: {'Accept': 'application/json', 'Cache-Control': 'no-cache'},
                    dataType: 'json',
                    timeout: config.meta.timeout || (is_get ? TIMEOUTSOON : TIMEOUTFOREVER),
                    beforeSend: function (xhr, req) {
                        var aborted = !(promise.id in outstanding_requests),
                            message = (aborted ? 'ABORTED: ' : '') + req.type + ' ' + req.url + ' HTTP/1.1' +
                                (!is_get ? '\n\n' + req.data : '');
                        og.dev.log(message);
                        if (aborted) return false;
                    },
                    error: function (xhr, status, error) {
                        // re-send requests that have timed out only if they are GETs (/updates requests don't time out)
                        if (error === 'timeout' && is_get && !config.meta.is_update) return send();
                        var result = {
                            error: xhr.status || true, data: null,
                            meta: {content_length: (xhr.responseText || '').length, url: url},
                            message: status === 'parsererror' ? 'JSON parser failed'
                                : xhr.responseText || 'There was no response from the server.'
                        };
                        delete outstanding_requests[promise.id];
                        if (error === 'abort') return; // do not call handler if request was cancelled
                        config.meta.handler(result);
                        promise.deferred.resolve(result);
                    },
                    success: function (data, status, xhr) {
                        if (promise.ignore) return;
                        var meta = {content_length: xhr.responseText.length, url: url},
                            location = xhr.getResponseHeader('Location'), result, cache_for;
                        delete outstanding_requests[promise.id];
                        if (location && ~!location.indexOf('?')) meta.id = location.split('/').pop();
                        result = {error: false, message: status, data: post_process(data, url), meta: meta};
                        if (cache_for = config.meta.cache_for)
                            cache_set(url, result), setTimeout(function () {cache_del(url);}, cache_for);
                        config.meta.handler(result);
                        promise.deferred.resolve(result);
                    },
                    complete: loading_end
                });
            };
            if (is_get && !register({id: promise.id, config: config, current: current, url: url, method: method}))
                // if registration fails, it's because we don't have a client ID yet, so stall
                return setTimeout(request.partial(method, config, promise), STALL), promise;
            if (!is_get && og.app.READ_ONLY) return setTimeout(function () {
                var result = {error: true, data: null, meta: {}, message: 'The app is in read-only mode.'};
                config.meta.handler(result);
                promise.deferred.resolve(result);
            }, INSTANT), promise;
            if (config.meta.update) if (is_get) config.data['clientId'] = api.id;
                else warn(module.name + ': update functions are only for GETs');
            if (config.meta.cache_for && !is_get)
                warn(module.name + ': only GETs can be cached'), delete config.meta.cache_for;
            loading_start(config.meta.loading);
            if (is_get) { // deal with client-side caching of GETs
                if (cache_get(url) && typeof cache_get(url) === 'object') return setTimeout((function (result) {
                    return function () {config.meta.handler(result), promise.deferred.resolve(result);};
                })(cache_get(url)), INSTANT), promise;
                if (cache_get(url)) // if cache_get returns true a request is already outstanding, so stall
                    return setTimeout(request.partial(method, config, promise), STALL), promise;
                if (config.meta.cache_for) cache_set(url, true);
            }
            outstanding_requests[promise.id] = {
                current: current, dependencies: config.meta.dependencies, promise: promise
            };
            if (is_delete) registrations = registrations
                .filter(function (reg) {return !~reg.method.join('/').indexOf(method.join('/'));});
            return config.meta.dry ? promise : send(), promise;
        };
        var request_expired = function (request, current) {
            return (current.page !== request.current.page) || request.dependencies.some(function (field) {
                return current.args[field] !== request.current.args[field];
            });
        };
        var simple_get = function (config) {
            meta = check({bundle: {method: this.root + '#get', config: config || {}}});
            return request([this.root], {data: {}, meta: meta});
        };
        api = {
            abort: function (promise) {
                if (!promise) return;
                var xhr = outstanding_requests[promise.id] && outstanding_requests[promise.id].ajax;
                api.deregister(promise);
                // if request is still outstanding remove it
                if (!xhr) return; else delete outstanding_requests[promise.id];
                if (typeof xhr === 'object' && 'abort' in xhr) xhr.abort();
            },
            aggregators: { // all requests that begin with /aggregators
                root: 'aggregators',
                get: simple_get,
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            clean: function () {
                var id, current = routes.current(), request;
                for (id in outstanding_requests) { // clean up outstanding requests
                    if (!(request = outstanding_requests[id]).dependencies) continue;
                    if (request_expired(request, current)) api.abort({id: id});
                }
                // clean up registrations
                registrations.filter(request_expired.partial(void 0, current)).pluck('id')
                    .forEach(function (id) {api.abort({id: id});});
            },
            compressor: { // all requests that begin with /compressor
                root: 'compressor',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = [root, 'decompress'], data = {}, meta;
                    meta = check({bundle: {method: root + '#get', config: config}, required: [{all_of: ['content']}]});
                    meta.type = 'POST';
                    data.content = config.content.replace(/\-/g, '=').replace(/\_/g, '\/').replace(/\./g, '+');
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    config = config || {};
                    var root = this.root, method = [root, 'compress'], data = {}, meta;
                    meta = check({bundle: {method: root + '#put', config: config}, required: [{all_of: ['content']}]});
                    meta.type = 'POST';
                    data.content = JSON.stringify(config.content);
                    return request(method, {data: data, meta: meta});
                },
                del: not_available.partial('del')
            },
            configs: { // all requests that begin with /configs
                root: 'configs',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), version = str(config.version), version_search = version === '*',
                        fields = ['name', 'type'], field_search = fields.some(function (val) {return val in config;}),
                        all = fields.concat('id', 'version', 'page_size', 'page', 'from', 'to'),
                        ids = config.ids, id_search = ids && $.isArray(ids) && ids.length,
                        meta_request = config.meta, template = str(config.template);
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        dependencies: [{fields: ['version'], require: 'id'}],
                        empties: [
                            {condition: template, label: 'template request', fields: all.concat('meta')},
                            {condition: field_search || id_search, label: 'search request', fields: ['version', 'id']},
                            {condition: meta_request, label: 'meta data request', fields: all}
                        ]
                    });
                    if (meta_request) method.push('metaData');
                    if (!meta_request && !template && (field_search || version_search || id_search || !id))
                        data = paginate(config);
                    if (field_search) fields
                        .forEach(function (val, idx) {if (val = str(config[val])) data[fields[idx]] = val;});
                    if (data.type === '*') delete data.type; // * is superfluous here
                    if (id_search) data.configId = ids;
                    if (template) method.push('templates', template);
                    if (id) method = method.concat(version ? [id, 'versions', version_search ? '' : version] : id);
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), fields = ['name', 'json', 'type', 'xml'],
                        api_fields = ['name', 'configJSON', 'type', 'configXML'];
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        empties: [{
                            condition: !!config.json, label: 'json and xml are mutually exclusive', fields: ['xml']
                        }]
                    });
                    meta.type = id ? 'PUT' : 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    if (id) method.push(id);
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            deregister: function (promise) {
                registrations = registrations.filter(function (val) {return val.id !== promise.id;});
            },
            disconnected: false,
            exchanges: { // all requests that begin with /exchanges
                root: 'exchanges',
                get: default_get.partial(['name'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            fire: og.common.events.fire,
            handshake: { // all requests that begin with /handshake
                root: 'handshake',
                get: function (config) {
                    config = config || {};
                    if (api.id) warn(module.name + ': handshake has already been called');
                    var root = this.root, data = {}, meta;
                    meta = check({bundle: {method: root + '#get', config: config}});
                    return request(null, {url: '/handshake', data: data, meta: meta});
                },
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            holidays: { // all requests that begin with /holidays
                root: 'holidays',
                get: default_get.partial(['name', 'type', 'currency'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            id: null,
            livedatasources: { // all requests that begin with /livedatasources
                root: 'livedatasources',
                get: simple_get,
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            marketdatasnapshots: { // all requests that begin with /marketdatasnapshots
                root: 'marketdatasnapshots',
                get: default_get.partial([], null),
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            off: og.common.events.off,
            on: og.common.events.on,
            portfolios: { // all requests that begin with /portfolios
                root: 'portfolios',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), node = str(config.node), version = str(config.version),
                        name = str(config.name), name_search =  'name' in config, version_search = version === '*',
                        ids = config.ids, id_search = ids && $.isArray(ids) && ids.length,
                        nodes = config.nodes, node_search = nodes && $.isArray(nodes) && nodes.length,
                        search = !id || id_search || node_search || name_search || version_search;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        dependencies: [{fields: ['node', 'version'], require: 'id'}],
                        required: [{condition: version_search, one_of: ['id', 'node']}],
                        empties: [
                            {
                                condition: name_search || id_search || node_search,
                                label: 'search request cannot have id, node, or version',
                                fields: ['id', 'node', 'version']
                            },
                            {label: 'meta data unavailable for /' + root, fields: ['meta']}
                        ]
                    });
                    if (search) data = paginate(config);
                    if (name_search) data.name = name;
                    if (id_search) data['portfolioId'] = ids;
                    if (node_search) data['nodeId'] = nodes;
                    version = version ? [id, 'versions', version_search ? false : version].filter(Boolean) : id;
                    if (id) method = method.concat(version);
                    if (node) method.push('nodes', node);
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        name = str(config.name), id = str(config.id), version = str(config.version),
                        node = str(config.node), new_node = config['new'], position = str(config.position);
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        required: [{one_of: ['name', 'id']}, {one_of: ['name', 'position']}],
                        dependencies: [
                            {fields: ['node', 'version', 'position'], require: 'id'},
                            {fields: ['position'], require: 'node'}
                        ],
                        empties: [
                            {condition: !!name, label: 'name exists', fields: ['position']},
                            {condition: new_node, label: 'node is not set to an ID', fields: ['position']}
                        ]
                    });
                    meta.type = !id || new_node || position ? 'POST' : 'PUT';
                    if (name) data.name = name;
                    if (id) method = method.concat(version ? [id, 'versions', version] : id);
                    if (new_node || node) method = method.concat(new_node && !node ? 'nodes' : ['nodes', node]);
                    if (position) method.push('positions'), data.uid = position;
                    return request(method, {data: data, meta: meta});
                },
                del: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], meta,
                        id = str(config.id), version = str(config.version),
                        node = str(config.node), position = str(config.position);
                    meta = check({
                        bundle: {method: root + '#del', config: config},
                        required: [{all_of: ['id']}],
                        dependencies: [
                            {fields: ['node', 'version', 'position'], require: 'id'},
                            {fields: ['position'], require: 'node'}
                        ]
                    });
                    meta.type = 'DELETE';
                    method = method.concat(version ? [id, 'versions', version] : id);
                    if (node) method.push('nodes', node);
                    if (position) method.push('positions', position);
                    return request(method, {data: {}, meta: meta});
                }
            },
            positions: { // all requests that begin with /positions
                root: 'positions',
                get: default_get.partial(['min_quantity', 'max_quantity', 'identifier'],
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
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            regions: { // all requests that begin with /regions
                root: 'regions',
                get: default_get.partial(['name', 'classification'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            register: register,
            registrations: function () {return registrations;},
            securities: { // all requests that begin with /securities
                root: 'securities',
                get: default_get.partial(['name', 'type'], null),
                put: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), fields = ['identifier', 'scheme_type'],
                        api_fields = ['idvalue', 'idscheme'];
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        empties: [{condition: !!id, label: 'ID exists', fields: fields}],
                        required: [{condition: !id, all_of: fields}]
                    });
                    meta.type = id ? 'PUT' : 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    if (id) method.push(id);
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            sync: {  // all requests that begin with /sync
                root: 'sync',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta, fields = ['status', 'trades'];
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        required: [{one_of: fields}],
                        empties: [{condition: config.status, fields: ['trades'], label: 'status exists'}]
                    });
                    fields.forEach(function (field) {if (config[field]) method.push(config[field], field);});
                    return request(method, {data: data, meta: meta});
                },
                put: not_implemented.partial('put'),
                del: not_available.partial('del')
            },
            timeseries: { // all requests that begin with /timeseries
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
                        data = paginate(config);
                        fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    } else {
                        method.push(id);
                    }
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta,
                        fields = ['data_provider', 'data_field', 'start', 'end', 'scheme_type', 'identifier'],
                        api_fields = ['dataProvider', 'dataField', 'start', 'end', 'idscheme', 'idvalue'];
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        required: [{all_of: ['data_provider', 'data_field', 'scheme_type', 'identifier']}]
                    });
                    meta.type = 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            updates: { // all requests that begin with /updates
                root: 'updates',
                get: function (config) {
                    config = config || {};
                    var root = this.root, data = {}, meta;
                    meta = check({bundle: {method: root + '#get', config: config}});
                    meta.timeout = 12500; meta.is_update = true; // back-end will timeout at 10s, so 12.5 should be fine
                    return request(null, {url: ['', root, api.id].join('/'), data: data, meta: meta});
                },
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            valuerequirementnames: {
                root: 'valuerequirementnames',
                get: function (config) {
                    config = config || {};
                    var root = this.root, method = [root], data = {}, meta, meta_request = config.meta;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        required: [{all_of: ['meta']}]
                    });
                    data = paginate(config);
                    if (meta_request) method.push('metaData');
                    return request(method, {data: data, meta: meta});
                },
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            viewdefinitions: { // all requests that begin with /viewdefinitions
                root: 'viewdefinitions',
                get: default_get.partial([], null),
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            views: { // all requests that begin with /views
                root: 'views',
                get: not_available.partial('get'),
                put: function (config, promise) {
                    config = config || {};
                    var promise = promise || new Promise,
                        root = this.root, method = [root], data = {}, meta,
                        fields = [
                            'viewdefinition', 'aggregators', 'providers', 'valuation', 'version', 'correction'
                        ],
                        api_fields = [
                            'viewDefinitionId', 'aggregators', 'marketDataProviders',
                            'valuationTime', 'portfolioVersionTime', 'portfolioCorrectionTime'
                        ];
                    if (!api.id) return setTimeout((function (context) {                    // if handshake isn't
                        return function () {api.views.put.call(context, config, promise);}; // complete, return a
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
                    return request(method, {data: data, meta: meta}, promise);
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
                    return request(method, {data: {}, meta: meta});
                },
                grid: {
                    depgraphs: {
                        root: 'views/{{view_id}}/{{grid_type}}/depgraphs',
                        del: not_implemented.partial('del'),
                        get: not_available.partial('get'),
                        structure: {
                            root: 'views/{{view_id}}/{{grid_type}}/depgraphs/{{graph_id}}',
                            get: function (config) {
                                var root = this.root, method = root.split('/'), data = {}, meta;
                                meta = check({
                                    bundle: {method: root + '#get', config: config},
                                    required: [{all_of: ['view_id', 'grid_type', 'graph_id']}]
                                });
                                method[1] = config.view_id;
                                method[2] = config.grid_type;
                                method[4] = config.graph_id;
                                return request(method, {data: data, meta: meta});
                            },
                            put: not_available.partial('put'),
                            del: not_available.partial('del')
                        },
                        put: function (config) {
                            var promise = new Promise, root = this.root, method = root.split('/'), data = {}, meta,
                                fields = ['view_id', 'grid_type', 'row', 'col'],
                            meta = check({
                                bundle: {method: root + '#put', config: config}, required: [{all_of: fields}]
                            });
                            meta.type = 'POST';
                            promise.ignore = true; // this request will be answered in fire_updates NOT in ajax handler
                            data['requestId'] = promise.id;
                            data['clientId'] = api.id;
                            method[1] = config.view_id;
                            method[2] = config.grid_type;
                            fields.forEach(function (val, idx) {if (val = str(config[val])) data[fields[idx]] = val;});
                            return request(method, {data: data, meta: meta}, promise);
                        },
                        viewports: {
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
                                return request(method, {data: data, meta: meta});
                            },
                            put: function (config) {
                                config = config || {};
                                var promise = new Promise, root = this.root, method = root.split('/'), data = {}, meta,
                                    fields = ['cells', 'rows', 'cols', 'format', 'log'],
                                    api_fields = ['cells', 'rows', 'columns', 'format', 'enableLogging'];
                                meta = check({
                                    bundle: {method: root + '#put', config: config},
                                    required: [
                                        {all_of: ['view_id', 'graph_id', 'format']},
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
                                return request(method, {data: data, meta: meta}, promise);
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
                                return request(method, {data: {}, meta: meta});
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
                            return request(method, {data: data, meta: meta});
                        },
                        put: not_available.partial('put'),
                        del: not_available.partial('del')
                    },
                    viewports: {
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
                            return request(method, {data: data, meta: meta});
                        },
                        put: function (config) {
                            config = config || {};
                            var promise = new Promise, root = this.root, method = root.split('/'), data = {}, meta,
                                fields = ['cells', 'rows', 'cols', 'format', 'log'],
                                api_fields = ['cells', 'rows', 'columns', 'format', 'enableLogging'];
                            meta = check({
                                bundle: {method: root + '#put', config: config},
                                required: [
                                    {all_of: ['view_id', 'format']},
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
                            if (config.viewport_id) // use the promise id as viewport_version
                                (meta.type = 'PUT'), method.push(config.viewport_id);
                            else // the response will come back in fire_updates
                                (promise.ignore = true), (data['requestId'] = promise.id);
                            return request(method, {data: data, meta: meta}, promise);
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
                            return request(method, {data: {}, meta: meta});
                        }
                    }
                }
            }
        };
        common.cache_clear(module.name); // empty the cache from another session or window if it still exists
        api.subscribe = subscribe = api.handshake.get.partial({handler: function (result) {
            var listen, fire_updates;
            if (result.error)
                return warn(module.name + ': handshake failed\n', result.message), setTimeout(subscribe, RESUBSCRIBE);
            api.id = result.data['clientId'];
            (fire_updates = function (reset, result) {
                var current = routes.current();
                if (reset && api.disconnected) (api.disconnected = false), api.fire('reconnect');
                registrations = registrations // throw out stale registrations
                    .filter(function (reg) {return !request_expired(reg, current);});
                if (reset) return registrations = registrations // fire all updates if connection is reset (and clear)
                    .filter(function (reg) {return reg.update($.extend({reset: true}, reg)) && false;});
                result.data.updates.filter(function (update) {
                    var simple = typeof update === 'string', promise, request;
                    if (!simple && (promise = (request = outstanding_requests[update.id]) && request.promise)){
                        promise.deferred
                            .resolve({error: false, data: null, meta: {id: update.message.split('/').pop()}});
                        delete outstanding_requests[promise.id];
                    }
                    return simple;
                }).forEach(function (update) {
                    var lcv, len = registrations.length, reg;
                    for (lcv = 0; lcv < len; lcv += 1)
                        if ((reg = registrations[lcv]).url === update) (registrations[lcv] = null), reg.update(reg);
                    registrations = registrations.filter(Boolean);
                });
            })(true, null); // there are no registrations when subscribe() is called unless the connection's been reset
            (listen = function () {
                api.updates.get({handler: function (result) {
                    if (result.error) {
                        if (!api.disconnected) (api.disconnected = true), api.fire('disconnect'), (api.id = null);
                        warn(module.name + ': subscription failed\n', result.message);
                        return setTimeout(subscribe, RESUBSCRIBE);
                    }
                    if (!result.data || !result.data.updates.length) return setTimeout(listen, INSTANT);
                    fire_updates(false, result);
                    setTimeout(listen, INSTANT);
                }});
            })();
        }});
        return api;
    }
});
