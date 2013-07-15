/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.positions',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.layout',
        'og.views.common.versions'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            routes = common.routes,
            search, layout,
            ui = common.util.ui,
            module = this, view, details_page,
            page_name = module.name.split('.').pop(),
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add New Position',
                    width: 400, height: 300,
                    fields: [
                        {type: 'input', name: 'Quantity', id: 'quantity'},
                        {type: 'select', name: 'Scheme Type', id: 'scheme-type',
                                options: [
                                    {name: 'Bloomberg Ticker', value: 'BLOOMBERG_TICKER'},
                                    {name: 'Bloomberg BUID', value: 'BLOOMBERG_BUID'},
                                    {name: 'CUSIP', value: 'CUSIP'},
                                    {name: 'ISIN', value: 'ISIN'},
                                    {name: 'RIC', value: 'RIC'},
                                    {name: 'SEDOL', value: 'CSEDOL1'}
                                ]
                        },
                        {type: 'input', name: 'Identifier', id: 'identifier'}
                    ],
                    buttons: {
                        'OK': function () {
                            api.rest.positions.put({
                                handler: function (result) {
                                    var args = routes.current().args, rule = view.rules.load_item;
                                    if (result.error) return view.error(result.message);
                                    ui.dialog({type: 'input', action: 'close'});
                                    view.search(args);
                                    routes.go(routes.hash(rule, args, {add: {id: result.meta.id}, del: ['version']}));
                                },
                                quantity: ui.dialog({return_field_value: 'quantity'}),
                                scheme_type: ui.dialog({return_field_value: 'scheme-type'}),
                                identifier: ui.dialog({return_field_value: 'identifier'})
                            });
                        },
                        'Cancel': function () {$(this).dialog('close');}
                    }
                })},
                'import': og.views.common.toolbar.upload,
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete Position?',
                    width: 400, height: 190,
                    message: 'Are you sure you want to permanently delete this position?',
                    buttons: {
                        'Delete': function () {
                            api.rest.positions.del({
                                handler: function (result) {
                                    var args = routes.current().args;
                                    ui.dialog({type: 'confirm', action: 'close'});
                                    if (result.error) return view.error(result.message);
                                    routes.go(routes.hash(view.rules.load, args));
                                    setTimeout(function () {view.search(args);});
                                }, id: routes.current().args.id
                            });
                        },
                        'Cancel': function () {$(this).dialog('close');}
                    }
                })},
                'versions': function () {
                    var rule = view.rules.load_item, args = routes.current().args;
                    routes.go(routes.prefix() + routes.hash(rule, args, {add: {version: '*'}}));
                    if (!view.layout.inner.state.south.isClosed && args.version) {
                        view.layout.inner.close('south');
                    } else view.layout.inner.open('south');
                    view.layout.inner.options.south.onclose = function () {
                        routes.go(routes.hash(rule, args, {del: ['version']}));
                    };
                }
            };
        details_page = function (args, config) {
            var show_loading = !(config || {}).hide_loading, rest_options;
            // load versions
            if (args.version) {
                view.layout.inner.open('south');
                og.views.common.versions.load();
            } else view.layout.inner.close('south');
            rest_options = {
                id: args.id,
                cache_for: 500,
                version: args.version && args.version !== '*' ? args.version : void 0,
                loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});},
                dependencies: view.dependencies,
                update: view.update
            };
            $.when(api.rest.positions.get(rest_options), api.text({module: module.name}))
                .then(function (result, template) {
                    if (result.error) return view.notify(null), view.error(result.message);
                    var json = result.data, error_html = '\
                            <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                This position has been deleted\
                            </section>\
                        ', $html = $.tmpl(template, json.template_data);
                    history.put({
                        name: json.template_data.name,
                        item: 'history.' + page_name + '.recent',
                        value: routes.current().hash
                    });
                    $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                    $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                    ui.toolbar(view.options.toolbar.active);
                    if (json.template_data && json.template_data.deleted) {
                        $('.OG-layout-admin-details-north').html(error_html);
                        view.layout.inner.sizePane('north', '0');
                        view.layout.inner.open('north');
                        $('.OG-tools .og-js-delete').addClass('OG-disabled').unbind();
                    } else {
                        view.layout.inner.close('north');
                        $('.OG-layout-admin-details-north').empty();
                    }
                    common.gadgets.positions({
                        id: args.id, selector: '.og-js-gadgets-positions', view: view,
                        version: args.version, editable: args.version && args.version !== '*' ? false : true
                    });
                    common.gadgets.trades({
                        id: args.id, version: args.version, selector: '.og-js-trades-table'
                    });
                    if (show_loading) view.notify(null);
                    setTimeout(view.layout.inner.resizeAll);
                });
        };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            details: details_page,
            options: {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name'},
                        {id: 'quantity',
                            name: '<input type="text" '
                                + 'placeholder="Quantity" '
                                + 'class="og-js-quantity-filter" '
                                + 'style="width: 80px;">',
                            field: 'quantity', width: 100, toolTip: 'quantity'},
                        {id: 'trades', name: 'Trades', field: 'trades', width: 60, toolTip: 'trades'}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', divider: true, handler: toolbar_buttons['delete']},
                            {id: 'versions', label: 'versions', handler: toolbar_buttons['versions']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: view.rules(['quantity'], ['version'])
        });
    }
});