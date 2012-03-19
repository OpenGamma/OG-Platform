/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * provides wrappers for the REST API
 */
$.register_module({
    name: 'og.api.rest',
    dependencies: ['og.dev', 'og.api.common', 'og.common.routes'],
    obj: function () {
        var module = this, live_data_root = module.live_data_root, api, warn = og.dev.warn,
            common = og.api.common, routes = og.common.routes, start_loading = common.start_loading,
            end_loading = common.end_loading, encode = window['encodeURIComponent'], cache = window['sessionStorage'],
            outstanding_requests = {}, registrations = [], subscribe,
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
            INSTANT = 0 /* 0ms */, RESUBSCRIBE = 30000 /* 20s */,
            TIMEOUTSOON = 120000 /* 2m */, TIMEOUTFOREVER = 7200000 /* 2h */,
            /** @ignore */
            register = function (req) {
                return !req.config.meta.update ? true
                    : !api.id ? false
                        : !!registrations.push({
                            id: req.id, dependencies: req.config.meta.dependencies || [],
                            config: req.config, method: req.method,
                            update: req.config.meta.update, url: req.url, current: req.current
                        });
            },
            /** @ignore */
            get_cache = function (key) {
                try { // if cache is restricted, bail
                    return cache['getItem'](module.name + key) ? JSON.parse(cache['getItem'](module.name + key)) : null;
                } catch (error) {
                    return warn(module.name + ': get_cache failed\n', error);
                    return null;
                }
            },
            /** @ignore */
            set_cache = function (key, value) {
                try { // if the cache is too full, fail gracefully
                    cache['setItem'](module.name + key, JSON.stringify(value));
                } catch (error) {
                    warn(module.name + ': set_cache failed\n', error);
                    del_cache(key);
                }
            },
            /** @ignore */
            del_cache = function (key) {
                try { // if cache is restricted, bail
                    cache['removeItem'](module.name + key);
                } catch (error) {
                    warn(module.name + ': del_cache failed\n', error);
                }
            },
            Promise = function () {
                var deferred = new $.Deferred, promise = deferred.promise();
                promise.deferred = deferred;
                promise.id = ++request_id;
                return promise;
            },
            /** @ignore */
            request = function (method, config, promise) {
                var no_post_body = {GET: 0, DELETE: 0}, is_get = config.meta.type === 'GET',
                    // build GET/DELETE URLs instead of letting $.ajax do it
                    url = config.url || (config.meta.type in no_post_body ?
                        [live_data_root + method.map(encode).join('/'), $.param(config.data, true)]
                            .filter(Boolean).join('?')
                                : live_data_root + method.map(encode).join('/')),
                    current = routes.current(), send;
                promise = promise || new Promise;
                /** @ignore */
                send = function () {
                    // GETs are being POSTed with method=GET so they do not cache. TODO: change this
                    outstanding_requests[promise.id].ajax = $.ajax({
                        url: url,
                        type: is_get ? 'POST' : config.meta.type,
                        data: is_get ? $.extend(config.data, {method: 'GET'}) : config.data,
                        headers: {'Accept': 'application/json'},
                        dataType: 'json',
                        timeout: is_get ? TIMEOUTSOON : TIMEOUTFOREVER,
                        beforeSend: function (xhr, req) {
                            var aborted = !(promise.id in outstanding_requests),
                                message = (aborted ? 'ABORTED: ' : '') + req.type + ' ' + req.url + ' HTTP/1.1' +
                                    (!is_get ? '\n\n' + req.data : '');
                            og.dev.log(message);
                            if (aborted) return false;
                        },
                        error: function (xhr, status, error) {
                            // re-send requests that have timed out only if the are GETs
                            if (error === 'timeout' && is_get) return send();
                            var result = {
                                error: true, data: null, meta: {},
                                message: status === 'parsererror' ? 'JSON parser failed'
                                    : xhr.responseText || 'There was no response from the server.'
                            };
                            delete outstanding_requests[promise.id];
                            if (error === 'abort') return; // do not call handler if request was cancelled
                            config.meta.handler(result);
                            promise.deferred.resolve(result);
                        },
                        success: function (data, status, xhr) {
                            var meta = {content_length: xhr.responseText.length},
                                location = xhr.getResponseHeader('Location'), result, cache_for;
                            delete outstanding_requests[promise.id];
                            if (location && ~!location.indexOf('?')) meta.id = location.split('/').pop();
                            if (config.meta.type in no_post_body) meta.url = url;
                            result = {error: false, message: status, data: data, meta: meta};
                            if (cache_for = config.meta.cache_for)
                                set_cache(url, result), setTimeout(function () {del_cache(url);}, cache_for);
                            config.meta.handler(result);
                            promise.deferred.resolve(result);
                        },
                        complete: end_loading
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
                if (config.meta.update && !is_get) warn(module.name + ': update functions are only for GETs');
                if (config.meta.update && is_get) config.data['clientId'] = api.id;
                if (config.meta.cache_for && !is_get)
                    warn(module.name + ': only GETs can be cached'), delete config.meta.cache_for;
                start_loading(config.meta.loading);
                if (is_get && get_cache(url) && typeof get_cache(url) === 'object')
                    return setTimeout(function () {
                        config.meta.handler(get_cache(url));
                        promise.deferred.resolve(get_cache(url));
                    }, INSTANT), promise;
                if (is_get && get_cache(url)) // if get_cache returns true a request is already outstanding, so stall
                    return setTimeout(request.partial(method, config, promise), STALL), promise;
                if (is_get && config.meta.cache_for) set_cache(url, true);
                outstanding_requests[promise.id] = {current: current, dependencies: config.meta.dependencies};
                return send(), promise;
            },
            /** @ignore */
            request_expired = function (request, current) {
                return (current.page !== request.current.page) || request.dependencies.some(function (field) {
                    return current.args[field] !== request.current.args[field];
                });
            },
            /** @ignore */
            paginate = function (config) {
                var from = str(config.from), to = str(config.to);
                return from ? {'pgIdx': from, 'pgSze': to ? +to - +from : str(config.page_size) || PAGE_SIZE}
                    : {'pgSze': str(config.page_size) || PAGE_SIZE, 'pgNum': str(config.page) || PAGE};
            },
            /** @ignore */
            check = function (params) {
                common.check(params);
                if (typeof params.bundle.config.handler !== 'function') params.bundle.config.handler = $.noop;
                if (params.bundle.config.page && (params.bundle.config.from || params.bundle.config.to))
                    throw new TypeError(params.bundle.method + ': config.page + config.from/to is ambiguous');
                if (str(params.bundle.config.to) && !str(params.bundle.config.from))
                    throw new TypeError(params.bundle.method + ': config.to requires config.from');
                if (params.bundle.config.page_size === '*' || params.bundle.config.page === '*')
                    params.bundle.config.page_size = MAX_INT, params.bundle.config.page = PAGE;
                return ['handler', 'loading', 'update', 'dependencies', 'cache_for'].reduce(function (acc, val) {
                    return (val in params.bundle.config) && (acc[val] = params.bundle.config[val]), acc;
                }, {type: 'GET'});
            },
            // convert all incoming params into strings (so for example, the value 0 ought to be truthy, not falsey)
            str = common.str,
            /** @ignore */
            default_get = function (fields, api_fields, config) {
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
            },
            /** @ignore */
            default_del = function (config) {
                config = config || {};
                var root = this.root, method = [root], meta, id = str(config.id), version = str(config.version);
                meta = check({
                    bundle: {method: root + '#del', config: config},
                    required: [{all_of: ['id']}],
                    dependencies: [{fields: ['version'], require: 'id'}]
                });
                meta.type = 'DELETE';
                return request(method.concat(version ? [id, 'versions', version] : id), {data: {}, meta: meta});
            },
            /** @ignore */
            not_available = function (method) {
                throw new Error(this.root + '#' + method + ' does not exist in the REST API');
            },
            /** @ignore */
            not_implemented = function (method) {
                throw new Error(this.root + '#' + method + ' exists in the REST API, but does not have a JS version');
            };
        // initialize cache so nothing leaks from other sessions (e.g. from a FF crash); if cache is restricted, bail
        try {cache.clear();} catch (error) {warn(module.name + ': cache initalize failed\n', error);}
        api = {
            abort: function (promise) {
                if (!promise) return;
                var xhr = outstanding_requests[promise.id] && outstanding_requests[promise.id].ajax;
                api.deregister(promise);
                // if request is still outstanding remove it
                if (!xhr) return; else delete outstanding_requests[promise.id];
                if (typeof xhr === 'object' && 'abort' in xhr) xhr.abort();
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
                    if (field_search) fields.forEach(function (val, idx) {
                        if (val = str(config[val])) data[fields[idx]] = val;
                    });
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
            exchanges: { // all requests that begin with /exchanges
                root: 'exchanges',
                get: default_get.partial(['name'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
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
            marketdatasnapshots: { // all requests that begin with /marketdatasnapshots
                root: 'marketdatasnapshots',
                get: default_get.partial([], null),
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
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
                            {condition: true, label: 'meta data unavailable for /' + root, fields: ['meta']}
                        ]
                    });
                    if (search) data = paginate(config);
                    if (name_search) data.name = name;
                    if (id_search) data.portfolioId = ids;
                    if (node_search) data.nodeId = nodes;
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
                        required: [{condition: true, all_of: ['meta']}]
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
            }
        };
        (subscribe = api.handshake.get.partial({handler: function (result) {
            var listen, fire_updates;
            if (result.error)
                return warn(module.name + ': handshake failed\n', result.message), setTimeout(subscribe, RESUBSCRIBE);
            api.id = result.data['clientId'];
            (fire_updates = function (reset, result) {
                var current = routes.current(), handlers = [];
                registrations = registrations.filter(function (reg) {
                    return request_expired(reg, current) ? false // purge expired requests
                        // fire all updates if connection is reset (and clear registrations)
                        : reset ? handlers.push($.extend({reset: true}, reg)) && false
                            // otherwise fire matching URLs (and clear them from registrations)
                            : ~result.data.updates.indexOf(reg.url) ? handlers.push(reg) && false
                                // keep everything else registered
                                : true;
                });
                handlers.forEach(function (reg) {reg.update(reg);});
            })(true, null); // there are no registrations when subscribe() is called unless the connection's been reset
            (listen = function () {
                api.updates.get({handler: function (result) {
                    if (result.error) {
                        warn(module.name + ': subscription failed\n', result.message);
                        return setTimeout(subscribe, RESUBSCRIBE);
                    }
                    if (!result.data || !result.data.updates.length) return setTimeout(listen, INSTANT);
                    fire_updates(false, result);
                    setTimeout(listen, INSTANT);
                }});
            })();
        }}))();
        return api;
    }
});
