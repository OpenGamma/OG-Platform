/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.analytics.form'],
    obj: function () {
        var routes = og.common.routes, module = this, view, default_template, default_object,
            main_selector = '.OG-layout-analytics-center', form = og.analytics.form('.OG-layout-analytics-masthead');
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            default_details: function () {
                if (og.analytics.url.last.main) form.replay_query(og.analytics.url.last.main);
                og.analytics.containers.initialize();
                og.api.text({module: 'og.analytics.grid.configure_tash'}).pipe(function (markup) {
                    default_template = Handlebars.compile(markup);
                    $(main_selector).html(default_template(default_object));
                });
            },
            load: function (args) {
                console.log('load');
                var new_page = false;
                view.check_state({args: args, conditions: [
                    {new_page: function () {new_page = true; view.default_details();}}
                ]});
                og.analytics.resize();
                if (!new_page && !args.data && og.analytics.url.last.main) {
                    form.replay_query(og.analytics.url.last.main);
                    og.analytics.url.clear_main(), $(main_selector).html(default_template(default_object));
                }
            },
            load_item: function (args) {
                console.log('load_item');
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                og.analytics.url.process(args, function () {
                    if (og.analytics.url.last.main) form.replay_query(og.analytics.url.last.main);
                });
                og.analytics.resize();
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:data', method: module.name + '.load_item'}
            }
        };
    }
});