/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * API call for making and caching static requests
 */
$.register_module({
    name: 'og.api.text',
    dependencies: ['og.api.common'],
    obj: function () {
        var html_cache = {}, module = this, api,
            start_loading = og.api.common.start_loading, end_loading = og.api.common.end_loading;
            path = function (root, extension, page) {
                return !page ? null : root + [page.split('.').slice(1, -1).join('/'), page.toLowerCase() + extension]
                    .join('/').replace(/\s/g, '_');
            },
            html_path = path.partial(module.html_root, '.html'),
            data_path = path.partial(module.data_root, '.json');
        return api = function (config) {
            if (typeof config === 'undefined') throw new TypeError('static: config is undefined');
            if (typeof config.handler !== 'function') throw new TypeError('static: config.handler must be a function');
            if (typeof config.url !== 'string' && typeof config.module !== 'string' && typeof config.data !== 'string')
                throw new TypeError('static: config.data, config.url, or config.module must be a string');
            var do_not_cache = config.do_not_cache, clear_cache = config.clear_cache, is_data,
                url = (config.url || html_path(config.module) || (is_data = data_path(config.data))),
                success_handler = function (response) {
                    if (!do_not_cache) html_cache[url] = response;
                    end_loading();
                    config.handler(response, false);
                },
                error_handler = function (response) {
                    delete html_cache[url];
                    end_loading();
                    config.handler('Error (HTTP ' + response.status + ') retrieving: ' + url, true);
                };
            start_loading(config.loading);
            if (clear_cache) delete html_cache[url];
            if (html_cache[url]) return (do_not_cache = true), success_handler(html_cache[url]);
            if (html_cache[url] === null) // if it's null, it means a request is outstanding
                return setTimeout(api.partial(config), 500);
            if (!do_not_cache) html_cache[url] = null; // set it to null before making the request
            $.ajax({url: url, dataType: is_data ? 'json' : 'html', success: success_handler, error: error_handler});
        };
    }
});