/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.views.common.state', 'og.common.routes', 'og.common.gadgets.GadgetsContainer'],
    obj: function () {
        var routes = og.common.routes, module = this, view,
            GadgetsContainer = og.common.gadgets.GadgetsContainer;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            default_details: function () {
                og.api.text({module: 'og.analytics.grid.configure_tash'}).pipe(function (markup) {
                    var template = Handlebars.compile(markup);
                    $('.OG-layout-analytics-center').html(template({}));
                });
            },
            load: function (args) {
                if (!args.id) {
                    view.default_details();
                    og.analytics.resize();
                    og.analytics.form('.OG-layout-analytics-masthead');
                }
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                // TODO: remove global
                grid = new og.analytics.Grid({
                   selector: '.OG-layout-analytics-center',
                   source: {
                       type: 'portfolio',
                       depgraph: false,
                       viewdefinition: args.id,
                       live: true,
                       provider: 'Live market data (Bloomberg, Activ, TullettPrebon, ICAP)'
                   }
                });
                grid.on('cellselect', function (cell) {
                    if (cell.type === 'LABELLED_MATRIX_1D') {
                        routes.go(routes.hash(view.rules.load_item, routes.current().args, {
                            add: {south: 'data:' + cell.col + '|' + cell.row}
                        }));
                    }
                });
                ['south', 'dock-north', 'dock-center', 'dock-south'].forEach(function (val) {
                    new GadgetsContainer('.OG-layout-analytics-', val).add(args[val]);
                });
                og.analytics.form2('.OG-layout-analytics-masthead');
                og.analytics.resize();
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:id/south:?/dock-north:?/dock-center:?/dock-south:?',
                    method: module.name + '.load_item'}
            }
        };
    }
});