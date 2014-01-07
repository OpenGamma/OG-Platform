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
            loading_end = common.loading_end, encode = window['encodeURIComponent'], str = common.str,
            outstanding_requests = {}, subscribe, post_processors = {},
            meta_data = {configs: null, holidays: null, securities: null, viewrequirementnames: null},
            singular = { configs: 'config', exchanges: 'exchange', holidays: 'holiday',
                portfolios: 'portfolio', positions: 'position', regions: 'region', securities: 'security',
                timeseries: 'timeseries'},
            has_id_search = { configs: true, exchanges: true, holidays: true, portfolios: true, positions: true,
                regions: true, securities: true, timeseries: false},
            time_out_soon = 300000, /* 5m */
            time_out_forever = 7200000, /* 2h */
            check = common.check, paginate = common.paginate;
        var cache_get = function (key) {return common.cache_get(api.name + key); };
        var cache_set = function (key, value) {return common.cache_set(api.name + key, value); };
        var cache_del = function (key) {return common.cache_del(api.name + key); };
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
            if (meta_request) {
                method.push('metaData');
            }
            if (search) {
                data = paginate(config);
            }
            if (field_search) {
                fields.forEach(function (val, idx) {
                    if (val = str(config[val])) {
                        data[(api_fields || fields)[idx]] = val;
                    }
                });
            }
            if (id_search) {
                data[singular[root] + 'Id'] = ids;
            }
            version = version ? [id, 'versions', version_search ? false : version].filter(Boolean) : id;
            if (id) {
                method = method.concat(version);
            }
            return request(method, {data: data, meta: meta});
        };
        var post_process = function (data, url) {return post_processors[url] ? post_processors[url](data) : data; };
        post_processors[live_data_root + 'compressor/compress'] = function (data) {
            return (data.data = data.data.replace(/\=/g, '-').replace(/\//g, '_').replace(/\+/g, '.')), data;
        };
        var register = function (req) {
            if (!req.config.meta.update) return true;
            if (!api.id) return false;
            if (api.registrations.reduce(function (acc, val) { // do not add duplicates
                return val === null ? false
                    : acc || val.method.join('/') === req.method.join('/') && val.update === req.config.meta.update;
            }, false)) return true;
            return !!api.registrations.push({
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
            promise = promise || new common.Promise;
            /** @ignore */
            var send = function () {
                // GETs are being POSTed with method=GET so they do not cache. TODO: change this

                outstanding_requests[promise.id].ajax = $.ajax({
                    url: url,
                    type: is_get ? 'POST' : config.meta.type,
                    data: is_get ? $.extend(config.data, {method: 'GET'}) : config.data,
                    headers: config.meta.headers || {'Accept': 'application/json', 'Cache-Control': 'no-cache'},
                    dataType: config.meta.datatype || 'json',
                    timeout: config.meta.timeout || (is_get ? time_out_soon : time_out_forever),
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
                            meta: {content_length: (xhr.responseText || '').length, url: url, promise: promise.id},
                            message: status === 'parsererror' ? 'JSON parser failed'
                                : xhr.responseText || 'There was no response from the server.'
                        };
                        delete outstanding_requests[promise.id];
                        if (error === 'abort') return; // do not call handler if request was cancelled
                        if (config.meta.cache_for) cache_del(url);
                        config.meta.handler(result);
                        promise.deferred.resolve(result);
                    },
                    success: function (data, status, xhr) {
                        if (promise.ignore) return;
                        var meta = {content_length: xhr.responseText.length, url: url, promise: promise.id},
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
                return setTimeout(request.partial(method, config, promise), common.STALL), promise;
            if (!is_get && og.app.READ_ONLY) return setTimeout(function () {
                var result = {
                    error: true, data: null, meta: {promise: promise.id}, message: 'The app is in read-only mode.'
                };
                config.meta.handler(result);
                promise.deferred.resolve(result);
            }, common.INSTANT), promise;
            if (config.meta.update) if (is_get) config.data['clientId'] = api.id;
                else warn(api.name + ': update functions are only for GETs');
            if (config.meta.cache_for && !is_get)
                warn(api.name + ': only GETs can be cached'), delete config.meta.cache_for;
            loading_start(config.meta.loading);
            if (is_get) { // deal with client-side caching of GETs
                if (cache_get(url) && typeof cache_get(url) === 'object') return setTimeout((function (result) {
                    result.meta.promise = promise.id; // overwrite the promise id before sending it out
                    return function () {config.meta.handler(result), promise.deferred.resolve(result);};
                })(cache_get(url)), common.INSTANT), promise;
                if (cache_get(url)) // if cache_get returns true a request is already outstanding, so stall
                    return setTimeout(request.partial(method, config, promise), common.STALL), promise;
                if (config.meta.cache_for) cache_set(url, true);
            }
            outstanding_requests[promise.id] = {
                current: current, dependencies: config.meta.dependencies, promise: promise
            };
            if (is_delete) api.registrations = api.registrations
                .filter(function (reg) {return reg && !~reg.method.join('/').indexOf(method.join('/'));});
            return config.meta.dry ? promise : send(), promise;
        };
        var simple_get = function (config) {
            var meta = check({bundle: {method: this.root + '#get', config: config || {}}});
            return request(this.root.split('/'), {data: {}, meta: meta});
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
            clean: function () {
                var id, current = routes.current(), request;
                for (id in outstanding_requests) { // clean up outstanding requests
                    if (!(request = outstanding_requests[id]).dependencies) continue;
                    if (common.request_expired(request, current)) api.abort({id: id});
                }
                // clean up registrations
                api.registrations.filter(common.request_expired.partial(void 0, current)).pluck('id')
                    .forEach(function (id) {api.abort({id: id});});
            },
            default_del: default_del,
            default_get: default_get,
            deregister: function (promise) {
                api.registrations = api.registrations.filter(function (val) {return val.id !== promise.id;});
            },
            disconnected: false,
            fire: og.common.events.fire,
            id: null,
            name: module.name,
            off: og.common.events.off,
            on: og.common.events.on,
            outstanding_requests: outstanding_requests,
            register: register,
            registrations: [],
            request: request,
            simple_get: simple_get
        };
        common.cache_clear(api.name); // empty the cache from another session or window if it still exists
        $(window).on('beforeunload', function () {api.fire('abandon');});
        return api;
    }
});