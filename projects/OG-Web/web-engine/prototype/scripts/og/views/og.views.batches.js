/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.batches',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.state',
        'og.views.common.default_details'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            routes = common.routes,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            view,
            details_page = function (args){
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                api.rest.batches.get({
                    dependencies: view.dependencies,
                    handler: function (result) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var batch_functions = details.batch_functions, json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var $html = $.tmpl(template, json.template_data);
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                            view.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                            batch_functions.results('.OG-js-details-panel .og-js-results', json.data.batch_results);
                            batch_functions.errors('.OG-js-details-panel .og-js-errors', json.data.batch_errors);
                            view.notify(null);
                            ui.toolbar(view.options.toolbar.active);
                            setTimeout(view.layout.inner.resizeAll);
                        }});
                    },
                    id: args.id,
                    loading: function () {view.notify({0: 'loading...', 3000: 'still loading...'});}
                });
            };
        return view = $.extend(new og.views.common.Core(page_name), {
                details: details_page,
                options: {
                    slickgrid: {
                        'selector': '.OG-js-search', 'page_type': page_name,
                        'columns': [
                            {
                                id: 'ob_date', field: 'date', width: 130, cssClass: 'og-link',
                                name: '<input type="text" placeholder="observation date" '
                                    + 'class="og-js-ob_date-filter" style="width: 110px;">',
                                toolTip: 'observation date'
                            },
                            {
                                id: 'ob_time', field: 'time', width: 130,
                                name: '<input type="text" placeholder="observation time" '
                                    + 'class="og-js-ob_time-filter" style="width: 110px;">',
                                toolTip: 'observation time'
                            },
                            {
                                id: 'status', field: 'status', width: 130, name: 'Status', toolTip: 'status'

                            }
                        ]
                    },
                    toolbar: {
                        'default': {
                            buttons: [
                                {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                                {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                                {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                                {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                            ],
                            location: '.OG-tools'
                        },
                        active: {
                            buttons: [
                                {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
                                {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                                {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                                {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                            ],
                            location: '.OG-tools'
                        }
                    }
                },
                rules: rules = {
                    load: {route: '/' + page_name + '/ob_date:?/ob_time:?', method: module.name + '.load'},
                    load_filter: {
                        route: '/' + page_name + '/filter:/:id?/ob_date:?/ob_time:?',
                        method: module.name + '.load_filter'
                    },
                    load_item: {route: '/' + page_name + '/:id/ob_date:?/ob_time:?', method: module.name + '.load_item'}
                }
            });
    }
});