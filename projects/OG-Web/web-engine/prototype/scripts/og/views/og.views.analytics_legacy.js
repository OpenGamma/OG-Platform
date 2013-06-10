/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.analytics_legacy',
    dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.common.routes'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, view,
            layout = og.views.common.layout, masthead = og.common.masthead,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name);
        module.rules = {load: {route: '/:id?', method: module.name + '.load'}};
        return view = {
            load: function (args) {
                if ($('#temp_analytics_frame').length) return; // do not reload page
                var id = args.id ? '?' + args.id : '';
                masthead.menu.set_tab(page_name);
                $('.ui-layout-center').html('<iframe id="temp_analytics_frame" src="/analytics/' + id + '"\
                    style="position: absolute; top: 3px; height: 99%; width: 100%;"></iframe>');
            },
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});