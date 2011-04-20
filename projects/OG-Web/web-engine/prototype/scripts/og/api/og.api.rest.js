/**
 * provides wrappers for the REST API
 */
$.register_module({
    name: 'og.api.rest',
    dependencies: ['og.dev', 'og.api.common', 'og.api.live', 'og.common.routes'],
    obj: function () {
        var module = this, live_data_root = module.liveDataRoot, api,
            common = og.api.common, live = og.api.live, routes = og.common.routes, start_loading = common.start_loading,
            end_loading = common.end_loading, encode = encodeURIComponent,
            outstanding_requests = {}, registrations = [],
            request_id = 0, PAGE_SIZE = 50, PAGE = 1, TIMEOUT = 120000, // 2 minute timeout
            /** @ignore */
            register = function (obj) {
                var url = obj.url, current = obj.current, update = obj.config.meta.update,
                    dependencies = obj.config.meta.dependencies || [], id = obj.id;
                if (!update) return;
                registrations.push({id: id, dependencies: dependencies, update: update, url: url, current: current});
                live.register(registrations.map(function (val) {return val.url;}).join('\n'));
            },
            /** @ignore */
            filter_registrations = function (filter) {registrations = registrations.filter(filter);},
            /** @ignore */
            deliver_updates = function (updates) {
                var current = routes.current(), handlers = [];
                filter_registrations(function (val) {
                    return request_expired(val, current) ? false : !updates.some(function (url) {
                        var match = url === val.url;
                        return match && handlers.push(val), match;
                    });
                });
                handlers.forEach(function (val) {val.update.call(val);});
                live.register(registrations.map(function (val) {return val.url;}).join('\n'));
            },
            /** @ignore */
            request = function (method, config) {
                var id = request_id++, url = live_data_root + method.map(encode).join('/'), current = routes.current(),
                    /** @ignore */
                    send = function () {
                        outstanding_requests[id].ajax = $.ajax({
                            url: url,
                            type: config.meta.type,
                            data: config.data,
                            headers: {'Accept': 'application/json'},
                            dataType: 'json',
                            timeout: TIMEOUT,
                            beforeSend: function (xhr, req) {
                                var aborted = !(id in outstanding_requests),
                                    message = (aborted ? 'ABORTED: ' : '') + req.type + ' ' + req.url + ' HTTP/1.1' +
                                        (req.type !== 'GET' ? '\n\n' + req.data : '');
                                og.dev.log(message);
                                if (aborted) return false;
                            },
                            error: function (xhr, status, error) {
                                if (error === 'timeout') return send(); // re-send requests that have timed out
                                delete outstanding_requests[id];
                                if (error === 'abort') return; // do not call handler if request was cancelled
                                config.meta.handler({
                                    error: true, data: null, meta: {},
                                    message: status === 'parsererror' ? 'JSON parser failed' : xhr.responseText
                                });
                            },
                            success: function (data, status, xhr) {
                                var meta = {}, location = xhr.getResponseHeader('Location');
                                delete outstanding_requests[id];
                                if (location) meta.id = location.split('/').pop();
                                config.meta.handler({error: false, message: status, data: data, meta: meta});
                            },
                            complete: end_loading
                        });
                        return id;
                    };
                if (config.meta.type === 'GET') register({
                    id: id, config: config, current: current, url: [url, $.param(config.data)].filter(Boolean).join('?')
                });
                if (config.meta.update && config.meta.type !== 'GET') og.dev.warn('update functions are only for GETs');
                start_loading(config.meta.loading);
                outstanding_requests[id] = {current: current, dependencies: config.meta.dependencies};
                return send();
            },
            /** @ignore */
            request_expired = function (request, current) {
                return (current.page !== request.current.page) || request.dependencies.some(function (field) {
                    return current.args[field] !== request.current.args[field];
                });
            },
            /** @ignore */
            check = function (params) {
                common.check(params);
                if (typeof params.bundle.config.handler !== 'function')
                    throw new TypeError(params.bundle.method + ': config.handler must be a function');
                return ['handler', 'loading', 'update', 'dependencies'].reduce(function (acc, val) {
                    return (val in params.bundle.config) && (acc[val] = params.bundle.config[val]), acc;
                }, {type: 'GET'});
            },
            // convert all incoming params into strings (so for example, the value 0 ought to be truthy, not falsey)
            str = common.str,
            /** @ignore */
            default_get = function (fields, api_fields, config) {
                var root = this.root, method = [root], data = {}, meta,
                    id = str(config.id), version = str(config.version), version_search = version === '*',
                    field_search = fields.some(function (val) {return val in config;}),
                    page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                meta = check({
                    bundle: {method: root + '#get', config: config},
                    dependencies: [{fields: ['version'], require: 'id'}],
                    empties: [{condition: field_search, label: 'search request', fields: ['version', 'id']}]
                });
                if (field_search || version_search || !id) data = {pageSize: page_size, page: page};
                if (field_search) fields.forEach(function (val, idx) {
                    if (val = str(config[val])) data[(api_fields || fields)[idx]] = val;
                });
                if (id) method = method.concat(version ? [id, 'versions', version_search ? '' : version] : id);
                return request(method, {data: data, meta: meta});
            },
            /** @ignore */
            default_del = function (config) { // *this* refers to the child node of api that default_del is in
                var root = this.root, method = [root], meta,
                    id = str(config.id), version = str(config.version);
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
        return api = {
            abort: function (id) {
                var xhr = outstanding_requests[id] && outstanding_requests[id].ajax;
                // if request is registered as an update listener, remove it
                filter_registrations(function (val) {return val.id !== id;});
                deliver_updates([]);
                // if request is still outstanding remove it
                if (!xhr) return; else delete outstanding_requests[id];
                if (typeof xhr === 'object' && 'abort' in xhr) xhr.abort();
            },
            batches: { // all requests that begin with /batches
                root: 'batches',
                get: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE,
                        observation_date = str(config.observation_date),
                        observation_time = str(config.observation_time),
                        is_id = !!observation_date && !!observation_time;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        empties: [{condition: is_id, label: 'unique batch requested', fields: ['page', 'page_size']}]
                    });
                    if (is_id){
                        method.push(observation_date, observation_time);
                    }else{
                        data = {pageSize: page_size, page: page};
                        if (observation_date) data.observationDate = observation_date;
                        if (observation_time) data.observationTime = observation_time;
                    };
                    return request(method, {data: data, meta: meta});
                },
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            clean: function () {
                var id, current = routes.current(), request, mismatch;
                for (id in outstanding_requests){
                    if (!(request = outstanding_requests[id]).dependencies) continue;
                    if (request_expired(request, current)) api.abort(id);
                };
                deliver_updates([]);
            },
            configs: { // all requests that begin with /configs
                root: 'configs',
                get: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), version = str(config.version), version_search = version === '*',
                        fields = ['name', 'type'], type_search = config.type === '*',
                        field_search = !type_search && fields.some(function (val) {return val in config;}),
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        dependencies: [{fields: ['version'], require: 'id'}],
                        empties: [
                            {condition: field_search, label: 'search request', fields: ['version', 'id']},
                            {condition: type_search, label: 'type search request', fields: ['version', 'id', 'name']}
                        ]
                    });
                    if (!type_search && (field_search || version_search || !id))
                        data = {pageSize: page_size, page: page};
                    if (field_search) fields.forEach(function (val, idx) {
                        if (val = str(config[val])) data[fields[idx]] = val;
                    });
                    if (id) method = method.concat(version ? [id, 'versions', version_search ? '' : version] : id);
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), fields = ['name', 'xml'], api_fields = ['name', 'configxml'];
                    meta = check({bundle: {method: root + '#put', config: config}, required: [{all_of: fields}]});
                    meta.type = id ? 'PUT' : 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    if (id) method.push(id);
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            exchanges: { // all requests that begin with /exchanges
                root: 'exchanges',
                get: default_get.partial(['name'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            holidays: { // all requests that begin with /holidays
                root: 'holidays',
                get: default_get.partial(['name', 'type', 'currency'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            portfolios: { // all requests that begin with /portfolios
                root: 'portfolios',
                get: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), node = str(config.node), version = str(config.version),
                        name = str(config.name), name_search =  'name' in config, version_search = version === '*',
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        dependencies: [{fields: ['node', 'version'], require: 'id'}],
                        empties: [
                            {condition: name_search, label: 'name exists', fields: ['node', 'version', 'id']},
                            {condition: version_search, label: 'version is *', fields: ['node']}
                        ]
                    });
                    if (version_search || name_search || !id) data = {pageSize: page_size, page: page};
                    if (name_search) data.name = name;
                    version = version ? [id, 'versions', version_search ? '' : version] : id;
                    if (id) method = method.concat(node ? [version, 'nodes', node] : version);
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
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
                    var root = this.root, method = [root], meta,
                        id = str(config.id), version = str(config.version), node = str(config.node);
                    meta = check({
                        bundle: {method: root + '#del', config: config},
                        required: [{all_of: ['id']}],
                        dependencies: [{fields: ['node', 'version'], require: 'id'}]
                    });
                    meta.type = 'DELETE';
                    method = method.concat(version ? [id, 'versions', version] : id);
                    if (node) method.push('nodes', node);
                    return request(method, {data: {}, meta: meta});
                }
            },
            positions: { // all requests that begin with /positions
                root: 'positions',
                get: default_get.partial(['min_quantity', 'max_quantity', 'identifier'],
                        ['minquantity', 'maxquantity', 'identifier']),
                put: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id), version = str(config.version),
                        fields = ['identifier', 'quantity', 'scheme_type'],
                        api_fields = ['idvalue', 'quantity', 'idscheme'];
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        dependencies: [{fields: ['version'], require: 'id'}],
                        required: [{condition: !id, all_of: fields}, {condition: !!id, all_of: ['quantity']}]
                    });
                    meta.type = id ? 'PUT' : 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
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
            registrations: function () {return registrations;},
            securities: { // all requests that begin with /securities
                root: 'securities',
                get: default_get.partial(['name', 'type'], null),
                put: function (config) {
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
            timeseries: { // all requests that begin with /timeseries
                root: 'timeseries',
                get: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        id = str(config.id),
                        fields = ['identifier', 'data_source', 'data_provider', 'data_field', 'observation_time'],
                        api_fields = ['identifier', 'dataSource', 'dataProvider', 'dataField', 'observationTime'],
                        search = !id || fields.some(function (val) {return val in config;}),
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                    meta = check({
                        bundle: {method: root + '#get', config: config},
                        empties: [{condition: search, label: 'search request', fields: ['id']}]
                    });
                    if (search){
                        data = {pageSize: page_size, page: page};
                        fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    }else{
                        method.push(id);
                    };
                    return request(method, {data: data, meta: meta});
                },
                put: function (config) {
                    var root = this.root, method = [root], data = {}, meta,
                        fields = ['data_provider', 'data_field', 'start', 'end', 'scheme_type', 'identifier'],
                        api_fields = ['dataProvider', 'dataField', 'start', 'end', 'idscheme', 'idvalue'];
                    meta = check({
                        bundle: {method: root + '#put', config: config},
                        required: [{all_of: fields}]
                    });
                    meta.type = 'POST';
                    fields.forEach(function (val, idx) {if (val = str(config[val])) data[api_fields[idx]] = val;});
                    return request(method, {data: data, meta: meta});
                },
                del: default_del
            },
            update : deliver_updates
        };
    }
});
