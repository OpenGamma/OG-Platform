/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.holidays',
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
            check_state = og.views.common.state.check.partial('/' + page_name),
            holidays,
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'holidays',
                    'columns': [
                        {
                            id: 'name',
                            name: '<input type="text" placeholder="name" '
                                + 'class="og-js-name-filter" style="width: 80px;">',
                            field: 'name',
                            width: 100,
                            cssClass: 'og-link',
                            filter_type: 'input',
                            toolTip: 'name'
                        },
                        {
                            id: 'type',
                            name: '<select class="og-js-type-filter" style="width: 180px">'
                                + '  <option value="">Type</option>'
                                + '  <option>CURRENCY</option>'
                                + '  <option>BANK</option>'
                                + '  <option>SETTLEMENT</option>'
                                + '  <option>TRADING</option>'
                                + '</select>',
                            field: 'type', width: 200,
                            filter_type: 'select',
                            filter_type_options: ['CURRENCY', 'BANK', 'SETTLEMENT', 'TRADING'],
                            toolTip: 'type'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar'
                    }
                }
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $html = $.tmpl(template, {
                        name: 'Holidays',
                        favorites_list: history.get_html('history.holidays.favorites') || 'no favorited holidays',
                        recent_list: history.get_html('history.holidays.recent') || 'no recently viewed holidays'
                    });
                    $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                    $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                    og.views.common.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                    ui.toolbar(options.toolbar['default']);
                }});
            },
            details_page = function (args) {
                api.rest.holidays.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.holidays.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var $html = $.tmpl(template, json.template_data);
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                            og.views.common.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                            $('.OG-holiday .og-calendar').datepicker({
                                numberOfMonths: [4, 3],                     // Layout configuration
                                showCurrentAtPos: new Date().getMonth(),    // Makes the first month January
                                showButtonPanel: false,                     // Turns off default buttons
                                stepMonths: 12,                             // Pagination moves 1 year at a time
                                firstDay: 1,                                // Start the week on Monday
                                displayOnly: true,                          // This is an OG custom configuration
                                specialDates: json.dates                    // This is an OG custom configuration
                            });
                            details.favorites();
                            ui.toolbar(options.toolbar.active);
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            details.calendar_ui_changes(json.dates);
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
            load: {route: '/' + page_name + '/name:?/type:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?', method: module.name + '.load_filter'},
            load_holidays:
                {route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_' + page_name}
        };
        return holidays = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        holidays.search(args);
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
                        holidays.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_holidays, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_holidays: function (args) {
                check_state({args: args, conditions: [{new_page: holidays.load}]});
                holidays.details(args);
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