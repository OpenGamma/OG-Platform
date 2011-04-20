/**
 * maps URL hash routes
 */
$.register_module({
    name: 'og.common.routes',
    dependencies: ['og.dev'],
    obj: function () {
        return $.extend(true, window.RouteMap, {
            init: function () {
                $(window).bind('hashchange', this.handler);
                $(this.handler); // in addition to binding hash change events to window, also fire it onload
                // IE does not allow deleting from window so set to void 0 if it fails
                try{delete window.RouteMap;}catch(error){window.RouteMap = void 0;};
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