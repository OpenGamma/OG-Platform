/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.tooltip',
    dependencies: [],
    obj: function () {
        var routes = og.common.routes, module = this, view;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
                og.api.text({module: 'og.views.tooltip', handler: function (template) {
                    $('.OG-layout-tooltip').html(template);
                }});
            },
            load_item: function (args) { view.check_state({args: args, conditions: [{new_page: view.load}]}); },
            init: function () { for (var rule in view.rules) routes.add(view.rules[rule]); },
            rules: { load_item: { route: '/:data?', method: module.name + '.load_item' } }
        }
    }
});