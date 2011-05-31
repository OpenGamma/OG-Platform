/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.analytics',
    dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.api.rest', 'og.common.routes'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, analytics,
            layout = og.views.common.layout, masthead = og.common.masthead, mode_handler, page_name = 'analytics',
            check_state = og.views.common.state.check.partial('/' + page_name);
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_view: {route: '/' + page_name + '/:view', method: module.name + '.load_view'},
            load_view_mode: {route: '/' + page_name + '/:view/:mode', method: module.name + '.load_view_mode'}
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
                layout('analytics');
                $('#OG-analytics .OG-toolbar').hide();
                $('#OG-analytics .og-main').html('<iframe id="temp_analytics_frame" ' +
                        'src="/analytics/" width="100%"></iframe>');
                og.common.util.ui.expand_height_to_window_bottom({element: '#temp_analytics_frame'});
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