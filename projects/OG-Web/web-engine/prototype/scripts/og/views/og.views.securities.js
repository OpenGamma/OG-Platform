/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.securities',
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
        'og.views.common.versions',
        'og.views.common.state'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes, search,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            view,
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add Securities',
                    width: 400, height: 270,
                    fields: [
                        {type: 'select', name: 'Scheme Type', id: 'scheme-type',
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
                        {type: 'textarea', name: 'Identifiers', id: 'identifiers'}
                    ],
                    buttons: {
                        'OK': function () {
                            $(this).dialog('close');
                            api.rest.securities.put({
                                handler: function (result) {
                                    var args = routes.current().args;
                                    if (result.error) return view.error(result.message);
                                    view.search(args);
                                    if (result.data.data.length !== 1)
                                        return routes.go(routes.hash(view.rules.load, args));
                                    routes.go(routes.hash(view.rules.load_item, args, {
                                        add: {id: result.data.data[0].split('|')[1]},
                                        del: ['version']
                                    }));
                                },
                                scheme_type: ui.dialog({return_field_value: 'scheme-type'}),
                                identifier: ui.dialog({return_field_value: 'identifiers'})
                            });
                        },
                        'Cancel': function () {$(this).dialog('close');}
                    }
                })},
                'import': og.views.common.toolbar.upload,
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete Security?',
                    width: 400, height: 190,
                    message: 'Are you sure you want to permanently delete this security?',
                    buttons: {
                        'Delete': function () {
                            $(this).dialog('close');
                            api.rest.securities.del({
                                id: routes.current().args.id,
                                handler: function (result) {
                                    var args = routes.current().args;
                                    if (result.error) return view.error(result.message);
                                    routes.go(routes.hash(view.rules.load, args));
                                    setTimeout(function () {view.search(args);});
                                }
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
            },
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading, rest_options, render_page;
                // load versions
                if (args.version) {
                    view.layout.inner.open('south');
                    og.views.common.versions.load();
                } else view.layout.inner.close('south');
                rest_options = {
                    dependencies: view.dependencies,
                    update: view.update,
                    id: args.id,
                    version: args.version && args.version !== '*' ? args.version : void 0,
                    cache_for: 5000,
                    loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
                };
                api.rest.securities.get(rest_options).pipe(function (result) {
                    if (result.error) return view.notify(null), view.error(result.message);
                    var json = result.data, render,
                        security_type = json.template_data['securityType'].toLowerCase(),
                        template = module.name + '.' + security_type;
                    json.template_data.name = json.template_data.name || json.template_data.name.lang();
                    history.put({
                        name: json.template_data.name,
                        item: 'history.' + page_name + '.recent',
                        value: routes.current().hash
                    });
                    render = function (template) {
                        var error_html = '\
                                <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                    This security has been deleted\
                                </section>\
                            ',
                            $html = $.tmpl(template, json.template_data);
                        $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                        $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                        new og.common.gadgets.SecuritiesIdentifiers({
                            selector: '.og-js-identifiers',
                            id: rest_options.id
                        });
                        (function () {
                            if (json.template_data['underlyingOid']) {
                                var id = json.template_data['underlyingOid'],
                                    rule = view.rules.load_item,
                                    hash = routes.hash(rule, routes.current().args, {
                                        add: {id: id},
                                        del: ['version']
                                    }),
                                    text = json.template_data['underlyingName'] ||
                                        json.template_data['underlyingExternalId'],
                                    anchor = '<a class="og-js-live-anchor" href="' + routes.prefix() + hash + '">' +
                                        text + '</a>';
                                $('.OG-layout-admin-details-center .OG-js-underlying-id').html(anchor);
                            }
                        }());
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
                        if (json.template_data.hts_id || args.timeseries) new og.common.gadgets.Timeseries({
                            rest_options: {id: json.template_data.hts_id || args.timeseries},
                            selector: '.OG-timeseries-container'
                        })
                        if (show_loading) view.notify(null);
                        setTimeout(view.layout.inner.resizeAll);
                    };
                    $.when(
                        api.text({module: module.name + '.header'}),
                        api.text({module: template}),
                        api.text({module: module.name + '.attributes'})
                    ).pipe(function (header, template, attributes) {
                        return template.error ? (og.dev.warn('no template for: ' + security_type),
                            api.text({module: module.name + '.default'})) :
                                template.replace('${header}', header).replace('${attributes}', attributes);
                    }).pipe(render);
                });
            };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            details: details_page,
            load_filter: function (args) {
                view.filter = function () {
                        var filter_name = view.options.slickgrid.columns[0].name;
                        if (!filter_name || filter_name === 'loading') // wait until type filter is populated
                            return setTimeout(view.filter, 500);
                        search.filter();
                };
                view.check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                view.filter();
            },
            options: {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {id: 'type', toolTip: 'type', name: null, field: 'type', width: 100},
                        {
                            id: 'name', toolTip: 'name', field: 'name', width: 300, cssClass: 'og-link',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">'
                        }
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
            rules: view.rules(['name', 'type'], ['version', 'timeseries']),
            search: function (args) {
                if (!search) search = common.search_results.core();
                if (view.options.slickgrid.columns[0].name === 'loading')
                    return setTimeout(view.search.partial(args), 500);
                if (view.options.slickgrid.columns[0].name === null) return api.rest.securities.get({
                    meta: true,
                    handler: function (result) {
                        view.options.slickgrid.columns[0].name = [
                            '<select class="og-js-type-filter" style="width: 80px">',
                            result.data.types.reduce(function (acc, type) {
                                return acc + '<option value="' + type + '">' + type + '</option>';
                            }, '<option value="">Type</option>'),
                            '</select>'
                        ].join('');
                        view.search(args);
                    },
                    loading: function () {view.options.slickgrid.columns[0].name = 'loading';}
                });
                search.load($.extend(view.options.slickgrid, {url: args}));
            }
        });
    }
});