/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: ['og.analytics.Form', 'og.common.routes', 'og.common.masthead'],
    obj: function () {
        var module = this, masthead = og.common.masthead, page_name = module.name.split('.').pop(),
            routes = og.common.routes, main_selector = '.OG-layout-analytics-center', view, form;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
//                masthead.menu.set_tab(page_name);
                if (!form) form = new og.analytics.Form({selector:'.OG-layout-blotter-masthead .og-form'});
                $('.new_trade').css({display: 'inline-block'}).click(function (){new og.blotter.Dialog();});
            },
            load_item: function (args) {},
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:data', method: module.name + '.load_item'}
            }
        };
    }
});