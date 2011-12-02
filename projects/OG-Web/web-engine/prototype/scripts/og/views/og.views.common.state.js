/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.state',
    dependencies: ['og.common.routes'],
    obj: function () {
        var routes = og.common.routes;
        return {
            check: function (page, bundle) {
                var self = 'check', lcv, condition, value;
                if (typeof page !== 'string' || !page.length)
                    throw new TypeError(self + ': page must be a non-empty string');
                if (typeof bundle !== 'object') throw new TypeError (self + ': bundle must be an object');
                var last = routes.last() || {}, args = bundle.args,
                    /* /configs/new is the *same* page as /configs for our purposes */
                    new_page = !routes.last() || last.page.split('/')[1] !== page.split('/')[1];
                for (lcv = 0; lcv < bundle.conditions.length; lcv += 1) {
                    condition = false, value = bundle.conditions[lcv];
                    if (value.new_page && new_page) (condition = true), value.new_page(args);
                    if (value.new_value && (new_page || last.args[value.new_value] !== args[value.new_value]))
                        (condition = true), value.method(args);
                    if (value.condition) (condition = true), value.method(args);
                    if (condition && value.stop) break;
                }
            }
        };
    }
});