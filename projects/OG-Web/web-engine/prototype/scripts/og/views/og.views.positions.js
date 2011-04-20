/**
 * view for positions section
 */
$.register_module({
    name: 'og.views.positions',
    dependencies: [
        'og.common.routes', 'og.common.masthead.menu', 'og.common.search_results.core',
        'og.common.util.ui.message', 'og.views.common.layout', 'og.common.util.ui.toolbar'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, positions,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout,
            page_name = 'positions',
            check_state = og.views.common.state.check.partial('/' + page_name),
            get_quantities,
            /**
             * Options for SlickGrid.
             * Generate the search results columns.
             */
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'positions',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'},
                    {id: 'quantity', name: 'Quantity', field: 'quantity', width: 100, filter_type: 'input'},
                    {id: 'trades', name: 'Trades', field: 'trades', width: 50, filter_type: 'input'}
                ]
            },
            /**
             * Options for dialog boxes
             */
            dialog_new = {
                type: 'input',
                title: 'Add New Position',
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
                    'Ok': function () {
                        api.positions.put({
                            handler: function (r) {
                                if (r.error) return ui.dialog({type: 'error', message: r.message});
                                ui.dialog({type: 'input', action: 'close'});
                                routes.go(routes.hash(module.rules.load_new_positions,
                                        $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                ));
                            },
                            quantity: ui.dialog({return_field_value: 'quantity'}),
                            scheme_type: ui.dialog({return_field_value: 'scheme-type'}),
                            identifier: ui.dialog({return_field_value: 'identifier'})
                        });
                    }
                }
            },
            dialog_delete = {
                type: 'confirm',
                title: 'Delete Position?',
                message: 'Are you sure you want to permanently delete this position?',
                buttons: {
                    'Delete': function () {
                        api.positions.del({
                            handler: function (r) {
                                if (r.error) return ui.dialog({type: 'error', message: r.message});
                                ui.dialog({type: 'confirm', action: 'close'});
                                routes.go(routes.hash(module.rules.load_delete,
                                        $.extend({}, routes.last().args, {deleted: true})
                                ));
                            }, id: routes.last().args.id
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
            load_positions_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: positions.load, stop: true}]});
                delete args[field];
                positions.search(args);
                routes.go(routes.hash(module.rules.load_positions, args));
            },
            default_page = function () {
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            };
        module.rules = {
            load: {route: '/' + page_name + '/quantity:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/quantity:?', method: module.name + '.load_filter'},
            load_delete: {route: '/' + page_name + '/deleted:/quantity:?', method: module.name + '.load_delete'},
            load_positions: {
                route: '/' + page_name + '/:id/:node?/quantity:?', method: module.name + '.load_' + page_name
            },
            load_new_positions: {
                route: '/' + page_name + '/:id/:node?/new:/quantity:?', method: module.name + '.load_new_' + page_name
            },
            load_edit_positions: {
                route: '/' + page_name + '/:id/:node?/edit:/quantity:?', method: module.name + '.load_edit_' + page_name
            }
        };
        /**
         * @param input
         */
        get_quantities = function (input) {
            var obj = {}, str,
                regex_range         = /^\s*(-{0,1}[0-9]+)\s*-\s*(-{0,1}[0-9]+)\s*$/,  // (-)x-(-)x
                regex_less          = /^\s*<\s*(-{0,1}[0-9]+)\s*$/,                   // <(0)x
                regex_more          = /^\s*>\s*(-{0,1}[0-9]+)\s*$/,                   // >(0)x
                regex_less_or_equal = /^\s*<\s*=\s*(-{0,1}[0-9]+)\s*$/,               // <=(0)x
                regex_more_or_equal = /^\s*>\s*=\s*(-{0,1}[0-9]+)\s*$/,               // >=(0)x
                regex_exact         = /^\s*(-{0,1}[0-9]+)\s*$/;                       // (-)x
            str = input ? input.replace(/,/g, '') : '';
            switch (true) {
                case regex_less.test(str):
                    obj.max_quantity = +str.replace(regex_less, '$1') - 1;
                break;
                case regex_less_or_equal.test(str):
                    obj.max_quantity = str.replace(regex_less_or_equal, '$1');
                break;
                case regex_more.test(str):
                    obj.min_quantity = +str.replace(regex_more, '$1') + 1;
                break;
                case regex_more_or_equal.test(str):
                    obj.min_quantity = str.replace(regex_more_or_equal, '$1');
                break;
                case regex_exact.test(str):
                    obj.min_quantity = str.replace(regex_exact, '$1');
                    obj.max_quantity = str.replace(regex_exact, '$1');
                break;
                case regex_range.test(str):
                    obj.min_quantity = str.replace(regex_range, '$1');
                    obj.max_quantity = str.replace(regex_range, '$2');
                break;
            }
            return obj;
        };

        return positions = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        positions.search(args);
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
                        positions.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_positions, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(true, args, {filter: true}, get_quantities(args.quantity)));
            },
            load_delete: function (args) {
                positions.search(args);
                routes.go(routes.hash(module.rules.load, {}));
            },
            load_new_positions: load_positions_without.partial('new'),
            load_edit_positions: load_positions_without.partial('edit'),
            load_positions: function (args) {
                check_state({args: args, conditions: [{new_page: positions.load}]});
                positions.details(args);
            },
            search: function (args) {
                var obj = {};
                if (args.quantity) obj = get_quantities(args.quantity);
                search.load($.extend(true, search_options, {url: args}, {url: obj}));
            },
            details: function (args) {
                ui.toolbar(active_toolbar_options);
                api.positions.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data, f = details.position_functions;
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.render_main('.OG-position .og-js-main', json);
                            f.render_identifiers('.OG-position .og-js-identifiers', json.securities);
                            f.render_trade_rows('.OG-position .og-js-trades', json.trades);
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_positions, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
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