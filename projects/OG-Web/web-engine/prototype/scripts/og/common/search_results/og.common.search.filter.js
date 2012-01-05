/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Binds routes.go to each search form filter element
 */
$.register_module({
    name: 'og.common.search.filter',
    dependencies: ['og.common.routes'],
    obj: function () {
        var module = this, routes = og.common.routes,
            select = ['type'], // identify select form elements so we can handle these differently
            calendar = ['ob_date'], // calendar fields
            fields = ['name', 'type', 'quantity', 'data_source', 'identifier', 'data_provider', 'data_field',
                'ob_time', 'ob_date', 'status', 'observation_time'];
        return function (obj) { // obj holds a selector with the location of the filters container
            fields.forEach(function (filter) {
                var event_type = ~select.indexOf(filter) || ~calendar.indexOf(filter) ? 'change' : 'keyup',
                    $selector = $(obj.location + ' .og-js-' + filter + '-filter');
                if (!$selector.length) return;
                !!~calendar.indexOf(filter) && $selector.datepicker({firstDay: 1, dateFormat: 'yy-mm-dd'});
                $selector.val(routes.current().args[filter]);
                $selector.unbind(event_type).bind(event_type, function () {
                    var current = routes.current(), view = og.views[current.page.split('/')[1]],
                        hash, args = {filter: true};
                    args[filter] = $(this).val();
                    hash = routes.hash(view.rules.load_filter, current.args, {add: args});
                    clearTimeout(module.t), module.t = setTimeout(function () {routes.go(hash), delete module.t;}, 200);
                });
            });
        }
    }
});