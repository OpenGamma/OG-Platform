/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
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
        'og.views.common.state',
        'og.views.common.default_details'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search, layout,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            filter_rule_str = '/identifier:?/data_source:?/data_provider:?/data_field:?/observation_time:?',
            check_state = og.views.common.state.check.partial('/' + page_name),
            view,
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
                                    handler: function (result) {
                                        var args = routes.current().args, rule = module.rules.load_item;
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        view.search(args);
                                        if (result.meta.id)
                                            return routes.go(routes.hash(rule, args, {add: {id: result.meta.id}}));
                                        routes.go(routes.hash(module.rules.load, args));
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
                                    handler: function (result) {
                                        var args = routes.current().args, rule = module.rules.load;
                                        ui.dialog({type: 'confirm', action: 'close'});
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        routes.go(routes.hash(rule, args));
                                    }, id: routes.current().args.id
                                });
                            }
                        }
                    })
                }
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {
                            id: 'data_source', toolTip: 'data source',
                            name: '<input type="text" placeholder="data source" '
                                + 'class="og-js-data_source-filter" style="width: 70px;">',
                            field: 'data_source', width: 90, cssClass: 'og-uppercase'
                        },
                        {
                            id: 'identifier', toolTip: 'identifier',
                            name: '<input type="text" placeholder="identifier" '
                                + 'class="og-js-identifier-filter" style="width: 130px;">',
                            field: 'identifier', width: 150, cssClass: 'og-link'
                        },
                        {
                            id: 'data_provider', toolTip: 'data provider',
                            name: '<input type="text" placeholder="data provider" '
                                + 'class="og-js-data_provider-filter" style="width: 65px;">',
                            field: 'data_provider', width: 85, cssClass: 'og-link'
                        },
                        {
                            id: 'data_field', toolTip: 'data field',
                            name: '<input type="text" placeholder="data field" '
                                + 'class="og-js-data_field-filter" style="width: 50px;">',
                            field: 'data_field', width: 70, cssClass: 'og-link'
                        },
                        {
                            id: 'observation_time', toolTip: 'observation time',
                            name: '<input type="text" placeholder="observation time" '
                                + 'class="og-js-observation_time-filter" style="width: 100px;">',
                            field: 'observation_time', width: 120, cssClass: 'og-link'
                        }
                    ]
                },
                toolbar: {
                    'default':  {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', handler: toolbar_buttons['delete']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            default_details = og.views.common.default_details.partial(page_name, 'Time Series', options),
            details_page = function (args) {
                layout.inner.options.south.onclose = null;
                layout.inner.close('south');
                api.rest.timeseries.get({
                    dependencies: ['id'],
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data;
                        api.text({module: module.name, handler: function (template) {
                            var $html, error_html, json_id = json.identifiers, title, header, content;
                            error_html = '\
                                <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                    This time series has been deleted\
                                </section>\
                            ';
                            // check if any of the following scheme types are in json.identifiers, in
                            // reverse order of preference, delimiting if multiple, then assign to title
                            ['ISIN', 'SEDOL1', 'CUSIP', 'BLOOMBERG_BUID', 'BLOOMBERG_TICKER'].forEach(function (val) {
                                title = (function (type) {
                                    return json.identifiers.reduce(function (acc, val) {
                                        if (val.scheme === type) acc = acc
                                            ? acc + ', ' + val.value
                                            : type.lang() + ' - ' + val.value;
                                        return acc
                                    }, '')
                                }(val)) || title;
                            });
                            $html = $.tmpl(template, $.extend(json.template_data, {
                                title: title || json.template_data.object_id
                            }));
                            history.put({
                                name: title + ' (' + json.template_data.data_field + ')',
                                item: 'history.' + page_name + '.recent',
                                value: routes.current().hash
                            });
                            // Initial html setup
                            header = $.outer($html.find('> header')[0]);
                            content = $.outer($html.find('> section')[0]);
                            $('.ui-layout-inner-center .ui-layout-header').html(header);
                            $('.ui-layout-inner-center .ui-layout-content').html(content);
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                                $('.OG-tools .og-js-delete').addClass('OG-disabled').unbind();
                            } else {
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            // Identifiers
                            $('.ui-layout-inner-center .og-js-identifiers').html(
                                json_id.reduce(function (acc, cur) {
                                    return acc + '<tr><td><span>'+  cur.scheme +'<span></td><td>'+ cur.value +'</td></tr>'
                                }, '')
                            );
                            // Plot
                            common.gadgets.timeseries({
                                selector: '.OG-timeseries .og-plots',
                                id: result.data.template_data.object_id
                            });
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            setTimeout(layout.inner.resizeAll);
                        }});
                    },
                    id: args.id,
                    cache_for: 10000,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            };
        module.rules = {
            load: {route: '/' + page_name + '/' + filter_rule_str, method: module.name + '.load'},
            load_filter: {
                route: '/' + page_name + '/filter:/:id?' + filter_rule_str, method: module.name + '.load_filter'
            },
            load_item: {route: '/' + page_name + '/:id' + filter_rule_str, method: module.name + '.load_item'}
        };
        return view = {
            load: function (args) {
                layout = og.views.common.layout;
                check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), masthead.menu.set_tab(page_name);}}
                ]});
                if (!args.id) default_details();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                search.filter(args);
            },
            load_item: function (args) {
                check_state({args: args, conditions: [{new_page: view.load}]});
                view.details(args);
            },
            search: function (args) {
                if (!search) search = common.search_results.core();
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});
