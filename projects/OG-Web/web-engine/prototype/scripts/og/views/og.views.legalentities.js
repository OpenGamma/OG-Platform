/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.legalentities',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.toolbar'
    ],
    obj: function () {
        var api = og.api.rest, common = og.common, routes = og.common.routes, module = this, view, details = og.common.details,
            ui = og.common.util.ui, history = og.common.util.history, page_name = module.name.split('.').pop(),
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading, rest_options;
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                rest_options = {
                    dependencies: view.dependencies, update: view.update, id: args.id,
                    loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
                };
                $.when(api[page_name].get(rest_options), og.api.text({module: module.name}))
                    .then(function (result, template) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var json = result.data, $html = $.tmpl(template, json);
                        var legalentity_functions = details.legalentity_functions;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                        $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                        view.layout.inner.close('north'), $('.OG-layout-admin-details-north').empty();

                        $('.OG-details-content .og-js-name').text(json.template_data.name);
                        legalentity_functions.render_details('.OG-details-content .og-js-details', json.details);
                        legalentity_functions.render_attributes('.OG-details-content .og-js-attributes', json.attributes);
                        legalentity_functions.render_ratings('.OG-details-content .og-js-ratings', json.ratings);
                        legalentity_functions.render_accounts('.OG-details-content .og-js-accounts', json.accounts);
                        legalentity_functions.render_obligations('.OG-details-content .og-js-obligations', json.obligations_oids);
                        legalentity_functions.render_capabilities('.OG-details-content .og-js-capabilities', json.capabilities);
                        legalentity_functions.render_issued_securities('.OG-details-content .og-js-issued_securities', json.issued_securities_oids);
                        legalentity_functions.render_root_portfolio('.OG-details-content .og-js-root_portfolio', json.root_portfolio);
                        legalentity_functions.render_identifiers('.OG-details-content .og-js-identifiers', json.identifiers);

                        if (show_loading) view.notify(null);
                        ui.toolbar(view.options.toolbar.active);
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
                            id: 'name', toolTip: 'Name', field: 'name',
                            width: 300, cssClass: 'og-link',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 288px;">'
                        }
                    ]
                },
                toolbar: {
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: view.rules(['name'])
        });
    }
});
