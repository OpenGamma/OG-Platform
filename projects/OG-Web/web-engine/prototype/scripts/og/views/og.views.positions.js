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
        'og.views.common.layout',
        'og.views.common.versions',
        'og.views.common.state',
        'og.views.common.default_details'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search, layout,
            ui = common.util.ui,
            module = this, view,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
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
                                handler: function (result) {
                                    var args = routes.current().args, rule = module.rules.load_item;
                                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                                    ui.dialog({type: 'input', action: 'close'});
                                    view.search(args);
                                    routes.go(routes.hash(rule, args, {add: {id: result.meta.id}, del: ['version']}));
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
                                handler: function (result) {
                                    var args = routes.current().args;
                                    ui.dialog({type: 'confirm', action: 'close'});
                                    if (result.error) return ui.dialog({type: 'error', message: result.message});
                                    view.search(args);
                                    routes.go(routes.hash(module.rules.load, args));
                                }, id: routes.last().args.id
                            });
                        }
                    }
                })},
                'versions': function () {
                    var rule = module.rules.load_item, args = routes.current().args;
                    routes.go(routes.prefix() + routes.hash(rule, args, {add: {version: '*'}}));
                    if (!layout.inner.state.south.isClosed && args.version) {
                        layout.inner.close('south');
                    } else layout.inner.open('south');
                    layout.inner.options.south.onclose = function () {
                        routes.go(routes.hash(rule, args, {del: ['version']}));
                    };
                }
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name'},
                        {id: 'quantity',
                            name: '<input type="text" '
                                + 'placeholder="Quantity" '
                                + 'class="og-js-quantity-filter" '
                                + 'style="width: 80px;">',
                            field: 'quantity', width: 100, toolTip: 'quantity'},
                        {id: 'trades', name: 'Trades', field: 'trades', width: 60, toolTip: 'trades'}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', divider: true, handler: toolbar_buttons['delete']},
                            {id: 'versions', label: 'versions', handler: toolbar_buttons['versions']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            details_page = function (args) {
                // load versions
                if (args.version) {
                    layout.inner.open('south');
                    og.views.common.versions.load();
                } else layout.inner.close('south');
                api.rest.positions.get({
                    dependencies: ['id'],
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This position has been deleted\
                                    </section>\
                                ',
                                header, content;
                            var $html = $.tmpl(template, json.template_data);
                            header = $.outer($html.find('> header')[0]);
                            content = $.outer($html.find('> section')[0]);
                            $('.ui-layout-inner-center .ui-layout-header').html(header);
                            $('.ui-layout-inner-center .ui-layout-content').html(content);
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                                $('.OG-tools .og-js-delete').addClass('OG-disabled').unbind();
                            } else {
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            common.gadgets.positions({
                                id: args.id, selector: '.og-js-details-positions', editable: true
                            });
                            common.gadgets.trades({id: args.id, selector: '.og-js-trades-table'});
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            setTimeout(layout.inner.resizeAll);
                        }});
                    },
                    id: args.id,
                    cache_for: 500,
                    version: args.version && args.version !== '*' ? args.version : void 0,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
                            css: {left: 0},
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            },
            default_details = og.views.common.default_details.partial(page_name, 'Positions', options);
        module.rules = {
            load: {route: '/' + page_name + '/quantity:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/quantity:?', method: module.name + '.load_filter'},
            load_item: {route: '/' + page_name + '/:id/:node?/version:?/quantity:?', method: module.name + '.load_item'}
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
        return view = {
            load: function (args) {
                layout = og.views.common.layout;
                check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), masthead.menu.set_tab(page_name);}}
                ]});
                if (!args.id) default_details();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                search.filter($.extend(true, args, get_quantities(args.quantity)));
            },
            load_item: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        view.load(args);
                        layout.inner.options.south.onclose = null;
                        layout.inner.close.partial('south');
                    }},
                    {new_value: 'id', method: function (args) {
                        layout.inner.options.south.onclose = null;
                        layout.inner.close.partial('south');
                    }}
                ]});
                view.details(args);
            },
            search: function (args) {
                var obj = {};
                if (!search) search = common.search_results.core();
                if (args.quantity) obj = get_quantities(args.quantity);
                search.load($.extend(true, options.slickgrid, {url: args}, {url: obj}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});