/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.timeseries',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.toolbar'
    ],
    obj: function () {
        var api = og.api, common = og.common, details = common.details, history = common.util.history,
            routes = common.routes, ui = common.util.ui, module = this, view,
            page_name = module.name.split('.').pop(),
            toolbar_buttons = {
                'new': function () {
                    ui.dialog({
                        type: 'input',
                        title: 'Add New Timeseries',
                        width: 400, height: 480,
                        fields: [
                            {type: 'select', name: 'Scheme Type', id: 'scheme',
                                options: [
                                    {name: 'ActivFeed Ticker', value: 'ACTIVFEED_TICKER'},
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
                                        var args = routes.current().args, rule = view.rules.load_item;
                                        if (result.error) return view.error(result.message);
                                        view.search(args);
                                        if (result.meta.id)
                                            return routes.go(routes.hash(rule, args, {add: {id: result.meta.id}}));
                                        routes.go(routes.hash(view.rules.load, args));
                                    },
                                    scheme_type: ui.dialog({return_field_value: 'scheme'}),
                                    data_provider: ui.dialog({return_field_value: 'provider'}),
                                    data_field: ui.dialog({return_field_value: 'field'}),
                                    start: ui.dialog({return_field_value: 'start'}) || '',
                                    end: ui.dialog({return_field_value: 'end'}) || '',
                                    identifier: ui.dialog({return_field_value: 'identifiers'})
                                });
                            },
                            'Cancel': function () {$(this).dialog('close');}
                        }
                    })
                },
                'import': og.views.common.toolbar.upload,
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete timeseries?',
                        width: 400, height: 190,
                        message: 'Are you sure you want to permanently delete this timeseries?',
                        buttons: {
                            'Delete': function () {
                                api.rest.timeseries.del({
                                    handler: function (result) {
                                        ui.dialog({type: 'confirm', action: 'close'});
                                        if (result.error) return view.error(result.message);
                                        routes.go(routes.hash(view.rules.load, routes.current().args));
                                        setTimeout(function () {view.search(args);});
                                    }, id: routes.current().args.id
                                });
                            },
                            'Cancel': function () {$(this).dialog('close');}
                        }
                    })
                }
            },
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading, rest_options;
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                rest_options = {
                    dependencies: view.dependencies,
                    update: view.update,
                    id: args.id,
                    cache_for: 10000,
                    loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
                };
                $.when(api.rest.timeseries.get(rest_options), api.text({module: module.name}))
                    .then(function (result, template) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var json = result.data, $html, json_id = json.identifiers, title,
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
                                    return val.scheme === type ?
                                        acc ? acc + ', ' + val.value : type.lang() + ' - ' + val.value
                                            : acc;
                                }, '')
                            }(val)) || title;
                        });
                        json.template_data.title = title || json.template_data.name;
                        $html = $.tmpl(template, json.template_data);
                        history.put({
                            name: title + ' (' + json.template_data.data_field + ')',
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
                        // Update button
                        $('.OG-layout-admin-details-center .og-update').on('click', function () {
                            var args = routes.current().args;
                            view.notify('Updating TimeSeries...', 3000);
                            $.when(api.rest.timeseries.put({id: args.id})).then(function (result) {
                                if (result.error) return view.error(result.message);
                                view.notify('Updated', 3000);
                                view.details(routes.current().args, {hide_loading: true});
                            });
                        });
                        // Identifiers
                        $('.OG-layout-admin-details-center .og-js-identifiers tbody').html(
                            json_id.reduce(function (acc, cur) {
                                return acc + '<tr><td>[0]</td><td>[1]</td><td>[2]</td><td>[3]</td></tr>'
                                     .replace('[0]', cur.scheme.lang())
                                     .replace('[1]', cur.value)
                                     .replace('[2]', cur.date.start)
                                     .replace('[3]', cur.date.end);
                            }, '')
                        ).end().find('.OG-table').tablesorter();
                        // Plot
                        new og.common.gadgets.Timeseries({
                            rest_options: {id: result.data.template_data.object_id},
                            selector: '.OG-timeseries-container',
                            datapoints: true
                        })
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
                            {id: 'delete', tooltip: 'Delete', handler: toolbar_buttons['delete']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: view.rules(['data_field', 'data_provider', 'data_source', 'identifier', 'observation_time'])
        });
    }
});
