/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.regions',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.toolbar'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, view, details = og.common.details,
            ui = og.common.util.ui, history = og.common.util.history,
            page_name = module.name.split('.').pop(),
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading;
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                api.regions.get({
                    dependencies: view.dependencies,
                    update: view.update,
                    handler: function (result) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var region_functions = details.region_functions, json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            var $html = $.tmpl(template, json);
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                            view.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                            region_functions.render_regions('.OG-details-content .og-js-parent_regions', json.parent);
                            region_functions.render_regions('.OG-details-content .og-js-child_regions', json.child);
                            if (show_loading) view.notify(null);
                            ui.toolbar(view.options.toolbar.active);
                            setTimeout(view.layout.inner.resizeAll);
                        }});
                    },
                    id: args.id,
                    loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
                });
            };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            details: details_page,
            options: {
                toolbar: {
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    }
                },
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {
                            id: 'name', field: 'name',width: 300, cssClass: 'og-link', toolTip: 'name',
                            name: '<input type="text" placeholder="Name" class="og-js-name-filter" ' +
                                'style="width: 280px;">'
                        }
                    ]
                }
            },
            rules: view.rules(['name'])
        });
    }
});
