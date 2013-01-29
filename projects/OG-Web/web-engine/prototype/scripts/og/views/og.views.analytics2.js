/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.analytics2',
    dependencies: ['og.analytics.Form'],
    obj: function () {
        var routes = og.common.routes, module = this, view,
            main_selector = '.OG-layout-analytics-center', form;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
                $('.OG-masthead .og-analytics-beta').addClass('og-active');
                var new_page = false, layout = og.views.common.layout;
                if (!form) form = new og.analytics.Form({selector:'.OG-layout-analytics-masthead .og-form'});
                form.replay_query(og.analytics.url.last.main);
                view.check_state({args: args, conditions: [
                    {new_page: function () {new_page = true; og.analytics.containers.initialize();}}
                ]});
                og.analytics.resize({
                    selector: '.OG-layout-analytics-center',
                    handler: function (right, bottom) {
                        layout.inner.sizePane('south', bottom);
                        layout.main.sizePane('east', right);
                    },
                    context:  function (preset) {
                        var inner = layout.inner.sizePane, right = layout.right.sizePane, main = layout.main.sizePane;
                        switch (preset) {
                            case 1: inner('south', '50%'); main('east', '25%'); break;
                            case 2: inner('south', '50%'); main('east', '50%'); break;
                            case 3: inner('south', '15%'); main('east', '10%'); break;
                        }
                        right('north', '33%');
                        right('south', '33%');
                    }
                });
                if (!new_page && !args.data && og.analytics.url.last.main) {
                    og.analytics.url.clear_main(), $(main_selector).html('');
                    if (!og.analytics.url.last.main) form.reset_query();
                }
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                og.analytics.url.process(args, function () {
                    form.replay_query(og.analytics.url.last.main);
                });
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:data', method: module.name + '.load_item'}
            }
        };
    }
});