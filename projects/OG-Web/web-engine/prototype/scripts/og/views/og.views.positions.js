/**
 * view for positions section
 */
$.register_module({
    name: 'og.views.positions',
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
            search = common.search_results.core(),
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this, positions,
            page_name = 'positions',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            get_quantities,
            toolbar_buttons = {
                'new': function () {ui.dialog({
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
                            api.rest.positions.put({
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
                })},
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete Position?',
                    message: 'Are you sure you want to permanently delete this position?',
                    buttons: {
                        'Delete': function () {
                            api.rest.positions.del({
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
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'positions',
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'},
                        {id: 'quantity', name: 'Quantity', field: 'quantity', width: 100, filter_type: 'input'},
                        {id: 'trades', name: 'Trades', field: 'trades', width: 50}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', enabled: 'OG-disabled'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    },
                    active: {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', handler: 'handler'},
                            {name: 'edit', handler: 'handler'},
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'favorites', handler: 'handler'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    }
                }
            },
            load_positions_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: positions.load, stop: true}]});
                delete args[field];
                positions.search(args);
                routes.go(routes.hash(module.rules.load_positions, args));
            },
            details_page = function (args) {
                var render_securities = function (json) {
                        $('.OG-position .og-js-main').html([
                            '<td class="og-security"><a href=#/securities/', json.security.unique_id, '>', json.security.name, '</a></td>',
                            '<td>', json.security.security_type, '</td>',
                            '<td><strong class="og-quantity" data-og-editable="quantity">',
                            json.template_data.quantity, '</strong></td>'
                        ].join(''));
                    },
                    render_identifiers = function (json) {
                        $('.OG-position .og-js-identifiers').html(json.reduce(function (acc, val) {
                            acc.push(val.scheme, ': ', val.value, '<br />');
                            return acc
                        }, []).join(''));
                    },
                    render_trades = function (json) {
                        var fields = ['id', 'quantity', 'counterParty', 'date'], start = '<tr><td>', end = '</td></tr>',
                            selector = '.OG-position .og-js-trades';
                        if (!json[0]) return $(selector).html('<tr><td colspan="4">No Trades</td></tr>');
                        $(selector).html(json.reduce(function (acc, trade) {
                            acc.push(start, fields.map(function (field) {return trade[field];}).join('</td><td>'), end);
                            return acc;
                        }, []).join(''));
                    };
                ui.toolbar(options.toolbar.active);
                api.rest.positions.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        details_json = result.data;
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.positions.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var $warning, warning_message = 'This position has been deleted';
                            $.tmpl(template, details_json.template_data).appendTo($('#OG-details .og-main').empty());
                            $warning = $('#OG-details .OG-warning-message');
                            if (details_json.template_data.deleted) $warning.html(warning_message).show();
                                else $warning.empty().hide();
                            render_securities(details_json);
                            render_identifiers(details_json.securities);
                            render_trades(details_json.trades);
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
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Positions',
                        favorites_list: history.get_html('history.positions.favorites') || 'no favorited positions',
                        recent_list: history.get_html('history.positions.recent') || 'no recently viewed positions',
                        new_list: history.get_html('history.positions.new') || 'no new positions'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
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
         * @param {String} input text input from the quantity filter
         * @returns {Object} obj object with the required properties for the rest api (min_quantity, max_quantity)
         */
        get_quantities = function (input) {
            var obj = {}, str,
                range         = /^\s*(-{0,1}[0-9]+)\s*-\s*(-{0,1}[0-9]+)\s*$/,  // (-)x-(-)x
                less          = /^\s*<\s*(-{0,1}[0-9]+)\s*$/,                   // <(0)x
                more          = /^\s*>\s*(-{0,1}[0-9]+)\s*$/,                   // >(0)x
                less_or_equal = /^\s*<\s*=\s*(-{0,1}[0-9]+)\s*$/,               // <=(0)x
                more_or_equal = /^\s*>\s*=\s*(-{0,1}[0-9]+)\s*$/,               // >=(0)x
                exact         = /^\s*(-{0,1}[0-9]+)\s*$/;                       // (-)x
            str = input ? input.replace(/,/g, '') : '';
            switch (true) {
                case less.test(str): obj.max_quantity = +str.replace(less, '$1') - 1; break;
                case less_or_equal.test(str): obj.max_quantity = str.replace(less_or_equal, '$1'); break;
                case more.test(str): obj.min_quantity = +str.replace(more, '$1') + 1; break;
                case more_or_equal.test(str): obj.min_quantity = str.replace(more_or_equal, '$1'); break;
                case exact.test(str): obj.min_quantity = obj.max_quantity = str.replace(exact, '$1'); break;
                case range.test(str): obj.min_quantity = str.replace(range, '$1'),
                        obj.max_quantity = str.replace(range, '$2'); break;
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
                default_details_page();
                ui.toolbar(options.toolbar['default']);
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
                search.load($.extend(true, options.slickgrid, {url: args}, {url: obj}));
            },
            details: function (args) {details_page(args);},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});