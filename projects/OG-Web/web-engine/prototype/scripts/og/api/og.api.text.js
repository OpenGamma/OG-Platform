/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * API call for making and caching static requests
 */
$.register_module({
    name: 'og.api.text',
    dependencies: ['og.api.common'],
    obj: function () {
        var html_cache = {}, module = this, api, common = og.api.common,
            loading_start = common.loading_start, loading_end = og.api.common.loading_end,
            path = function (root, extension, page) {
                return !page ? null : root + [page.split('.').slice(1, -1).join('/'), page.toLowerCase() + extension]
                    .join('/').replace(/\s/g, '_');
            },
            html_path = path.partial(module.html_root, '.html'),
            data_path = path.partial(module.data_root, '.json'),
            STALL = common.STALL, INSTANT = common.INSTANT,
            Promise = function () {
                var deferred = new $.Deferred, promise = deferred.promise();
                promise.deferred = deferred;
                return promise;
            };
        return api = function (config, promise) {
            if (typeof config === 'undefined') throw new TypeError('text: config is undefined');
            if (typeof config.url !== 'string' && typeof config.module !== 'string' && typeof config.data !== 'string')
                throw new TypeError('text: config.data, config.url, or config.module must be a string');
            promise = promise || new Promise;
            var do_not_cache = config.do_not_cache, clear_cache = config.clear_cache, is_data,
                url = (config.url || html_path(config.module) || (is_data = data_path(config.data))),
                success_handler = function (response) {
                    if (!do_not_cache) html_cache[url] = response;
                    loading_end();
                    if (config.handler) config.handler(response, false);
                    promise.deferred.resolve(response);
                },
                error_handler = function (response) {
                    var result = new String('Error (HTTP ' + response.status + ') retrieving: ' + url);
                    result.error = true;
                    delete html_cache[url];
                    loading_end();
                    if (config.handler) config.handler(result);
                    promise.deferred.resolve(result);
                };
            loading_start(config.loading);
            if (clear_cache) delete html_cache[url];
            if (html_cache[url]) // if it's in the cache, return promise, break context, then fire success_handler
                return (do_not_cache = true), setTimeout(success_handler.partial(html_cache[url]), INSTANT), promise;
            if (html_cache[url] === null) // if it's null, it means a request is outstanding, stall and return promise
                return setTimeout(api.partial(config, promise), STALL), promise;
            if (!do_not_cache) html_cache[url] = null; // set it to null before making the request
            $.ajax({url: url, dataType: is_data ? 'json' : 'html', success: success_handler, error: error_handler});
            return promise;
        };
    }
});