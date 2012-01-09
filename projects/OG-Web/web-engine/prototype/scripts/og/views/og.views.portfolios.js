/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.portfolios',
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
        'og.views.common.default_details',
        'og.views.common.versions',
        'og.views.extras.portfolios_sync'
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
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            json = {},
            view,
            toolbar_buttons = {
                'new': function () {
                    ui.dialog({
                        type: 'input',
                        title: 'Add New Portfolio',
                        fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                        buttons: {
                            'OK': function () {
                                $(this).dialog('close');
                                api.rest.portfolios.put({
                                    handler: function (result) {
                                        var args = routes.current().args, rule = module.rules.load_item;
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        view.search(args);
                                        routes.go(routes.hash(rule, args, {
                                            add: {id: result.meta.id}, del: ['version', 'node', 'sync']}));
                                    },
                                    name: ui.dialog({return_field_value: 'name'})
                                });
                            }
                        }
                    })
                },
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete portfolio?',
                        message: 'Are you sure you want to permanently delete ' +
                            '<strong style="white-space: nowrap">' + portfolio_name + '</strong>?',
                        buttons: {
                            'Delete': function () {
                                var args = routes.current().args, rest_options = {
                                    id: args.id,
                                    handler: function (result) {
                                        var args = routes.current().args, rule = module.rules.load;
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        view.search(args);
                                        routes.go(routes.hash(rule, args));
                                    }
                                };
                                $(this).dialog('close');
                                api.rest.portfolios.del(rest_options);
                            }
                        }
                    })
                },
                'versions': function () {
                    var rule = module.rules.load_item, args = routes.current().args;
                    routes.go(routes.prefix() + routes.hash(rule, args, {add: {version: '*'}}));
                    if (!layout.inner.state.south.isClosed && args.version) {
                        layout.inner.close('south');
                    } else layout.inner.open('south');
                    layout.inner.options.south.onclose = function () {
                        routes.go(routes.hash(rule, args, {del: ['version', 'node', 'sync']}));
                    };
                }
            },
            options = {
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
            default_details = og.views.common.default_details.partial(page_name, 'Portfolios', options),
            details_page = function (args) {
                var render_portfolio_rows = function (selector, json) {
                        var display_columns = [], data_columns = [], format = common.slickgrid.formatters.portfolios,
                            html = '\
                                <h3>Portfolios</h3>\
                                <a href="#" class="OG-link-add OG-js-add-sub-portfolio">add new portfolio</a>\
                                <div class="og-divider"></div>\
                                <div class="og-js-portfolios-grid og-grid"></div>';
                        $(selector).html(html);
                        (function () { /* Hook up add button */
                            var do_update = function () {
                                api.rest.portfolios.put({
                                    handler: function (result) {
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        view.details(args);
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
                                    fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                                    buttons: {
                                        'OK': function () {
                                            if (ui.dialog({return_field_value: 'name'}) === '') return;
                                            $(this).dialog('close');
                                            do_update();
                                        }
                                    }
                                });
                                return false;
                            });
                        }());
                        if (json.portfolios[0]) {
                            display_columns = [{
                                id: 'name', name: 'Name', field: 'name', cssClass: 'og-link',
                                width: 250, formatter: format
                            }],
                            data_columns = [{
                                id: 'id', name: 'Id', field: 'id', width: 100, formatter: format
                            }];
                        } else {
                            display_columns = [{id: 'name', name: 'Name', field: 'name', width: 250}],
                            json.portfolios = [{name: 'No portfolios', id: ''}]
                        }
                        display_columns = common.slickgrid.calibrate_columns({
                            container: selector + ' .og-js-portfolios-grid',
                            columns: display_columns,
                            buffer: 17
                        });
                        slick = new Slick.Grid(selector + ' .og-js-portfolios-grid',
                            json.portfolios, display_columns.concat(data_columns), {headerHeight: 33});
                        slick.setColumns(display_columns);
                        slick.setSelectionModel(new Slick.RowSelectionModel());
                        slick.onClick.subscribe(function (e, dd) {
                            var rule = module.rules.load_item, node = json.portfolios[dd.row];
                            if (!$(e.target).hasClass('og-icon-delete'))
                                return routes.go(routes.hash(rule, routes.current().args, {add: {node: node.id}}));
                            ui.dialog({
                                type: 'confirm',
                                title: 'Delete sub portfolio?',
                                message: 'Are you sure you want to permanently delete ' +
                                    '<strong style="white-space: nowrap">' + node.name + '</strong>?',
                                buttons: {'Delete': function () {
                                    api.rest.portfolios.del({
                                        id: routes.current().args.id, node: node.id,
                                        handler: function (result) {
                                            if (result.error)
                                                return ui.dialog({type: 'error', message: result.message});
                                            view.details(routes.current().args);
                                        }
                                    });
                                    $(this).dialog('close');
                                }}
                            });
                        });
                        slick.onMouseEnter.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').show();
                        });
                        slick.onMouseLeave.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').hide();
                        });
                    },
                    render_position_rows = function (selector, json) {
                        var display_columns = [], data_columns = [], format = common.slickgrid.formatters.positions,
                            html = '\
                              <h3>Positions</h3>\
                              <a href="#" class="OG-link-add OG-js-add-position">add new position</a>\
                              <div class="og-divider"></div>\
                              <div class="og-js-position-grid og-grid"></div>';
                        $(selector).html(html);
                        (function () { /* hook up add button */
                            var do_update = function (e, id) {
                                api.rest.portfolios.put({
                                    handler: function (result) {
                                        if (result.error) return ui.dialog({type: 'error', message: result.message});
                                        view.details(args);
                                    },
                                    position: id ? id.item.value : $input.val(),
                                    id: json.template_data.object_id,
                                    node: json.template_data.node
                               });
                               ui.dialog({type: 'input', action: 'close'});
                            };
                            $('.OG-js-add-position').click(function () {
                                ui.dialog({
                                    type: 'input',
                                    title: 'Add Position',
                                    fields: [{type: 'input', name: 'Identifier', id: 'name'}],
                                    buttons: {
                                        'OK': function () {
                                            if (ui.dialog({return_field_value: 'name'}) === '') return;
                                            do_update();
                                            $(this).dialog('close');
                                        }
                                    }
                                });
                                $('#og-js-dialog-name').autocomplete({
                                    source: function (obj, callback) {
                                        api.rest.positions.get({
                                            handler: function (r) {
                                                callback(
                                                    r.data.data.map(function (val) {
                                                        var arr = val.split('|');
                                                        return {value: arr[0], label: arr[1], id: arr[0], node: arr[1]};
                                                    })
                                                );
                                            },
                                            loading: '', page_size: 10, page: 1,
                                            identifier: '*' + obj.term.replace(/\s/g, '*') + '*'
                                        });
                                    },
                                    minLength: 1,
                                    select: function (e, ui) {do_update(e, ui);}
                                });
                                return false;
                            });
                        }());
                        if (json.positions[0]) {
                            display_columns = [
                                {id:"name", name:"Name", field:"name", width: 250, cssClass: 'og-link'},
                                {id:"quantity", name:"Quantity", field:"quantity", width: 80, formatter: format}
                            ],
                            data_columns = [
                                {id:"id", name:"Id", field:"id", width: 100, formatter: format}
                            ];
                        } else {
                            display_columns = [
                                {id:"name", name:"Name", field:"name", width: 250},
                                {id:"quantity", name:"Quantity", field:"quantity", width: 80}
                            ],
                            json.positions = [{name: 'No positions', quantity:'', id: ''}]
                        }
                        display_columns = og.common.slickgrid.calibrate_columns({
                            container: selector + ' .og-js-position-grid',
                            columns: display_columns,
                            buffer: 17
                        });
                        slick = new Slick.Grid(selector + ' .og-js-position-grid',
                            json.positions, display_columns.concat(data_columns), {headerHeight: 33});
                        slick.setColumns(display_columns);
                        slick.setSelectionModel(new Slick.RowSelectionModel());
                        slick.onClick.subscribe(function (e, dd) {
                            var row = json.positions[dd.row], position = row.id, position_name = row.name;
                            if (!$(e.target).hasClass('og-icon-unhook')) {
                                common.gadgets.positions({
                                    id: position, selector: '.og-js-details-positions', editable: false
                                });
                                common.gadgets.trades({id: position, selector: '.og-js-trades-table'});
                                return;
                            }
                            ui.dialog({
                                type: 'confirm',
                                title: 'Remove Position?',
                                message: 'Are you sure you want to remove the position '
                                    + '<strong style="white-space: nowrap">' + position_name + '</strong>'
                                    + ' from this portfolio?',
                                buttons: {'Delete': function () {
                                    var args = routes.current().args;
                                    api.rest.portfolios.del({
                                        id: args.id,
                                        node: json.template_data.node,
                                        position: position,
                                        handler: function (result) {
                                            if (result.error)
                                                return ui.dialog({type: 'error', message: result.message});
                                            view.details(routes.current().args);
                                        }
                                    });
                                    $(this).dialog('close');
                                }}
                            });
                        });
                        slick.onMouseEnter.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').show();
                        });
                        slick.onMouseLeave.subscribe(function (e) {
                           $(e.currentTarget).closest('.slick-row').find('.og-button').hide();
                        });
                    },
                    breadcrumb = function (config) {
                        var data = config.data, rule = module.rules.load_item, args = routes.current().args;
                        data.path.shift();
                        api.text({module: 'og.views.portfolios.breadcrumb', handler: function (template) {
                            $(config.selector).html($.tmpl(template, data, {
                                href: function (node) {
                                    var change_obj = !node ? {del: ['node']} : {add: {node: node}};
                                    return routes.prefix() + routes.hash(rule, args, change_obj);
                                },
                                title: function (name) {return name.length > 30 ? name : ''},
                                short_name: function (name) {
                                    return name.length > 30 ? name.slice(0, 27) + '...' : name
                                }
                            }));
                        }});
                    };
                if (args.version || args.sync) { // load versions
                    layout.inner.open('south');
                    if (args.version) og.views.common.versions.load();
                    if (args.sync) og.views.extras.portfolios_sync.load(args);
                } else layout.inner.close('south');
                api.rest.portfolios.get({
                    dependencies: ['id'],
                    handler: function (result) {
                        if (result.error) return alert(result.message); // TODO: replace with UI error dialog
                        json = result.data;
                        history.put({
                            name: portfolio_name = json.template_data.portfolio_name || json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            var error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This portfolio has been deleted\
                                    </section>\
                                ',
                                $html = $.tmpl(template, json.template_data), header, content;
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
                            render_portfolio_rows('.OG-js-details-panel .og-js-portfolios', json);
                            render_position_rows('.OG-js-details-panel .og-js-positions', json);
                            if (json.template_data.path) breadcrumb({
                                selector: '.OG-header-generic .OG-js-breadcrumb',
                                data: json.template_data
                            });
                            ui.content_editable({
                                handler: function () {
                                    view.search(args);
                                    view.details(args);
                                }
                            });
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            setTimeout(layout.inner.resizeAll);
                        }});
                    },
                    id: args.id,
                    node: args.node,
                    version: args.version && args.version !== '*' ? args.version : void 0,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
                            css: {left: 0},
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?', method: module.name + '.load'},
            load_filter: {
                route: '/' + page_name + '/filter:/:id?/node:?/version:?/name:?/sync:?',
                method: module.name + '.load_filter'
            },
            load_item: {
                route: '/' + page_name + '/:id/node:?/version:?/name:?/sync:?', method: module.name + '.load_item'
            }
        };
        return view = {
            breadcrumb_node: function (node) {
                var args = routes.current().args;
                setTimeout(routes.go.partial(routes.hash(module.rules.load_item, args, {add: {node: node}})));
            },
            breadcrumb_root: function () {
                var args = routes.current().args;
                setTimeout(routes.go.partial(routes.hash(module.rules.load_item, args, {del: ['node']})));
            },
            filters: ['name'],
            load: function (args) {
                layout = og.views.common.layout;
                check_state({args: args, conditions: [
                    {new_page: function (args) {view.search(args), masthead.menu.set_tab(page_name);}}
                ]});
                if (!args.id) default_details();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_value: 'id', stop: true, method: function (args) {
                        view[args.id ? 'load_item' : 'load'](args);
                    }},
                    {new_value: 'node', method: function (args) {
                        view[args.node ? 'load_item' : 'load'](args);
                    }}
                ]});
                search.filter(args);
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
                if (!search) search = common.search_results.core();
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});