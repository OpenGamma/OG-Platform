/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.state',
    dependencies: ['og.common.routes'],
    obj: function () {
        var routes = og.common.routes;
        return {
            check: function (page, bundle) {
                var self = 'check';
                if (typeof page !== 'string' || !page.length)
                    throw new TypeError(self + ': page must be a non-empty string');
                if (typeof bundle !== 'object') throw new TypeError (self + ': bundle must be an object');
                var last = routes.last() || {}, args = bundle.args, new_page = !routes.last() || last.page !== page;
                // using $.each instead of [].map because you can break out of it
                $.each(bundle.conditions, function (index, val) {
                    var condition = false;
                    if (val.new_page && new_page) (condition = true) && val.new_page(args);
                    if (val.new_value && (new_page || last[val.new_value] !== args[val.new_value]))
                        (condition = true) && val.method(args);
                    if (condition && val.stop) return false;
                });
            }
        };
    }
});