/**
 * view for timeseries section
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
            search,
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = module.name.split('.').pop(),
            filter_rule_str = '/identifier:?/data_source:?/data_provider:?/data_field:?/observation_time:?',
            check_state = og.views.common.state.check.partial('/' + page_name),
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
                            'OK': function () {
                                $(this).dialog('close');
                                api.rest.timeseries.put({
                                    handler: function (r) {
                                        if (r.error) return ui.dialog({type: 'error', message: r.message});
                                        routes.go(routes.hash(module.rules.load_new_timeseries,
                                                $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
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
                                api.rest.timeseries.del({
                                    handler: function (r) {
                                        if (r.error) return ui.dialog({type: 'error', message: r.message});
                                        ui.dialog({type: 'confirm', action: 'close'});
                                        routes.go(routes.hash(module.rules.load_delete,
                                                $.extend({}, routes.last().args, {deleted: true})
                                        ));
                                    }, id: routes.last().args.id
                                });
                            }
                        }
                    })
                }
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'timeseries',
                    'columns': [
                        {
                            id: 'data_source', toolTip: 'data source',
                            name: '<input type="text" placeholder="data source" '
                                + 'class="og-js-data_source-filter" style="width: 70px;">',
                            field: 'data_source', width: 90, cssClass: 'og-uppercase', filter_type: 'input'
                        },
                        {
                            id: 'identifier', toolTip: 'identifier',
                            name: '<input type="text" placeholder="identifier" '
                                + 'class="og-js-identifier-filter" style="width: 130px;">',
                            field: 'identifier', width: 150, cssClass: 'og-link', filter_type: 'input'
                        },
                        {
                            id: 'data_provider', toolTip: 'data provider',
                            name: '<input type="text" placeholder="data provider" '
                                + 'class="og-js-data_provider-filter" style="width: 65px;">',
                            field: 'data_provider', width: 85, cssClass: 'og-link', filter_type: 'input'
                        },
                        {
                            id: 'data_field', toolTip: 'data field',
                            name: '<input type="text" placeholder="data field" '
                                + 'class="og-js-data_field-filter" style="width: 50px;">',
                            field: 'data_field', width: 70, cssClass: 'og-link', filter_type: 'input'
                        },
                        {
                            id: 'observation_time', toolTip: 'observation time',
                            name: '<input type="text" placeholder="observation time" '
                                + 'class="og-js-observation_time-filter" style="width: 100px;">',
                            field: 'observation_time', width: 120, cssClass: 'og-link', filter_type: 'input'
                        }
                    ]
                },
                toolbar: {
                    'default':  {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    }
                }
            },
            load_timeseries_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: timeseries.load, stop: true}]});
                delete args[field];
                timeseries.search(args);
                routes.go(routes.hash(module.rules.load_timeseries, args));
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    var $html = $.tmpl(template, {
                        name: 'Timeseries',
                        recent_list: history.get_html('history.timeseries.recent') || 'no recently viewed timeseries'
                    });
                    $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                    $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                    og.views.common.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                    ui.toolbar(options.toolbar['default']);
                }});
            },
            details_page = function (args) {
                api.rest.timeseries.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.timeseries_functions,
                            json = result.data;
                        history.put({
                            name: json.template_data.object_id,
                            item: 'history.timeseries.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This position has been deleted\
                                    </section>\
                                ',
                                $html = $.tmpl(template, json.template_data),
                                layout = og.views.common.layout,
                                stop_loading = function () {
                                    ui.message({location: '.ui-layout-inner-center', destroy: true});
                                    layout.inner.resizeAll();
                                };
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                                $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                            } else {
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            f.render_identifiers('.OG-timeseries .og-js-identifiers', json.identifiers);
                            ui.render_plot('.OG-timeseries .og-js-timeseriesPlot', json.timeseries.data);
                            f.render_table('.OG-timeseries .og-js-table', {
                                'fieldLabels': json.timeseries.fieldLabels, 'data': json.timeseries.data
                            }, stop_loading);
                            $('.og-js-timeSeriesCsv').click(function () {
                                window.location.href = '/jax/timeseries/' + args.id + '.csv';
                            });
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            };
        module.rules = {
            load: {route: '/' + page_name + '/:id?' + filter_rule_str, method: module.name + '.load'},
            load_filter: {
                route: '/' + page_name + '/filter:/:id?' + filter_rule_str, method: module.name + '.load_filter'
            },
            load_delete: {
                route: '/' + page_name + '/:id/deleted:' + filter_rule_str, method: module.name + '.load_delete'
            },
            load_timeseries: {
                route: '/' + page_name + '/:id' + filter_rule_str, method: module.name + '.load_' + page_name
            },
            load_new_timeseries: {
                route: '/' + page_name + '/:id/new:' + filter_rule_str, method: module.name + '.load_new_' + page_name
            }
        };
        return timeseries = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        timeseries.search(args);
                        masthead.menu.set_tab(page_name);
                    }}
                ]});
                if (args.id) return;
                default_details_page();
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
                timeseries.search(args);
                routes.go(routes.hash(module.rules.load, {}));
            },
            load_new_timeseries: load_timeseries_without.partial('new'),
            load_timeseries: function (args) {
                check_state({args: args, conditions: [{new_page: timeseries.load}]});
                timeseries.details(args);
            },
            search: function (args) {
                if (!search) search = common.search_results.core();
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});