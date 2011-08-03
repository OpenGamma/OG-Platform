/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
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
        'og.common.layout.resize',
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
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            resize = common.layout.resize,
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
                        'OK': function () {
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
                    'selector': '.OG-js-search', 'page_type': 'positions',
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'},
                        {id: 'quantity',
                            name: '<input type="text" '
                                + 'placeholder="Quantity" '
                                + 'class="og-js-quantity-filter" '
                                + 'style="width: 80px;">',
                            field: 'quantity', width: 100, filter_type: 'input'},
                        {id: 'trades', name: 'Trades', field: 'trades', width: 60}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
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
                var render_identifiers = function (json) {
                        $('.OG-js-details-panel .og-js-identifiers').html(json.reduce(function (acc, val) {
                            acc.push('<tr><td><span>' + val.scheme + '</span></td><td>' + val.value + '</td></tr>');
                            return acc
                        }, []).join(''));
                    },
                    render_trades = function (json) {
                        var fields = ['id', 'quantity', 'counterParty', 'date'], start = '<tr><td>', end = '</td></tr>',
                            selector = '.OG-js-details-panel .og-js-trades';
                        if (!json[0]) return $(selector).html('<tr><td colspan="4">No Trades</td></tr>');
                        $(selector).html(json.reduce(function (acc, trade) {
                            acc.push(start, fields.map(function (field) {return trade[field];}).join('</td><td>'), end);
                            return acc;
                        }, []).join(''));
                    };
                api.rest.positions.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.positions.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var $warning, warning_message = 'This position has been deleted';
                            $.tmpl(template, json.template_data).appendTo($('.OG-js-details-panel .OG-details').empty());
                            $warning = $('.OG-js-details-panel .og-box-error');
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $warning.html(warning_message).show();
                                resize();
                                $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                            } else {$warning.empty().hide(), resize();}
                            render_identifiers(json.securities);
                            render_trades(json.trades);
                            resize({element: '.OG-details-container', offsetpx: -41});
                            resize({element: '.OG-details-container .og-details-content', offsetpx: -48});
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_positions, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
                            details.favorites();
                            ui.message({location: '.OG-js-details-panel', destroy: true});
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
                        recent_list: history.get_html('history.positions.recent') || 'no recently viewed positions'
                    }).appendTo($('.OG-js-details-panel .OG-details').empty());
                    ui.toolbar(options.toolbar['default']);
                    $('.OG-js-details-panel .og-box-error').empty().hide(), resize();
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
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});