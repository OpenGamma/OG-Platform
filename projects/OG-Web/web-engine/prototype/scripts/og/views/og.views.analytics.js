/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.analytics',
    dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.api.rest', 'og.common.routes'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, analytics,
            layout = og.views.common.layout, masthead = og.common.masthead, mode_handler,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name);
        module.rules = {
            load: {route: '/', method: module.name + '.load'},
            load_view: {route: '/' + '/:view', method: module.name + '.load_view'},
            load_view_mode: {route: '/' + '/:view/:mode', method: module.name + '.load_view_mode'}
        };
        mode_handler = {
            portfolio: function (view, structure) {
                console.log('mode_handler[portfolio]', structure);
            },
            primitives: function (view, structure) {
                console.log('mode_handler[primitives]', structure);
            }
        };
        return analytics = {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('.ui-layout-center').html('<iframe id="temp_analytics_frame" src="/analytics/"\
                    style="position: absolute; top: 3px; height: 99%; width: 100%;"></iframe>');
            },
            load_view: function (args) {
                check_state({args: args, conditions: [{new_page: analytics.load}]});
            },
            load_view_mode: function (args) {
                check_state({args: args, conditions: [{new_page: analytics.load}]});
            },
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});