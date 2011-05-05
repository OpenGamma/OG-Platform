/**
 * view for holidays section
 */
$.register_module({
    name: 'og.views.holidays',
    dependencies: [
        'og.common.routes',
        'og.common.masthead.menu',
        'og.common.search_results.core',
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.util.ui.toolbar',
        'og.common.util.history'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, holidays,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout, history = og.common.util.history,
            page_name = 'holidays',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {}, // The returned json for the details area
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'holidays',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 100, cssClass: 'og-link', filter_type: 'input'},
                    {
                        id: 'type', name: 'Type', field: 'type', width: 200,
                        filter_type: 'select',
                        filter_type_options: ['currency', 'bank', 'settlement', 'trading']
                    }
                ]
            },
            default_toolbar_options = {
                buttons: [
                    {name: 'new', enabled: 'OG-disabled'},
                    {name: 'up', enabled: 'OG-disabled'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            active_toolbar_options = {
                buttons: [
                    {name: 'new', enabled: 'OG-disabled'},
                    {name: 'up', handler: 'handler'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', handler: 'handler'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            default_page = function () {
                og.api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Holidays',
                        favorites_list: history.get_html('history.holidays.favorites') || 'no favorited holidays',
                        recent_list: history.get_html('history.holidays.recent') || 'no recently viewed holidays'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            };
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/filter_type:?',
                method: module.name + '.load_filter'},
            load_holidays: {
                route: '/' + page_name + '/:id/name:?/filter_type:?',
                method: module.name + '.load_' + page_name
            }
        };
        return holidays = {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                ui.toolbar(default_toolbar_options);
                layout('default');
                search.load($.extend(search_options, {url: args}));
                default_page();
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
                // Load search if changed
                if (routes.last()) {
                    if (routes.last().page !== module.rules.load.route) {
                        masthead.menu.set_tab(page_name);
                        search.load($.extend(search_options, {url: args}));
                    } else ui.toolbar(active_toolbar_options);
                }else{
                    masthead.menu.set_tab(page_name);
                    ui.toolbar(active_toolbar_options);
                    search.load($.extend(search_options, {url: args}));
                }
                // Setup details page
                layout('default');
                api.holidays.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.holidays.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            $('.OG-holiday .og-calendar').datepicker({
                                numberOfMonths: [4, 3],                     // Layout configuration
                                showCurrentAtPos: new Date().getMonth(),    // Makes the first month January
                                showButtonPanel: false,                     // Turns off default buttons
                                stepMonths: 12,                             // Pagination moves 1 year at a time
                                firstDay: 1,                                // Start the week on Monday
                                displayOnly: true,                          // This is an OG custom configuration
                                specialDates: details_json.dates            // This is an OG custom configuration
                            });
                            details.favorites();
                            ui.message({location: '#OG-details', destroy: true});
                            details.calendar_ui_changes(details_json.dates);
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});