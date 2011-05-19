/**
 * view for securities section
 */
$.register_module({
    name: 'og.views.timeseries',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.masthead.menu',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.layout',
        'og.views.common.state'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search = common.search_results.core(),
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = 'timeseries',
            filter_rule_str = '/identifier:?/data_source:?/data_provider:?/data_field:?/observation_time:?',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            timeseries,
            toolbar_buttons = {
                'new': function () {
                    ui.dialog({
                        type: 'input',
                        title: 'Add New Timeseries',
                        fields: [
                            {type: 'select', name: 'Scheme Type', id: 'scheme',
                                options: [
                                    {name: 'Bloomberg Ticker', value: 'BLOOMBERG_TICKER'},
                                    {name: 'Bloomberg Ticker/Coupon/Maturity', value: 'BLOOMBERG_TCM'},
                                    {name: 'Bloomberg BUID', value: 'BLOOMBERG_BUID'},
                                    {name: 'CUSIP', value: 'CUSIP'},
                                    {name: 'ISIN', value: 'ISIN'},
                                    {name: 'RIC', value: 'RIC'},
                                    {name: 'SEDOL', value: 'CSEDOL1'}
                                ]
                            },
                            {type: 'input', name: 'Data provider', id: 'provider'},
                            {type: 'input', name: 'Data field', id: 'field'},
                            {type: 'input', name: 'Start date(yyyy-mm-dd)', id: 'start'},
                            {type: 'input', name: 'End date(yyyy-mm-dd)', id: 'end'},
                            {type: 'textarea', name: 'Identifiers', id: 'identifiers'}
                        ],
                        buttons: {
                            'Ok': function () {
                                $(this).dialog('close');
                                api.rest.timeseries.put({
                                    handler: function (r) {
                                        routes.go(routes.hash(module.rules.load_new_timeseries,
                                                $.extend({}, routes.last().args, {
                                                    id: r.data.data[0].split('|')[1], 'new': true
                                                })
                                        ));
                                    },
                                    scheme_type: ui.dialog({return_field_value: 'scheme'}),
                                    data_provider: ui.dialog({return_field_value: 'provider'}),
                                    data_field: ui.dialog({return_field_value: 'field'}),
                                    start: ui.dialog({return_field_value: 'start'}) || '',
                                    end: ui.dialog({return_field_value: 'end'}) || '',
                                    identifier: ui.dialog({return_field_value: 'identifiers'})
                                });
                            }
                        }
                    })
                },
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete timeseries?',
                        message: 'Are you sure you want to permanently delete this timeseries?',
                        buttons: {
                            'Delete': function () {
                                $(this).dialog('close');
                                api.rest.timeseries.del({
                                    handler: function () {
                                        window.location.hash = routes.hash(og.views[page_name].rules['load'], {});
                                    }, id: args.id
                                });
                            }
                        }
                    })
                }
            },
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'timeseries',
                    'columns': [
                        {id: 'data_source', name: 'Datasource', field: 'data_source', width: 90, cssClass: 'og-uppercase',
                                filter_type: 'input'},
                        {id: 'identifier', name: 'Identifiers', field: 'identifier', width: 150, cssClass: 'og-link',
                                filter_type: 'input'},
                        {id: 'data_provider', name: 'Data Provider',  field: 'data_provider', width: 85, cssClass: 'og-link',
                                filter_type: 'input'},
                        {id: 'data_field', name: 'Data Field', field: 'data_field', width: 70, cssClass: 'og-link',
                                filter_type: 'input'},
                        {id: 'observation_time', name: 'Observation Time', field: 'observation_time', width: 120,
                                cssClass: 'og-link', filter_type: 'input'}
                    ]
                },
                toolbar: {
                    'default':  {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', enabled: 'OG-disabled'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    },
                    active: {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', handler: 'handler'},
                            {name: 'edit', handler: 'handler'},
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'favorites', handler: 'handler'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    }
                }
            },
            load_timeseries_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: timeseries.load, stop: true}]});
                delete args[field];
                timeseries.search(args);
                routes.go(routes.hash(module.rules.load_timeseries, args));
            },
            default_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Timeseries',
                        favorites_list: history.get_html('history.timeseries.favorites') || 'no favorited timeseries',
                        recent_list: history.get_html('history.timeseries.recent') || 'no recently viewed timeseries',
                        new_list: history.get_html('history.timeseries.new') || 'no new timeseries'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            };
        module.rules = {
            load: {route: '/' + page_name + '/:id?' + filter_rule_str, method: module.name + '.load'},
            load_filter:
                {route: '/' + page_name + '/filter:/:id?' + filter_rule_str, method: module.name + '.load_filter'},
            load_delete:
                {route: '/' + page_name + '/:id/deleted:' + filter_rule_str, method: module.name + '.load_delete'},
            load_timeseries:
                {route: '/' + page_name + '/:id' + filter_rule_str, method: module.name + '.load_' + page_name},
            load_new_timeseries:
                {route: '/' + page_name + '/:id/new:' + filter_rule_str, method: module.name + '.load_new_' + page_name}
        };
        return timeseries = {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                layout('default');
                ui.toolbar(options.toolbar['default']);
                search.load($.extend(options.slickgrid, {url: args}));
                default_page();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        timeseries.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_timeseries, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {
                securities.search(args);
                routes.go(routes.hash(module.rules.load, {}));
            },
            load_new_timeseries: load_timeseries_without.partial('new'),
            load_timeseries: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.last().page !== module.rules.load.route) {
                        masthead.menu.set_tab(page_name);
                        ui.toolbar(options.toolbar.active);
                        search.load($.extend(search_options, {url: args}));
                    } else ui.toolbar(options.toolbar.active);
                } else {
                    masthead.menu.set_tab(page_name);
                    ui.toolbar(options.toolbar.active);
                    search.load($.extend(options.slickgrid, {url: args}));
                }
                // Setup details page
                layout('default');
                api.rest.timeseries.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.timeseries_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.id,
                            item: 'history.timeseries.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var stop_loading = ui.message.partial({location: '#OG-details', destroy: true});
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            // TODO: add in deleted message. Need delete api working first
                            f.render_timeseries_identifiers('.OG-timeseries .og-js-identifiers', details_json.identifiers);
                            ui.render_plot('.OG-timeseries .og-js-timeseriesPlot', details_json.timeseries.data);
                            f.render_timeseries_table('.OG-timeseries .og-js-table', {
                                'fieldLabels': details_json.timeseries.fieldLabels,
                                'data': details_json.timeseries.data
                            }, stop_loading);
                            // Hook up CSV button
                            $('.OG-timeseries .og-js-timeSeriesCsv').click(function () {
                                window.location.href = 'http://localhost:8080/jax/timeseries/Tss::3535.csv';
                            });
                            ui.expand_height_to_window_bottom({element: '.OG-timeseries .og-dataPoints tbody'});
                            ui.expand_height_to_window_bottom({element: '.OG-timeseries .og-dataPoints table'});
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});