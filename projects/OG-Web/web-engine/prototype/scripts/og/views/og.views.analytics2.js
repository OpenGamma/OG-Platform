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
                og.api.text({
                    module: 'og.views.analytics.grid', handler: function (result) {
                        $('.OG-layout-analytics-center').html(result);
                    }
                });
            },
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});