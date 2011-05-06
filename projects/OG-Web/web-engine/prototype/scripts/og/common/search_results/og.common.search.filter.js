/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Binds routes.go to each search form filter element
 *
 */
$.register_module({
    name: 'og.common.search.filter',
    dependencies: ['og.common.routes'],
    obj: function () {
        var self = this;
        return function (obj) { // obj holds a selector with the location of the filters container
            var select = ['type']; // identify select form elements so we can handle these differently
            ['name', 'type', 'quantity', 'datasource'].forEach(function (filter) {
                var event_type = !!select.indexOf(filter) ? 'keyup' : 'change',
                    $selector = $(obj.location + ' .og-js-' + filter + '-filter');
                if (!$selector.length) return;
                $selector.unbind(event_type).bind(event_type, function () {
                    var routes = og.common.routes, view = og.views[routes.last().page.substring(1)], hash, obj;
                    obj = {}, obj[filter] = $(this).val(), obj.filter = true;
                    hash = routes.hash(view.rules.load_filter, $.extend(true, {}, routes.last().args, obj));
                    clearTimeout(self.t), self.t = setTimeout(function () {routes.go(hash), delete self.t;}, 200);
                });
            });
        }
    }
});