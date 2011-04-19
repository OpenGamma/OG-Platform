/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.search.filter',
    dependencies: [],
    obj: function () {
        return function (obj) {
            /**
             * name filter
             */
            $(obj.location + ' .og-js-name-filter').unbind('keyup').bind('keyup', function () {
                var routes = og.common.routes,
                      last = routes.last(),
                      name = last.page.substring(1),
                      view = og.views[name],
                       obj = $.extend(true, {}, routes.last().args, {name: $(this).val(), filter: true}),
                      hash = routes.hash(view.rules.load_filter, obj);
                clearTimeout(window.og_filter_timer);
                og_filter_timer = setTimeout(function () {
                    routes.go(hash);
                    delete window.og_filter_timer;
                }, 200);
            });
            /**
             * filter_type filter
             */
            $(obj.location + ' .og-js-type-filter').unbind('change').bind('change', function () {
                var routes = og.common.routes,
                      last = routes.last(),
                      name = last.page.substring(1),
                      view = og.views[name],
                       obj = $.extend(true, {}, routes.last().args, {filter_type: $(this).val(), filter: true}),
                      hash = routes.hash(view.rules.load_filter, obj);
                    routes.go(hash);
            });
            /**
             * quantity filter
             */
            $(obj.location + ' .og-js-quantity-filter').unbind('keyup').bind('keyup', function () {
                var routes = og.common.routes,
                      last = routes.last(),
                      name = last.page.substring(1),
                      view = og.views[name],
                       obj = $.extend(true, {}, routes.last().args, {quantity: $(this).val(), filter: true}),
                      hash = routes.hash(view.rules.load_filter, obj);
                clearTimeout(window.og_filter_timer);
                og_filter_timer = setTimeout(function () {
                    routes.go(hash);
                    delete window.og_filter_timer;
                }, 200);
            });
            /**
             * datasource filter
             */
            $(obj.location + ' .og-js-datasource-filter').unbind('keyup').bind('keyup', function () {
                var routes = og.common.routes,
                      last = routes.last(),
                      name = last.page.substring(1),
                      view = og.views[name],
                       obj = $.extend(true, {}, routes.last().args, {dataSource: $(this).val(), filter: true}),
                      hash = routes.hash(view.rules.load_filter, obj);
                clearTimeout(window.og_filter_timer);
                og_filter_timer = setTimeout(function () {
                    routes.go(hash);
                    delete window.og_filter_timer;
                }, 200);
            });

        }
    }
});