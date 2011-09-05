/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * maps URL hash routes
 */
$.register_module({
    name: 'og.common.routes',
    dependencies: ['og.dev'],
    obj: function () {
        var routes, set_title = function (title) {document.title = 'OpenGamma: ' + title;};
        return routes = $.extend(true, window.RouteMap, {
            init: function () {
                var title, go = routes.go;
                // overwrite routes.go so it can accept new title parameter
                routes.go = function (location, new_title) {
                    title = new_title;
                    go(location);
                };
                // listen to all clicks that bubble up and capture their titles
                $('a[href]').live('click', function (e) {title = $(e.target).attr('title');});
                $(window).bind('hashchange', function () {
                    routes.handler();
                    set_title(title || routes.current().hash);
                    title = null;
                });
                $(function () { // in addition to binding hash change events to window, also fire it onload
                    $('.OG-js-loading').hide();
                    $('.ui-layout-container').show();
                    og.views.common.layout = /^.*\/analytics\.ftl$/.test(window.location.href)
                        ? og.views.common.layout.analytics()
                        : og.views.common.layout['default']();
                    routes.handler();
                    set_title(routes.current().hash);
                });
                // IE does not allow deleting from window so set to void 0 if it fails
                try {delete window.RouteMap;} catch (error) {window.RouteMap = void 0;}
            },
            post_add: function (compiled) { // add optional debug param to all rules that don't ask for it
                if (!~compiled.rules.keyvals.map(function (val) {return val.name;}).indexOf('debug'))
                    compiled.rules.keyvals.push({name: 'debug', required: false});
                return compiled;
            },
            pre_dispatch: function (parsed) { // if the debug param exists, use it to set API debug mode
                if (parsed.length && parsed[0].args.debug) og.dev.debug = parsed[0].args.debug === 'true';
                og.api.rest.clean();
                return parsed;
            }
        });
    }
});