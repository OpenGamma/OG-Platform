/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.views.common.state', 'og.common.routes'],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, view;
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            default_details: function (args) {
                og.api.text({module: 'og.analytics.grid.configure_tash'}).pipe(function (markup) {
                    var template = Handlebars.compile(markup);
                    $('.OG-layout-analytics-center').html(template({}));
                });
            },
            load: function (args) {
                view.check_state({args: args, conditions: [{new_page: og.analytics.layout_manager}]});
                if (!args.id) view.default_details(args);
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                $('.OG-layout-analytics-center')
                    .html('<iframe width="100%" height="100%" src="gadget.ftl#/grid/foo/frame=true"></iframe>');
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:id', method: module.name + '.load_item'}
            }
        };
    }
});