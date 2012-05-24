/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.views.common.state', 'og.common.routes', 'og.common.gadgets.GadgetsContainer'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, view,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            GadgetsContainer = og.common.gadgets.GadgetsContainer,
            count = 0, counter = function () {return ('100' + count++);},
            gc_south, gc_r_north, gc_r_center, gc_r_south;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            default_details: function (args) {
                og.api.text({module: 'og.analytics.grid.configure_tash'}).pipe(function (markup) {
                    var template = Handlebars.compile(markup);
                    $('.OG-layout-analytics-center').html(template({}));
                });
            },
            load: function (args) {
                var timeseries_obj = function () {
                    var num = counter();
                    return {
                        gadget: 'og.common.gadgets.timeseries',
                        options: {id: 'DbHts~' + num, datapoints_link: false, child: true},
                        name: 'Timeseries ' + num + ' long name etc, etc...',
                        margin: true
                    }
                };
                gc_south = (new GadgetsContainer('.OG-layout-analytics-south')).add([
                    timeseries_obj(), {gadget: 'og.analytics.Grid', name: 'grid down south', options: {}}
                ]);
                gc_r_north = (new GadgetsContainer('.OG-layout-analytics-dock-north')).add([timeseries_obj()]);
                gc_r_center = (new GadgetsContainer('.OG-layout-analytics-dock-center')).add([
                    timeseries_obj(), timeseries_obj(), timeseries_obj(), timeseries_obj(), timeseries_obj(),
                    timeseries_obj(), timeseries_obj(), timeseries_obj()
                ]);
                gc_r_south = (new GadgetsContainer('.OG-layout-analytics-dock-south').init());
                if (!args.id) view.default_details();
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                new og.analytics.Grid({selector: '.OG-layout-analytics-center'});
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:id', method: module.name + '.load_item'}
            }
        };
    }
});
