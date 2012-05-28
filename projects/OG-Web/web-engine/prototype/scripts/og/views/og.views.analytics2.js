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
            load: function (args) {if (!args.id) view.default_details();},
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                var convert_args = function (str) {
                        if (!str) return;
                        return str.split(';').reduce(function (acc, val) {
                            var data = val.split(':');
                            acc[data[0]] = data[1].split(',');
                            return acc;
                        }, {});
                    },
                    init_panel = function (name, obj) {
                        var selector = '.OG-layout-analytics-' + name, type, gadgets = [], options = {};
                        options.t = function (id) {
                            return {
                                gadget: 'og.common.gadgets.timeseries',
                                options: {id: id, datapoints_link: false, child: true},
                                name: 'Timeseries ' + id,
                                margin: true
                            }
                        };
                        options.g = function (id) {
                            return {gadget: 'og.analytics.Grid', name: 'grid ' + id, options: {}}
                        };
                        if (!obj) return new GadgetsContainer(selector).init();
                        else {
                            for (type in obj) {
                                obj[type].forEach(function (val) {
                                    gadgets.push(options[type](val));
                                });
                            }
                            new GadgetsContainer(selector).add(gadgets);
                        }
                    };
                new og.analytics.Grid({selector: '.OG-layout-analytics-center'});
                init_panel('south', convert_args(args[1]));
                init_panel('dock-north', convert_args(args[2]));
                init_panel('dock-center', convert_args(args[3]));
                init_panel('dock-south', convert_args(args[4]));
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:id/1:?/2:?/3:?/4:?', method: module.name + '.load_item'}
            }
        };
    }
});