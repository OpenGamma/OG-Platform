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
        var routes, set_title = function (title) {document.title = 'OpenGamma: ' + title;},
            hash = window.RouteMap.hash;
        return routes = $.extend(true, window.RouteMap, {
            init: function () {
                var title, go = routes.go;
                // overwrite routes.go so it can accept new title parameter
                routes.go = function (location, new_title) {
                    title = new_title;
                    go(location);
                };
                // listen to all clicks that bubble up and capture their titles
                // overwrite href with new filter values
                $('a[href]').live('click', function (e) {
                    var anchor = $(e.target), current, parsed, rule, page, add, href;
                    title = anchor.attr('title');
                    if (!anchor.is('.og-js-live-anchor')) return;
                    page = (parsed = routes.parse(anchor.attr('href'))).page.slice(1);
                    current = routes.current();
                    if (current.page !== parsed.page) return;
                    if (!og.views[page].filters || !og.views[page].filters.length) return;
                    add = og.views[page].filters.reduce(function (acc, val) {
                        return (acc[val] = current.args[val]), acc;
                    }, {});
                    rule = parsed.args.id ? 'load_item' : 'load';
                    href = routes.prefix() + routes.hash(og.views[page].rules[rule], parsed.args, {add: add});
                    anchor.attr('href', href);
                });
                $(window).bind('hashchange', function () {
                    routes.handler();
                    set_title(title || routes.current().hash);
                    title = null;
                });
                $(function () { // in addition to binding hash change events to window, also fire it onload
                    var common = og.views.common;
                    $('.OG-js-loading').hide();
                    $('.ui-layout-container').show();
                    common.layout = /^.*\/analytics\.ftl$/.test(window.location.href) ? common.layout.analytics()
                        : common.layout['default']();
                    routes.handler();
                    set_title(routes.current().hash);
                });
                // IE does not allow deleting from window so set to void 0 if it fails
                try {delete window.RouteMap;} catch (error) {window.RouteMap = void 0;}
            },
            hash: function (rule, params, extras) {
                var modified_params;
                if (!extras) return hash(rule, params);
                modified_params = $.extend({}, params);
                if (extras.add) $.extend(modified_params, extras.add);
                if (extras.del) extras.del.forEach(function (param) {delete modified_params[param];});
                return hash(rule, modified_params);
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