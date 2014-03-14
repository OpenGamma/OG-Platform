/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * maps URL hash routes
 */
$.register_module({
    name: 'og.common.routes',
    dependencies: ['og.dev'],
    obj: function () {
        var routes, hash = window.RouteMap.hash, hashchange = false,
            SL = '/', MOZSLASH = 'MOZSLOG', MOZSLASH_EXP = new RegExp(MOZSLASH, 'g'), ENCODEDSL = '%2F';
        var slash_replace = function (obj, acc, val) {return acc[val] = ('' + obj[val]).replace(/\//g, MOZSLASH), acc;};
        return routes = $.extend(true, window.RouteMap, {
            get: function () {
                var hash = window.location.hash.replace(MOZSLASH_EXP, ENCODEDSL), index = hash.indexOf(SL);
                return ~index ? hash.slice(index) : SL;
            },
            suppress_hashchange: function (suppress) {
                suppresshashchange = suppress;
            },
            init: function () {
                var go = routes.go;
                // overwrite routes.go so it can accept new title parameter
                routes.go = function (location, title) {
                    routes.title = title;
                    go(location);
                };
                // listen to all clicks that bubble up and capture their titles
                // overwrite href with new filter values
                $('a[href]').live('click', function (e) {
                    var $anchor = $(this), current, parsed, rule, view, href;
                    routes.title = $anchor.attr('title');
                    if (!$anchor.is('.og-js-live-anchor')) return;
                    view = og.views[(parsed = routes.parse($anchor.attr('href'))).page.slice(1)];
                    if (!view.filter_params.length || (current = routes.current()).page !== parsed.page) return;
                    rule = parsed.args.id ? 'load_item' : 'load';
                    href = routes.prefix() + routes.hash(view.rules[rule], parsed.args, {
                        add: view.filter_params
                            .reduce(function (acc, val) {return (acc[val] = current.args[val]), acc;}, {})
                    });
                    $anchor.attr('href', href);
                });
                $(window).on('hashchange', function () {
                    if (hashchange) return hashchange = false;
                    if (og.common.events.fire('hashchange') === false)
                        return hashchange = true, routes.go(routes.last().hash);
                    routes.handler();
                    routes.set_title(routes.title || routes.current().hash);
                    routes.title = null;
                });
                $(window).on('beforeunload', function (event) {
                    return og.common.events.fire('unload') ? void 0 : 'You have unsaved changes';
                });
                $(window).on('keydown', function (event) {
                    if (event.keyCode !== $.ui.keyCode.ESCAPE) return;
                    event.preventDefault(); // escape key will break long-polling, so prevent the default action
                    if ($('.OG-cell-options.og-frozen').remove().length) // remove any inplace gadgets and clean up
                        $('.og-inplace-resizer').remove(), og.common.gadgets.manager.clean();
                });
                $(function () { // in addition to binding hash change events to window, also fire it onload
                    var common = og.views.common, is_child, opener_og, parent_api, parent_data, api = og.api.rest;
                    $('.OG-js-loading').hide();
                    $('.OG-layout-admin-container, .OG-layout-analytics-container, .OG-layout-blotter-container')
                        .css({'visibility': 'visible'});
                    common.layout = (({
                        'analytics_legacy.ftl': common.layout.analytics_legacy,
                        'analytics.ftl': common.layout.analytics,
                        'blotter.ftl': common.layout.blotter,
                        'gadget.ftl': common.layout.gadget,
                        'admin.ftl': common.layout.admin
                    })[window.location.pathname.split('/').reverse()[0].toLowerCase()] || $.noop)();
                    // check if the parent's document is the same as the window's (instead of just comparing
                    // window.parent to window, we use document because IE8 doesn't know true from false)

		    is_child = (window.parent.document !== window.document) ||
			window.opener;
		    // try-catch so that permission denied error is caught
		    // if opengamma is called from external web page.
		    try {
			parent_api = ((opener_og = window.opener &&
				       window.opener.og) &&
				      window.opener.og.api.rest) ||
                            window.parent.og.api.rest;
			parent_data = (opener_og &&
				       window.opener.og.analytics &&
				       window.opener.og.analytics.Data) ||
                            window.parent.og.analytics &&
			    window.parent.og.analytics.Data;
		    } catch (e) {
			    parent_api = null;
			    parent_data = null;
		    }

		    if (is_child && parent_api)
                        (og.api.rest = parent_api).on('abandon',
						      function () {
							  document.location.reload();});
		    else if (og.api.rest) api.subscribe();
		    // the admin pages do not need og.analytics
		    if (is_child && parent_data && og.analytics)
                        og.analytics.Data = parent_data;
                    routes.set_title(routes.title || (routes.get() || ''));
                    routes.handler();
                });
                // IE does not allow deleting from window so set to void 0 if it fails
                try {delete window.RouteMap;} catch (error) {window.RouteMap = void 0;}
            },
            hash: function (rule, params, extras) {
                var modified_params = Object.keys(params).reduce(slash_replace.partial(params), {});
                if (extras && extras.add)
                    $.extend(modified_params, Object.keys(extras.add).reduce(slash_replace.partial(extras.add), {}));
                if (extras && extras.del) extras.del.forEach(function (param) {delete modified_params[param];});
                return hash(rule, modified_params);
            },
            post_add: function (compiled) { // add optional debug param to all rules that don't ask for it
                if (!~compiled.rules.keyvals.pluck('name').indexOf('debug'))
                    compiled.rules.keyvals.push({name: 'debug', required: false});
                return compiled;
            },
            pre_dispatch: function (parsed) { // if the debug param exists, use it to set API debug mode
                if (parsed.length && parsed[0].args.debug) og.dev.debug = parsed[0].args.debug === 'true';
                if (og.api.rest) og.api.rest.clean();
                return parsed;
            },
            set_title: function (title) {document.title = title;},
            title: null
        });
    }
});
