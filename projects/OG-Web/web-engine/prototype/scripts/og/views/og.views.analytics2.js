/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.views.common.state', 'og.common.routes'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, view,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name);
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            load: function (args) {
                og.analytics.layout_manager();
                // TODO THIS IS A GLOBAL, REMOVE IT LATER
                grid = new og.analytics.Grid({selector: '.OG-layout-analytics-center'});
            },
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});