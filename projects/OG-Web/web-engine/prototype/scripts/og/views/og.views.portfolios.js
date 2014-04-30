/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.portfolios',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.versions',
        'og.views.common.versions',
        'og.views.extras.portfolios_sync',
        'og.common.gadgets.manager'
    ],
    obj: function () {
        var api = og.api, common = og.common, gadgets = common.gadgets, gadgets_manager = common.gadgets.manager,
            details = common.details, history = common.util.history,
            routes = common.routes, ui = common.util.ui, module = this,
            page_name = module.name.split('.').pop(), json = {},
            view, details_page, portfolio_name,
            create_portolio = function () {
                $(this).dialog('close');
                api.rest.portfolios.put({
                    handler: function (result) {
                        var args = routes.current().args, rule = view.rules.load_item;
                        if (result.error) return view.error(result.message);
                        view.search(args);
                        routes.go(routes.hash(rule, args, {
                            add: {id: result.meta.id}, del: ['version', 'node', 'sync']}));
                    },
                    name: ui.dialog({return_field_value: 'name'})
                });
            },
            toolbar_buttons = {
                'new': function () {
                    ui.dialog({
                        width: 400, height: 190,
                        type: 'input',
                        title: 'Add New Portfolio',
                        fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                        buttons: {
                            'OK': create_portolio,
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                },
                'import': og.views.common.toolbar.upload,
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete portfolio?',
                        width: 400, height: 190,
                        message: 'Are you sure you want to permanently delete ' +
                            '<strong style="white-space: nowrap">' + portfolio_name + '</strong>?',
                        buttons: {
                            'Delete': function () {
                                var args = routes.current().args, rest_options = {
                                    id: args.id,
                                    handler: function (result) {
                                        var args = routes.current().args, rule = view.rules.load;
                                        if (result.error) return view.error(result.message);
                                        routes.go(routes.hash(rule, args));
                                        setTimeout(function () {view.search(args); });
                                    }
                                };
                                $(this).dialog('close');
                                api.rest.portfolios.del(rest_options);
                            },
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                },
                'versions': function () {
                    var rule = view.rules.load_item, args = routes.current().args;
                    routes.go(routes.prefix() + routes.hash(rule, args, {add: {version: '*'}}));
                    if (!view.layout.inner.state.south.isClosed && args.version) {
                        view.layout.inner.close('south');
                    } else view.layout.inner.open('south');
                    view.layout.inner.options.south.onclose = function () {
                        routes.go(routes.hash(rule, args, {del: ['version', 'node', 'sync']}));
                    };
                }
            };
        details_page = function (args, config) {
            var show_loading = !(config || {}).hide_loading, rest_options,
                render_portfolio_rows, render_position, render_position_rows, breadcrumb;
            render_portfolio_rows = function (selector, json) {
                var display_columns = [], data_columns = [], format = common.slickgrid.formatters.portfolios, slick,
                    rand = Math.floor(Math.random() * 100000000), alive = 'alive-' + rand, no_message = 'No portfolios',
                    html = '\
                        <div class="og-divider">\
                            <h3><span>Portfolios</span></h3>\
                            <a href="#" class="OG-link-add OG-js-add-sub-portfolio">add new sub portfolio</a>\
                        </div>\
                        <div class="og-js-portfolios-grid og-grid"></div>';
                $(selector).html(html).addClass(alive);
                gadgets_manager.register({
                    alive: function () {return !!$('.' + alive).length;}, resize: function () {
                        if (json.portfolios[0] && json.portfolios[0].name !== no_message) {
                            display_columns = [{
                                id: 'name', name: 'Name', field: 'name', cssClass: 'og-link',
                                width: 250, formatter: format
                            }],
                            data_columns = [{id: 'id', name: 'Id', field: 'id', width: 100, formatter: format}];
                        } else {
                            display_columns = [{id: 'name', name: 'Name', field: 'name', width: 250}],
                            json.portfolios = [{name: no_message, id: ''}]
                        }
                        display_columns = common.slickgrid.calibrate_columns({
                            container: selector + ' .og-js-portfolios-grid',
                            columns: display_columns,
                            buffer: 18
                        });
                        if (typeof slick === 'object') return slick.setColumns(display_columns);
                        slick = new Slick.Grid(selector + ' .og-js-portfolios-grid',
                            json.portfolios, display_columns.concat(data_columns), {headerHeight: 33});
                        slick.setColumns(display_columns);
                        slick.setSelectionModel(new Slick.RowSelectionModel());
                        slick.onClick.subscribe(function (e, dd) {
                            var rule = view.rules.load_item, node = json.portfolios[dd.row];
                            if (!$(e.target).hasClass('og-icon-delete'))
                                return routes.go(routes.hash(rule, routes.current().args,
                                    {add: {node: node.id}, del: ['position']}));
                            ui.dialog({
                                type: 'confirm',
                                title: 'Delete sub-portfolio?',
                                width: 400, height: 190,
                                message: 'Are you sure you want to permanently delete ' +
                                    '<strong style="white-space: nowrap">' + node.name + '</strong>?',
                                buttons: {
                                    'Delete': function () {
                                        api.rest.portfolios.del({
                                            id: routes.current().args.id, node: node.id,
                                            handler: function (result) {
                                                if (result.error) return view.error(result.message);
                                            }
                                        });
                                        $(this).dialog('close');
                                    },
                                    'Cancel': function () {$(this).dialog('close');}
                                }
                            });
                        });
                        slick.onMouseEnter.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').show();
                        });
                        slick.onMouseLeave.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').hide();
                        });
                    }
                });
                (function () { /* Hook up add button */
                    var do_update = function () {
                        api.rest.portfolios.put({
                            handler: function (result) {
                                if (result.error) return view.error(result.message);
                            },
                            name: ui.dialog({return_field_value: 'name'}),
                            id: json.template_data.object_id,
                            node: json.template_data.node,
                            'new': true
                        });
                    };
                    $('.OG-js-add-sub-portfolio').click(function () {
                        ui.dialog({
                            type: 'input',
                            title: 'Add New sub Portfolio',
                            width: 400, height: 190,
                            fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                            buttons: {
                                'OK': function () {
                                    if (ui.dialog({return_field_value: 'name'}) === '') return;
                                    $(this).dialog('close');
                                    do_update();
                                },
                                'Cancel': function () {$(this).dialog('close');}
                            }
                        });
                        return false;
                    });
                }());
            };
            render_position = function (json) {
                var position = routes.current().args.position;
                // if the position in the URL is not in the JSON, it has been removed so don't display
                // but don't redirect so history will still work
                if (!position || !~json.positions.pluck('id').indexOf(position)) return;
                api.rest.positions.get({id: position, update: view.update, cache_for: 500, dependencies: ['position']});
                gadgets.positions({
                    id: position, selector: '.og-js-gadgets-positions', editable: false, view: view
                });
                gadgets.trades({id: position, selector: '.og-js-trades-table'});
            };
            render_position_rows = function (selector, json) {
                var display_columns = [], data_columns = [], format = common.slickgrid.formatters.positions, slick,
                    rand = Math.floor(Math.random() * 100000000), alive = 'alive-' + rand, no_message = 'No positions',
                    html = '\
                      <div class="og-divider">\
                          <h3><span>Positions</span></h3>\
                          <a href="#" class="OG-link-add OG-js-add-position">add new position</a>\
                      </div>\
                      <div class="og-js-position-grid og-grid"></div>';
                $(selector).html(html).addClass(alive);
                gadgets_manager.register({
                    alive: function () {return !!$('.' + alive).length;}, resize: function () {
                        if (json.positions[0] && json.positions[0].name !== no_message) {
                            display_columns = [
                                {id: 'name', name: 'Name', field: 'name', width: 250, cssClass: 'og-link'},
                                {id: 'quantity', name: 'Quantity', field: 'quantity', width: 80, formatter: format}
                            ],
                            data_columns = [{id: 'id', name: 'Id', field: 'id', width: 100, formatter: format}];
                        } else {
                            display_columns = [
                                {id: 'name', name: 'Name', field: 'name', width: 250},
                                {id: 'quantity', name: 'Quantity', field: 'quantity', width: 80}
                            ],
                            json.positions = [{name: no_message, quantity:'', id: ''}]
                        }
                        display_columns = og.common.slickgrid.calibrate_columns({
                            container: selector + ' .og-js-position-grid',
                            columns: display_columns,
                            buffer: 18
                        });
                        if (typeof slick === 'object') return slick.setColumns(display_columns);
                        slick = new Slick.Grid(selector + ' .og-js-position-grid',
                            json.positions, display_columns.concat(data_columns), {headerHeight: 33});
                        slick.setColumns(display_columns);
                        slick.setSelectionModel(new Slick.RowSelectionModel());
                        slick.onClick.subscribe(function (e, dd) {
                            var row = json.positions[dd.row], position = row.id, position_name = row.name;
                            if (!$(e.target).hasClass('og-icon-unhook')) {
                                return routes.go(routes.hash(
                                    view.rules.load_item, routes.current().args, {add: {position: position}}));
                            }
                            ui.dialog({
                                type: 'confirm',
                                title: 'Remove Position?',
                                message: 'Are you sure you want to remove the position '
                                    + '<strong style="white-space: nowrap">' + position_name + '</strong>'
                                    + ' from this portfolio?',
                                buttons: {
                                    'Delete': function () {
                                        var args = routes.current().args;
                                        api.rest.portfolios.del({
                                            id: args.id,
                                            node: json.template_data.node,
                                            position: position,
                                            handler: function (result) {
                                                if (result.error) return view.error(result.message);
                                            }
                                        });
                                        $(this).dialog('close');
                                    },
                                    'Cancel': function () {$(this).dialog('close');}
                                }
                            });
                        });
                        slick.onMouseEnter.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').show();
                        });
                        slick.onMouseLeave.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').hide();
                        });
                    }
                });
                (function () { /* hook up add button */
                    var do_update = function (e, id) {
                        api.rest.portfolios.put({
                            handler: function (result) {
                                if (result.error) return view.error(result.message);
                            },
                            position: id ? id.item.value : $input.val(),
                            id: json.template_data.object_id,
                            node: json.template_data.node
                       });
                       ui.dialog({type: 'input', action: 'close'});
                    };
                    $('.OG-js-add-position').click(function () {
                        var nodeId = json.template_data.node;
                        new og.blotter.Dialog({node:{name: nodeId, id: nodeId, portfolio: args.id},
                            handler: function (data) {return og.api.rest.blotter.trades.put(data);}
                        });
                    });
                }());
            };
            breadcrumb = function (config) {
                var data = config.data, rule = view.rules.load_item, args = routes.current().args;
                data.path.shift();
                api.text({module: 'og.views.portfolios.breadcrumb', handler: function (template) {
                    $(config.selector).html($.tmpl(template, data, {
                        href: function (node) {
                            return routes.prefix() + routes.hash(rule, args, node ?
                                {add: {node: node}, del: ['position']} : {del: ['node', 'position']});
                        },
                        title: function (name) {return name.length > 30 ? name : '';},
                        short_name: function (name) {
                            return name.length > 30 ? name.slice(0, 27) + '...' : name;
                        }
                    }));
                    ui.content_editable();
                }});
            };
            if (args.version || args.sync) { // load versions
                view.layout.inner.open('south');
                if (args.version) og.views.common.versions.load();
                if (args.sync) og.views.extras.portfolios_sync.load(args);
            } else view.layout.inner.close('south');
            rest_options = {
                dependencies: view.dependencies,
                update: view.update,
                id: args.id,
                node: args.node,
                version: args.version && args.version !== '*' ? args.version : void 0,
                loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
            };
            $.when(api.rest.portfolios.get(rest_options), og.api.text({module: module.name}))
                .then(function (result, template) {
                    if (result.error) {
                        view.notify(null);
                        if (args.node) {
                            view.error('There is no sub-portfolio with the ID: ' + args.node +
                                '. It may have been deleted.');
                            return routes.go(routes.hash(view.rules.load_item, args, {del: ['node']}));
                        }
                        return view.error(result.message);
                    }
                    var json = result.data, error_html = '\
                            <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                This portfolio has been deleted\
                            </section>\
                        ',
                        $html = $.tmpl(template, json.template_data);
                    history.put({
                        name: portfolio_name = json.template_data.portfolio_name || json.template_data.name,
                        item: 'history.' + page_name + '.recent',
                        value: routes.current().hash
                    });
                    $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                    $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                    ui.toolbar(view.options.toolbar.active);
                    if (json.template_data && json.template_data.deleted) {
                        $('.OG-layout-admin-details-north').html(error_html);
                        view.layout.inner.sizePane('north', '0');
                        view.layout.inner.open('north');
                        $('.OG-tools .og-js-delete').addClass('OG-disabled').unbind();
                    } else {
                        view.layout.inner.close('north');
                        $('.OG-layout-admin-details-north').empty();
                    }
                    render_portfolio_rows('.OG-js-details-panel .og-js-portfolios', json);
                    render_position_rows('.OG-js-details-panel .og-js-positions', json);
                    render_position(json);
                    if (json.template_data.path) breadcrumb({
                        selector: '.OG-header-generic .OG-js-breadcrumb',
                        data: json.template_data
                    });
                    ui.content_editable();
                    if (show_loading) view.notify(null);
                    setTimeout(view.layout.inner.resizeAll);
                });
        };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            dependencies: ['id', 'node', 'version'],
            details: details_page,
            load_filter: function (args) {
                view.check_state({args: args, conditions: [
                    {new_value: 'id', stop: true, method: function (args) {
                        view[args.id ? 'load_item' : 'load'](args);
                    }},
                    {new_value: 'node', method: function (args) {
                        view[args.node ? 'load_item' : 'load'](args);
                    }}
                ]});
                view.filter();
            },
            options: {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {
                            id: 'name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', handler: toolbar_buttons['import']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', handler: toolbar_buttons['import']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', divider: true, handler: toolbar_buttons['delete']},
                            {id: 'versions', label: 'versions', handler: toolbar_buttons['versions']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: view.rules(['name'], ['node', 'position', 'sync', 'version'])
        });
    }
});