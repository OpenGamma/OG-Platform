/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.viewdefinition',
    dependencies: [
        'og.api.rest',
        'og.common.routes',
        'og.common.util.ui.message',
        'og.common.util.ui.Form',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, routes = og.common.routes,
            RMDF = 'resultModelDefinition',
            SETS = 'calculationConfiguration',
            DEFP = 'defaultProperties',
            COLS = 'portfolioRequirementsBySecurityType',
            RLTR = 'resolutionRuleTransform',
            SPEC = 'specificRequirement',
            SPVN = 'valueName',
            SPCT = 'computationTargetIdentifier',
            SPTT = 'computationTargetType',
            REQS = 'portfolioRequirement',
            SECU = 'securityType',
            REQO = 'requiredOutput',
            CONS = 'constraints';
        return function (config) {
            og.dev.log('config.data!', config.data.template_data.configJSON);
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop,
                orig_name = config.data.template_data.name, submit_type,
                resource_id = config.data.template_data.object_id,
                new_handler = config.new_handler, save_handler = config.save_handler,
                id_count = 0, prefix = 'view_def_', master = config.data.template_data.configJSON, column_set_tabs,
                form = new ui.Form({
                    module: 'og.views.forms.view-definition',
                    data: master,
                    selector: selector,
                    extras: {name: master.name},
                    processor: function (data) { // remove undefineds that we added
                        if (!data[SETS]) return;
                        data[SETS] = data[SETS].filter(function (set) {return set !== undefined;});
                        data[SETS].forEach(function (set, set_idx) {
                            if (set[COLS]) {
                                set[COLS] = set[COLS].filter(function (col) {return col !== undefined;});
                                set[COLS].forEach(function (col, col_idx) {
                                    if (!col[REQS]) return;
                                    col[REQS] = col[REQS].filter(function (req) {return req !== undefined;});
                                });
                            }
                            if (set[SPEC]) set[SPEC] = set[SPEC].filter(function (spec) {return spec !== undefined;});
                        });
                    }
                }),
                save_new_resource = function (data) {
                    if (orig_name === data.name) return window.alert('Please select a new name.');
                    api.configs.put({
                        name: data.name,
                        json: JSON.stringify(data),
                        loading: loading,
                        handler: new_handler
                    });
                },
                save_resource = function (data) {
                    api.configs.put({
                        id: resource_id,
                        name: data.name,
                        json: JSON.stringify(data),
                        loading: loading,
                        handler: save_handler
                    });
                };
            form.attach([
                {type: 'form:load', handler: load_handler},
                {type: 'click', selector: '#' + form.id + ' .og-js-submit', handler: function (e) {
                    submit_type = $(e.target).val();
                }},
                {type: 'form:submit', handler: function (result) {
                    og.dev.log(submit_type, result.data, result.errors);
                    switch (submit_type) {
                        case 'save': save_resource(result.data); break;
                        case 'save_as_new': save_new_resource(result.data); break;
                    }
                }},
                {type: 'click', selector: '#' + form.id + ' .og-js-collapse-handle', handler: function (e) {
                    var $target = $(e.target), $handle = $target.is('.og-js-collapse-handle') ? $target
                            : $target.parents('.og-js-collapse-handle:first'),
                        $container = $target.parents('.og-js-collapse-container:first'),
                        $el = $container.find('.og-js-collapse-element');
                    if ($handle.is('.og-open')) {
                        $el.hide();
                        $handle.removeClass('og-open').addClass('og-closed');
                        $container.removeClass('og-container-closed');
                    } else {
                        $el.show();
                        $handle.removeClass('og-closed').addClass('og-open');
                        $container.addClass('og-container-closed');
                    }
                }}
            ]);
            form.children = [
                new form.Block({ // form item_0
                    module: 'og.views.forms.view-definition-identifier-currency',
                    extras: {name: master.name},
                    children: [
                        new forms.Dropdown({
                            form: form, resource: 'portfolios', index: 'identifier', value: master.identifier,
                            placeholder: 'Please choose a portfolio...', fields: [0, 2]
                        })
                    ],
                    handlers: [
                        {type: 'form:load', handler: function () {
                            $('#' + form.id + ' select[name=currency]').val(master.currency);
                        }},
                        {type: 'keyup', selector: '#' + form.id + ' input[name=name]', handler: function (e) {
                            $('#' + form.id + ' h1').text($(e.target).val());
                        }}
                    ]
                }),
                (function () { // form item_1
                    var result_def = master[RMDF], ids = [], fields;
                    fields = ['primitive', 'security', 'position', 'aggregatePosition', 'trade'].map(function (val) {
                        return ids.push(prefix + id_count++), val + 'OutputMode';
                    });
                    return new form.Block({
                        module: 'og.views.forms.view-definition-result-model-definition',
                        extras: fields.reduce(function (acc, val, idx) {return acc[val] = ids[idx], acc;}, {}),
                        handlers: [{type: 'form:load', handler: function () {
                            fields.forEach(function (field, idx) {
                                $('#' + ids[idx]).attr('name', RMDF + '.' + field).val(result_def[field]);
                            });
                        }}]
                    })
                })(),
                new form.Block({ // form item_2
                    module: 'og.views.forms.view-definition-execution-parameters',
                    handlers: [{type: 'form:load', handler: function () {
                        ['DeltaCalcPeriod', 'FullCalcPeriod'].forEach(function (suffix) {
                            ['min', 'max'].forEach(function (prefix) {
                                $('#' + prefix + suffix).val(master[prefix + suffix]);
                            });
                        });
                    }}]
                })
            ];
            (function () {
                var column_sets = new form.Block({
                        handlers: [{
                            type: 'click', selector: '#' + form.id + ' .og-js-rem-colset',
                            handler: function (e) { // remove a column set
                                var rem_idx = $(this.selector).index(e.target),
                                    $set = $('#' + form.id + ' .og-js-colset-holder:eq(' + rem_idx + ')'),
                                    length = $('#' + form.id + ' .og-js-colset-holder').length,
                                    index = $('#' + form.id + ' .og-js-colset-holder').index($set),
                                    next = index ? index - 1 : index + 1, is_last = next === length,
                                    $tab = $('#' + form.id + ' .og-js-colset-tab:eq(' + index + ')'),
                                    is_active = $tab.is('.og-active'), $next_tab, $next_set,
                                    // because there may be previously removed colsets that have been set as undefined
                                    // we have to find the rem_idxth not-undefined thing in master[SETS]
                                    set_idx = master[SETS].reduce(function (acc, val, idx) {
                                        if (val) acc.rem_idx--;
                                        if (!acc.rem_idx) acc.idx = idx;
                                        return acc;
                                    }, {rem_idx: rem_idx + 1, idx: null}).idx;
                                master[SETS][set_idx] = undefined;
                                if (!is_last) {
                                    $next_tab = $('#' + form.id + ' .og-js-colset-tab:eq(' + next + ')');
                                    $next_set = $('#' + form.id + ' .og-js-colset-holder:eq(' + next + ')');
                                } else {
                                    $('#' + form.id + ' .og-js-empty-colsets').show();
                                }
                                $tab.parent('li').remove(), $set.remove();
                                if (is_last || !is_active) return false;
                                $next_tab.addClass('og-active');
                                $next_set.show();
                                return false;
                            }
                        }]
                    }),
                    new_col_set = function (set, set_idx) {
                        var set_id = prefix + id_count++, new_col_tab, new_col_val, new_spec_val,
                            spec_vals = new form.Block({wrap: '<ul class="og-js-spec-holder">{{html html}}</ul>'}),
                            col_vals = new form.Block({wrap: '{{html html}}'}),
                            col_tabs = new form.Block({wrap: '{{html html}}'}),
                            column_set = new form.Block({
                                wrap: '<div class="og-mod-content OG-clearFix og-js-colset-holder" id="' + set_id +
                                    '">{{html html}}</div>',
                                handlers: [
                                    {
                                        type: 'form:load', handler: function () {
                                            if (!$('#' + set_id + ' .og-js-col-holder').length)
                                                return $('#' + set_id + ' .og-js-empty-cols').show();
                                            $('#' + set_id + ' .og-js-col-holder:first').show().siblings().hide();
                                            $('#' + set_id + ' .og-js-col-tab:first').addClass('og-active');
                                        }
                                    },
                                    {
                                        type: 'click', selector: '#' + set_id + ' .og-js-add-spec',
                                        handler: function (e) { // add an additional value
                                            var $el = $('#' + set_id + ' .og-js-spec-holder'), block, spec = {};
                                            spec[SPVN] = '', spec[SPTT] = '', spec[SPCT] = '';
                                            if (!set[SPEC]) set[SPEC] = [spec]; else set[SPEC].push(spec);
                                            column_set.children.push(block = new_spec_val(spec, set[SPEC].length - 1));
                                            block.html(function (html) {$el.append($(html)), block.load();});
                                            return false;
                                        }
                                    },
                                    {
                                        type: 'click', selector: '#' + set_id + ' .og-js-rem-spec',
                                        handler: function (e) { // remove an additional value
                                            var $el = $(e.target).parents('.og-js-spec:first'),
                                                specs = $el.find('input:first').attr('name').split('.').slice(0, -1),
                                                index = specs.pop();
                                            specs.reduce(function (a, v) {return a[v];}, master)[index] = undefined;
                                            $el.remove();
                                            return false;
                                        }
                                    },
                                    {
                                        type: 'click', selector: '#' + set_id + ' .og-js-rem-col',
                                        handler: function (e) { // remove a column value
                                            var $target = $(e.target),
                                                $tab = $target.is('.og-js-col-tab') ? $target :
                                                    $target.parents('.og-js-col-tab:first'),
                                                index = $('#' + set_id + ' .og-js-col-tab').index($tab),
                                                $col = $('#' + set_id + ' .og-js-col-holder:eq(' + index + ')'),
                                                cols = $col.find('select:first').attr('name').split('.').slice(0, -1),
                                                col_idx = cols.pop(),
                                                length = $('#' + set_id + ' .og-js-col-holder').length,
                                                next = index ? index - 1 : index + 1, is_last = next === length,
                                                is_active = $tab.is('.og-active'), $next_tab, $next_col;
                                            if (!is_last) {
                                                $next_tab = $('#' + set_id + ' .og-js-col-tab:eq(' + next + ')');
                                                $next_col = $('#' + set_id + ' .og-js-col-holder:eq(' + next + ')');
                                            } else {
                                                $('#' + set_id + ' .og-js-empty-cols').show();
                                            }
                                            $col.remove(), $tab.remove();
                                            cols.reduce(function (a, v) {return a[v];}, master)[col_idx] = undefined;
                                            if (is_last || !is_active) return false;
                                            $next_tab.addClass('og-active');
                                            $next_col.show();
                                            return false;
                                        }
                                    },
                                    {
                                        type: 'click', selector: '#' + set_id + ' .og-js-rem-port-req',
                                        handler: function (e) { // remove a portfolio requirement
                                            var $li = $(e.target).parents('li:first'),
                                                reqs = $li.find('select:first').attr('name').split('.').slice(0, -1),
                                                index = reqs.pop();
                                            reqs.reduce(function (a, v) {return a[v];}, master)[index] = undefined;
                                            $li.remove();
                                            return false;
                                        }
                                    },
                                    {
                                        type: 'click', selector: '#' + set_id + ' .og-js-col-tab',
                                        handler: function (e) { // switch columns
                                            var $target = $(e.target),
                                                $tab = $target.is('.og-js-col-tab') ? $target :
                                                    $target.parents('.og-js-col-tab:first'),
                                                active_cl = '.og-active', new_cl = '.og-js-new',
                                                rem_cl = '.og-js-rem-col', index,
                                                is_remove = $target.is(rem_cl), is_active = $tab.is(active_cl),
                                                is_new = $tab.is(new_cl);
                                            if (is_active) return false;
                                            if (is_remove) return false;
                                            if (is_new) { // add a column value
                                                $('#' + set_id + ' .og-js-empty-cols').hide();
                                                (function () {
                                                    var block, col = {}, idx;
                                                    col[SECU] = '';
                                                    if (!set[COLS]) set[COLS] = [col]; else set[COLS].push(col);
                                                    idx = set[COLS].length - 1;
                                                    column_set.children.push(block = new_col_val(col, idx));
                                                    block.html(function (html) {
                                                        var tab, $col = $(html);
                                                        $('#' + set_id + ' .og-js-cols').append($col);
                                                        tab = new_col_tab(col, idx);
                                                        tab.html(function (html) {
                                                            var $tab = $(html);
                                                            $('#' + set_id + ' .og-js-col-tab.og-js-new')
                                                                .before($tab);
                                                            $tab.addClass('og-active').siblings()
                                                                .removeClass('og-active');
                                                            $col.siblings().hide();
                                                        });
                                                        col_tabs.children.push(tab);
                                                    });
                                                })();
                                                return false;
                                            }
                                            index = $('#' + set_id + ' .og-js-col-tab').index($tab);
                                            $('#' + set_id + ' .og-js-col-holder').each(function (idx, col) {
                                                $(col)[idx === index ? 'show' : 'hide']();
                                            });
                                            $('#' + set_id + ' .og-js-col-tab:eq(' + index + ')').addClass('og-active')
                                                .siblings().removeClass('og-active');
                                            return false;
                                        }
                                    }
                                ]
                            });
                        new_col_tab = function (col, col_idx) {
                            return new form.Field({
                                generator: function (handler) {
                                    var secu = col[SECU] || 'not set';
                                    handler('<li class="og-js-col-tab"><div class="og-delete og-js-rem-col"></div>' +
                                        '<strong class="og-js-secu">' + secu + '</strong></li>');
                                }
                            });
                        };
                        new_col_val = function (col, col_idx) {
                            var col_id = prefix + id_count++,
                                new_port_req = function (req, req_idx) {
                                    var sel_name = [SETS, set_idx, COLS, col_idx, REQS, req_idx, REQO].join('.'),
                                        cons_name = [SETS, set_idx, COLS, col_idx, REQS, req_idx, CONS].join('.');
                                    return new form.Block({
                                        module: 'og.views.forms.view-definition-portfolio-requirement',
                                        extras: {title: 'Column ' + (req_idx + 1), name: sel_name},
                                        children: [
                                            new forms.Dropdown({
                                                form: form, resource: 'valuerequirementnames', index: sel_name,
                                                value: req[REQO], rest_options: {meta: true},
                                                classes: 'og-js-collapse-element',
                                                placeholder: 'Please select...'
                                            }),
                                            new forms.Constraints({
                                                form: form, data: req[CONS], index: cons_name,
                                                classes: 'og-js-collapse-element'
                                            })
                                        ]
                                    });
                                },
                                reqs_block = new form.Block({
                                    wrap: '<ul class="og-js-port-req">{{html html}}</ul>'
                                });
                            if (col[REQS]) Array.prototype.push.apply(reqs_block.children, col[REQS].map(new_port_req));
                            return new form.Block({
                                module: 'og.views.forms.view-definition-column-value',
                                extras: {id: col_id},
                                handlers: [{
                                    type: 'click', selector: '#' + col_id + ' .og-js-add-port-req',
                                    handler: function (e) { // add a portfolio requirement
                                        var $ul = $(e.target).parents('div:first').find('ul.og-js-port-req'),
                                            block, req = {};
                                        req[CONS] = {}, req[REQO] = '';
                                        if (!col[REQS]) col[REQS] = [req]; else col[REQS].push(req);
                                        reqs_block.children.push(block = new_port_req(req, col[REQS].length - 1));
                                        block.html(function (html) {$ul.append($(html)), block.load();});
                                        return false;
                                    }
                                }],
                                children: [
                                    new forms.Dropdown({
                                        form: form, resource: 'securities', rest_options: {meta: true},
                                        index: [SETS, set_idx, COLS, col_idx, SECU].join('.'),
                                        value: col[SECU],
                                        handlers: [{
                                            type: 'change',
                                            selector: '#' + set_id + ' [name="' +
                                                [SETS, set_idx, COLS, col_idx, SECU].join('.') +'"]',
                                            handler: function (e) {
                                                $('#' + set_id + ' .og-js-col-tab:eq(' + col_idx + ')')
                                                    .find('.og-js-secu').text($(e.target).val());
                                            }
                                        }],
                                        placeholder: 'Please select...'
                                    }),
                                    reqs_block
                                ]
                            });
                        };
                        new_spec_val = function (spec, spec_idx) {
                            var names = {
                                name: 'Additional Value ' + (spec_idx + 1),
                                value: [SETS, set_idx, SPEC, spec_idx, SPVN].join('.'),
                                type: [SETS, set_idx, SPEC, spec_idx, SPTT].join('.'),
                                identifier: [SETS, set_idx, SPEC, spec_idx, SPCT].join('.')
                            };
                            return new form.Block({
                                module: 'og.views.forms.view-definition-specific-requirements',
                                extras: names,
                                handlers: [{type: 'form:load', handler: function () {
                                    $('#' + set_id + ' input[name="' + names.value + '"]').val(spec[SPVN]);
                                    $('#' + set_id + ' select[name="' + names.type + '"]').val(spec[SPTT]);
                                    $('#' + set_id + ' input[name="' + names.identifier + '"]').val(spec[SPCT]);
                                }}],
                                children: [
                                    new forms.Constraints({
                                        form: form, data: spec[CONS] || (spec[CONS] = {}),
                                        classes: 'og-js-collapse-element',
                                        index: [SETS, set_idx, SPEC, spec_idx, CONS].join('.')
                                    })
                                ]
                            });
                        };
                        column_set.children = [
                            new form.Block({ // column set top (name, default properties, etc)
                                module: 'og.views.forms.view-definition-column-set-top',
                                extras: {name: [SETS, set_idx, 'name'].join('.'), value: set.name},
                                children: [
                                    col_tabs, // column tabs
                                    col_vals, // column values
                                    spec_vals, // additional values
                                    new forms.Constraints({ // default properties
                                        form: form, data: set[DEFP], index: [SETS, set_idx, DEFP].join('.')
                                    }),
                                    new forms.RuleTransform({ // resolution rule transform
                                        form: form, data: set[RLTR], index: [SETS, set_idx, RLTR].join('.')
                                    })
                                ],
                                handlers: [
                                    {
                                        selector: '#' + set_id + ' input[name=' + [SETS, set_idx, 'name'].join('.')
                                            + ']',
                                        type: 'keyup',
                                        handler: function (e) {
                                            $('#' + form.id + ' .og-js-colset-tab.og-active .og-js-colset-tab-name')
                                                .text($(e.target).val());
                                        }
                                    },
                                    {
                                        type: 'click',
                                        selector: '#' + set_id + ' .og-js-colset-nav',
                                        handler: function (e) {
                                            var $target = $(e.target),
                                                $tab = $target.is(this.selector) ? $target
                                                    : $target.parents(this.selector + ':first'),
                                                navs = [
                                                    'og-js-col-vals', 'og-js-specs',
                                                    'og-js-def-prop', 'og-js-rule-trans'
                                                ],
                                                show = navs[$(this.selector).index($tab)];
                                            $tab.addClass('og-active').siblings().removeClass('og-active');
                                            navs.forEach(function (cl) {
                                                $('#' + set_id + ' .' + cl)[cl === show ? 'show' : 'hide']();
                                            });
                                        }
                                    }
                                ]
                            })
                        ];
                        // column tabs
                        if (set[COLS]) Array.prototype.push.apply(col_tabs.children, set[COLS].map(new_col_tab));
                        // column values
                        if (set[COLS]) Array.prototype.push.apply(col_vals.children, set[COLS].map(new_col_val));
                        // additional values
                        if (set[SPEC]) Array.prototype.push.apply(spec_vals.children, set[SPEC].map(new_spec_val));
                        return column_set;
                    };
                form.children.push(
                    new form.Block({ // form item_3
                        module: 'og.views.forms.view-definition-colset-tabs',
                        extras: {
                            tabs: master[SETS].reduce(function (acc, set, idx) {
                                return acc + '<li><a class="og-tab og-js-colset-tab' + (idx ? '' : ' og-active') + '"' +
                                    ' href="#"><div class="og-delete og-js-rem-colset"></div>' +
                                    '<span class="og-js-colset-tab-name">' + set.name + '</span></a></li>';
                            }, '')
                        },
                        handlers: [
                            {type: 'form:load', handler: function () {
                                $('#' + form.id + ' a.og-js-colset-tab').each(function (idx, tab) {
                                    $('#' + form.id + ' div.og-js-colset-holder:gt(0)').hide();
                                });
                            }},
                            // switch colsets
                            {type: 'click', selector: '#' + form.id + ' .og-js-colset-tab', handler: function (e) {
                                var $target = $(e.target),
                                    $tab = $target.is('.og-js-colset-tab') ? $target
                                        : $target.parents('.og-js-colset-tab:first'),
                                    active_cl = '.og-active', new_cl = '.og-js-new', rem_cl = '.og-js-rem-colset',
                                    is_remove = $target.is(rem_cl) || $target.parent(rem_cl).length,
                                    is_active = $target.is(active_cl) || $target.parent(active_cl).length,
                                    is_new = $target.is(new_cl) || $target.parent(new_cl).length, index;
                                if (is_active) return false;
                                if (is_remove) return false;
                                if (is_new) { // add a column set
                                    $('#' + form.id + ' .og-js-empty-colsets').hide();
                                    (function () {
                                        var $sec = $('#' + form.id + ' section.og-js-colsets'),
                                            block, set = {name: 'Set '};
                                        set[DEFP] = {};
                                        if (!master[SETS]) master[SETS] = [set]; else master[SETS].push(set);
                                        set.name += master[SETS].length;
                                        column_sets.children.push(block = new_col_set(set, master[SETS].length - 1));
                                        block.html(function (html) {
                                            $sec.append($(html));
                                            $('#' + form.id + ' .og-js-colset-tab').removeClass('og-active');
                                            $('#' + form.id + ' .og-js-colset-tabs .og-js-new').before($(
                                                '<li><a class="og-tab og-js-colset-tab og-active" href="#"><div ' +
                                                'class="og-delete og-js-rem-colset"></div>' +
                                                '<span class="og-js-colset-tab-name">' + set.name + '</span></a></li>'
                                            ));
                                            $('#' + form.id + ' .og-js-colset-holder').hide();
                                            $('#' + form.id + ' .og-js-colset-holder:last').show();
                                            block.load();
                                        });
                                    })();
                                    return false;
                                }
                                index = $('#' + form.id + ' .og-js-colset-tab').index($tab);
                                $('#' + form.id + ' .og-js-colset-holder').each(function (idx, set) {
                                    $(set)[idx === index ? 'show' : 'hide']();
                                });
                                $('#' + form.id + ' .og-js-colset-tab').removeClass('og-active');
                                $tab.addClass('og-active');
                                return false;
                            }}
                        ]
                    }),
                    column_sets // form item_4
                );
                Array.prototype.push.apply(column_sets.children, master[SETS].map(new_col_set));
            })();
            form.dom();
        };
    }
});