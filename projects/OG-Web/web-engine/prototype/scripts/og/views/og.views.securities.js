/**
 * view for securities section
 */
$.register_module({
    name: 'og.views.securities',
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
        var api = og.api.rest, routes = og.common.routes, module = this, securities,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout, history = og.common.util.history,
            page_name = 'securities',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            /**
             * Options for SlickGrid.
             * Generate the search results columns.
             */
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'securities',
                'columns': [
                    {
                        id: 'type', name: 'Type', field: 'type', width: 80,
                        filter_type: 'select',
                        filter_type_options: ['BOND', 'CASH', 'OPTION', 'FRA', 'FUTURE', 'EQUITY', 'SWAP']
                    },
                    {
                        id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link',
                        filter_type: 'input'
                    }
                ]
            },
            /**
             * Options for dialog boxes
             */
            dialog_new = {
                type: 'input',
                title: 'Add Securities',
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
                    'Ok': function () {
                        api.securities.put({
                            handler: function (r) {
                                if (r.error) return ui.dialog({type: 'input', html: r.message});
                                ui.dialog({type: 'input', action: 'close'});
                                routes.go(routes.hash(module.rules.load_new_securities,
                                        $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                ));
                            },
                            scheme_type: ui.dialog({return_field_value: 'scheme-type'}),
                            identifier: ui.dialog({return_field_value: 'identifiers'})
                        });
                    }
                }
            },
            dialog_delete = {
                type: 'confirm',
                title: 'Delete portfolio?',
                message: 'Are you sure you want to permanently delete this portfolio?',
                buttons: {
                    'Delete': function () {
                        $(this).dialog('close');
                        api.securities.del({
                            handler: function () {routes.go(routes.hash(module.rules.load, {}));},
                            id: args.id
                        });
                    }
                }
            },
            buttons = {
                'new': function () {ui.dialog(dialog_new)},
                'delete': function () {ui.dialog(dialog_delete)}
            },
            /**
             * Options for the toolbar
             */
            default_toolbar_options = {
                buttons: [
                    {name: 'new', handler: buttons['new']},
                    {name: 'up', enabled: 'OG-disabled'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            active_toolbar_options = {
                buttons: [
                    {name: 'new', handler: buttons['new']},
                    {name: 'up', handler: 'handler'},
                    {name: 'edit', handler: 'handler'},
                    {name: 'delete', handler: buttons['delete']},
                    {name: 'favorites', handler: 'handler'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            load_securities_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: securities.load, stop: true}]});
                delete args[field];
                securities.search(args);
                routes.go(routes.hash(module.rules.load_securities, args));
            },
            default_page = function () {
                og.api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Securities',
                        favorites_list: history.get_html('history.securities.favorites') || 'no favorited securities',
                        recent_list: history.get_html('history.securities.recent') || 'no recently viewed securities',
                        new_list: history.get_html('history.securities.new') || 'no new securities'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            };
            state = {};
        module.rules = {
            load: {route: '/' + page_name + '/name:?/filter_type:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/type:?/name:?/filter_type:?',
                    method: module.name + '.load_filter'},
            load_delete: {route: '/' + page_name + '/deleted:/name:?/filter_type:?',
                    method: module.name + '.load_delete'},
            load_securities: {
                route: '/' + page_name + '/:id/type:/name:?/filter_type:?', method: module.name + '.load_' + page_name
            },
            load_new_securities: {
                route: '/' + page_name + '/new:/name:?/filter_type:?', method: module.name + '.load_new_' + page_name
            }
        };
        return securities = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        securities.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_page();
                ui.toolbar(default_toolbar_options);
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        securities.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_securities, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function () {
                securities.search();
                routes.go(routes.hash(module.rules.load, {}));
            },
            load_new_securities: function (args) {
                securities.load(args);
            },
            load_securities: function (args) {
                check_state({args: args, conditions: [{new_page: securities.load}]});
                securities.details(args);
            },
            search: function (args) {
                search.load($.extend(search_options, {url: args}));
            },
            details: function (args) {
                ui.toolbar(active_toolbar_options);
                api.securities.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.security_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.securities.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name + '.' + args.type, handler: function (template) {
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.render_security_identifiers('.OG-security .og-js-identifiers', details_json.identifiers);
                            details.favorites();
                            ui.message({location: '#OG-details', destroy: true});
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '#OG-details',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            },
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});