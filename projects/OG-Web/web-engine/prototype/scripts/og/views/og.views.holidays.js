/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.holidays',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar'
    ],
    obj: function () {
        var api = og.api, common = og.common, details = common.details, history = common.util.history,
            routes = common.routes, ui = common.util.ui, module = this,
            page_name = module.name.split('.').pop(), view,
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading;
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                api.rest.holidays.get({
                    dependencies: view.dependencies,
                    update: view.update,
                    handler: function (result) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var json = result.data;
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
                            $('.OG-holiday .og-calendar').datepicker({
                                numberOfMonths: [4, 3],                     // Layout configuration
                                showCurrentAtPos: new Date().getMonth(),    // Makes the first month January
                                showButtonPanel: false,                     // Turns off default buttons
                                stepMonths: 12,                             // Pagination moves 1 year at a time
                                firstDay: 1,                                // Start the week on Monday
                                displayOnly: true,                          // This is an OG custom configuration
                                specialDates: json.dates                    // This is an OG custom configuration
                            });
                            ui.toolbar(view.options.toolbar.active);
                            if (show_loading) view.notify(null);
                            details.calendar_ui_changes(json.dates);
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
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {
                            id: 'name', field: 'name', width: 100, cssClass: 'og-link', toolTip: 'name',
                            name:
                                '<input type="text" placeholder="name" class="og-js-name-filter" style="width: 80px;">'
                        },
                        {
                            id: 'type', field: 'type', width: 200, toolTip: 'type',
                            name: '<select class="og-js-type-filter" style="width: 180px">\
                                     <option value="">Type</option>\
                                     <option>CURRENCY</option>\
                                     <option>BANK</option>\
                                     <option>SETTLEMENT</option>\
                                     <option>TRADING</option>\
                                   </select>'
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
            rules: view.rules(['name', 'type'])
        });
    }
});