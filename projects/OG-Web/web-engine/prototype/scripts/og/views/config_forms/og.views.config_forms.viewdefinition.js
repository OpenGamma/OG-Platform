/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.viewdefinition',
    dependencies: [
        'og.api.rest',
        'og.common.util.ui',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var Form = og.common.util.ui.Form, forms = og.views.forms, api = og.api.rest,
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
            CONS = 'constraints',
            WITH = 'with',
            WTHO = 'without',
            INDX = '<INDEX>',
            arr = function (obj) {return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [];};
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name,
                resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler,
                id_count = 0, prefix = 'viewdef_',
                master = config.data.template_data.configJSON.data,
                column_set_tabs,
                config_type = config.type,
                type_map = [
                    ['0',                                                                           Form.type.STR],
                    // <constraints>
                    [[SETS, INDX, DEFP, WITH, '*'].join('.'),                                       Form.type.IND],
                    [[SETS, INDX, DEFP, WITH, '*', 'optional'].join('.'),                           Form.type.IND],
                    [[SETS, INDX, DEFP, WITH, '*', '*'].join('.'),                                  Form.type.STR],
                    [[SETS, INDX, DEFP, WTHO].join('.'),                                            Form.type.STR],
                    [[SETS, INDX, DEFP, WTHO, '*'].join('.'),                                       Form.type.STR],
                    // </constraints>
                    [[SETS, INDX, 'name'].join('.'),                                                Form.type.STR],
                    // <constraints>
                    [[SETS, INDX, COLS, INDX, REQS, INDX, CONS, WITH, '*'].join('.'),               Form.type.IND],
                    [[SETS, INDX, COLS, INDX, REQS, INDX, CONS, WITH, '*', 'optional'].join('.'),   Form.type.IND],
                    [[SETS, INDX, COLS, INDX, REQS, INDX, CONS, WITH, '*', '*'].join('.'),          Form.type.STR],
                    [[SETS, INDX, COLS, INDX, REQS, INDX, CONS, WTHO].join('.'),                    Form.type.STR],
                    [[SETS, INDX, COLS, INDX, REQS, INDX, CONS, WTHO, '*'].join('.'),               Form.type.STR],
                    // </constraints>
                    [[SETS, INDX, COLS, INDX, REQS, INDX, REQO].join('.'),                          Form.type.STR],
                    [[SETS, INDX, COLS, INDX, SECU].join('.'),                                      Form.type.STR],
                    [[SETS, INDX, RLTR, '0'].join('.'),                                             Form.type.STR],
                    [[SETS, INDX, RLTR, '*'].join('.'),                                             Form.type.STR],
                    [[SETS, INDX, SPEC, INDX, SPCT].join('.'),                                      Form.type.STR],
                    [[SETS, INDX, SPEC, INDX, SPTT].join('.'),                                      Form.type.STR],
                    // <constraints>
                    [[SETS, INDX, SPEC, INDX, CONS, WITH, '*'].join('.'),                           Form.type.IND],
                    [[SETS, INDX, SPEC, INDX, CONS, WITH, '*', 'optional'].join('.'),               Form.type.IND],
                    [[SETS, INDX, SPEC, INDX, CONS, WITH, '*', '*'].join('.'),                      Form.type.STR],
                    [[SETS, INDX, SPEC, INDX, CONS, WTHO].join('.'),                                Form.type.STR],
                    [[SETS, INDX, SPEC, INDX, CONS, WTHO, '*'].join('.'),                           Form.type.STR],
                    // </constraints>
                    [[SETS, INDX, SPEC, INDX, SPVN].join('.'),                                      Form.type.STR],
                    ['currency',                                                                    Form.type.STR],
                    ['identifier',                                                                  Form.type.STR],
                    ['maxDeltaCalcPeriod',                                                          Form.type.SHR],
                    ['maxFullCalcPeriod',                                                           Form.type.SHR],
                    ['minDeltaCalcPeriod',                                                          Form.type.SHR],
                    ['minFullCalcPeriod',                                                           Form.type.SHR],
                    ['name',                                                                        Form.type.STR],
                    [[RMDF, 'aggregatePositionOutputMode'].join('.'),                               Form.type.STR],
                    [[RMDF, 'positionOutputMode'].join('.'),                                        Form.type.STR],
                    [[RMDF, 'primitiveOutputMode'].join('.'),                                       Form.type.STR],
                    [[RMDF, 'securityOutputMode'].join('.'),                                        Form.type.STR],
                    [[RMDF, 'tradeOutputMode'].join('.'),                                           Form.type.STR],
                    ['uniqueId',                                                                    Form.type.STR],
                    [['user', 'ipAddress'].join('.'),                                               Form.type.STR],
                    [['user', 'userName'].join('.'),                                                Form.type.STR]
                ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {}),
                form = new Form({
                    module: 'og.views.forms.view-definition',
                    data: master,
                    type_map: type_map,
                    selector: selector,
                    extras: {name: master.name},
                    processor: function (data) {
                        if (!data[SETS]) return;
                        // remove undefineds that we added
                        data[SETS] = arr(data[SETS]).filter(function (set) {return set !== undefined;});
                        data[SETS].forEach(function (set, set_idx) {
                            if (set[COLS]) {
                                set[COLS] = arr(set[COLS]).filter(function (col) {return col !== undefined;});
                                set[COLS].forEach(function (col, col_idx) {
                                    if (!col[REQS]) return;
                                    col[REQS] = col[REQS].filter(function (req) {return req !== undefined;});
                                });
                            }
                            if (set[SPEC])
                                set[SPEC] = arr(set[SPEC]).filter(function (spec) {return spec !== undefined;});
                        });
                    }
                }),
                form_id = '#' + form.id,
                save_resource = function (result) {
                    var data = result.data, meta = result.meta, as_new = result.extras.as_new;
                    if (!deleted && !is_new && as_new && (orig_name === data.name))
                        return window.alert('Please select a new name.');
                    api.configs.put({
                        id: as_new ? void 0 : resource_id,
                        name: data.name,
                        json: JSON.stringify({data: data, meta: meta}),
                        type: config_type,
                        loading: loading,
                        handler: as_new ? save_new_handler : save_handler
                    });
                };
            master[SETS] = arr(master[SETS]); // initialize master[SETS]
            form.attach([
                {type: 'form:load', handler: function () {
                    var header = '\
                        <header class="OG-header-generic">\
                          <div class="OG-tools"></div>\
                          <h1 class="og-js-name">' + master.name + '</h1>\
                          (View Definition)\
                        </header>\
                    ';
                    $('.ui-layout-inner-center .ui-layout-header').html(header);
                    load_handler(form);
                }},
                {type: 'form:submit', handler: save_resource},
                {type: 'click', selector: form_id + ' .og-js-collapse-handle', handler: function (e) {
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
                            rest_options: {page: '*'}, placeholder: 'Please choose a portfolio...', fields: [0, 2],
                            processor: function (selector, data) {if (!data.identifier) delete data.identifier;}
                        }),
                        new form.Field({module: 'og.views.forms.currency', generator: function (handler, template) {
                            handler(template);
                        }})
                    ],
                    handlers: [
                        {type: 'form:load', handler: function () {
                            $(form_id + ' select[name=currency]').val(master.currency);
                        }},
                        {type: 'keyup', selector: form_id + ' input[name=name]', handler: function (e) {
                            $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
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
                            type: 'click', selector: form_id + ' .og-js-rem-colset',
                            handler: function (e) { // remove a column set
                                var rem_idx = $(this.selector).index(e.target),
                                    $set = $(form_id + ' .og-js-colset-holder:eq(' + rem_idx + ')'),
                                    length = $(form_id + ' .og-js-colset-holder').length,
                                    index = $(form_id + ' .og-js-colset-holder').index($set),
                                    next = index ? index - 1 : index + 1, is_last = next === length,
                                    $tab = $(form_id + ' .og-js-colset-tab:eq(' + index + ')'),
                                    is_active = $tab.is('.og-active'), $next_tab, $next_set,
                                    // because there may be previously removed colsets that have been undefined
                                    // we have to find the rem_idxth not-undefined thing in master[SETS]
                                    set_idx = master[SETS].reduce(function (acc, val, idx) {
                                        if (val) acc.rem_idx--;
                                        if (!acc.rem_idx && acc.idx === null) acc.idx = idx;
                                        return acc;
                                    }, {rem_idx: rem_idx + 1, idx: null}).idx;
                                master[SETS][set_idx] = undefined;
                                if (!is_last) {
                                    $next_tab = $(form_id + ' .og-js-colset-tab:eq(' + next + ')');
                                    $next_set = $(form_id + ' .og-js-colset-holder:eq(' + next + ')');
                                } else {
                                    $(form_id + ' .og-js-empty-colsets').show();
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
                                            e.preventDefault();
                                            var $target = $(e.target), tab_cl = '.og-js-col-tab',
                                                $tab = $target.is(tab_cl) ? $target
                                                    : $target.parents(tab_cl + ':first'),
                                                active_cl = '.og-active', new_cl = '.og-js-new',
                                                clone_cl = '.og-js-clone', rem_cl = '.og-js-rem-col', index,
                                                is_remove = $target.is(rem_cl), is_active = $tab.is(active_cl),
                                                is_new = $tab.is(new_cl), is_clone = $tab.is(clone_cl),
                                                set_idx, col_idx;
                                            if (is_active) return false;
                                            if (is_remove) return false;
                                            if (is_new || is_clone) { // add a column value
                                                $('#' + set_id + ' .og-js-empty-cols').hide();
                                                return (function () {
                                                    var block, col, idx;
                                                    if (is_new) {
                                                        col = {}, col[SECU] = '';
                                                    } else {
                                                        col_idx = $(form_id + ' ' + tab_cl)
                                                            .index($(form_id + ' ' + tab_cl + active_cl));
                                                        set_idx = $(form_id + ' .og-js-colset-tab')
                                                            .index($(form_id + ' ' + active_cl));
                                                        if (!~col_idx) return;
                                                        col = $.extend(true, {},
                                                            form.compile().data[SETS][set_idx][COLS][col_idx]
                                                        );
                                                        if (col[REQS]) col[REQS] = arr(col[REQS]);
                                                    }
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
                                                        block.load();
                                                    });
                                                })();
                                            }
                                            index = $('#' + set_id + ' .og-js-col-tab').index($tab);
                                            $('#' + set_id + ' .og-js-col-holder').each(function (idx, col) {
                                                $(col)[idx === index ? 'show' : 'hide']();
                                            });
                                            $('#' + set_id + ' .og-js-col-tab:eq(' + index + ')').addClass('og-active')
                                                .siblings().removeClass('og-active');
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
                                                value: req[REQO], rest_options: {meta: true, page: '*'},
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
                            if (col[REQS] = arr(col[REQS]))
                                Array.prototype.push.apply(reqs_block.children, col[REQS].map(new_port_req));
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
                                        form: form, resource: 'securities', value: col[SECU],
                                        rest_options: {meta: true, cache_for: 300 * 1000},
                                        index: [SETS, set_idx, COLS, col_idx, SECU].join('.'),
                                        handlers: [{
                                            type: 'change',
                                            selector: '#' + set_id + ' [name="' +
                                                [SETS, set_idx, COLS, col_idx, SECU].join('.') +'"]',
                                            handler: function (e) {
                                                var idx = $(this.selector).parents('.og-js-col-holder:first')
                                                    .index('#' + set_id + ' .og-js-col-holder');
                                                $('#' + set_id + ' .og-js-col-tab:eq(' + idx + ')')
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
                                            $(form_id + ' .og-js-colset-tab.og-active .og-js-colset-tab-name')
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
                        if ((set[COLS] = arr(set[COLS])).length)
                            Array.prototype.push.apply(col_tabs.children, set[COLS].map(new_col_tab));
                        // column values
                        if ((set[COLS] = arr(set[COLS])).length)
                            Array.prototype.push.apply(col_vals.children, set[COLS].map(new_col_val));
                        // additional values
                        if ((set[SPEC] = arr(set[SPEC])).length)
                            Array.prototype.push.apply(spec_vals.children, set[SPEC].map(new_spec_val));
                        return column_set;
                    };
                form.children.push(
                    new form.Block({ // form item_3
                        module: 'og.views.forms.view-definition-user',
                        extras: {user: master.user['userName'], ip: master.user['ipAddress']}
                    }),
                    new form.Block({ // form item_4
                        module: 'og.views.forms.view-definition-colset-tabs',
                        extras: {
                            tabs: master[SETS].reduce(function (acc, set, idx) {
                                set.name = set.name || 'Set ' + (idx + 1);
                                return acc + '<li><a class="og-tab og-js-colset-tab' + (idx ? '' : ' og-active') + '"' +
                                    ' href="#"><div class="og-delete og-js-rem-colset"></div>' +
                                    '<span class="og-js-colset-tab-name">' + set.name + '</span></a></li>';
                            }, '')
                        },
                        handlers: [
                            {type: 'form:load', handler: function () {
                                $(form_id + ' a.og-js-colset-tab').each(function (idx, tab) {
                                    $(form_id + ' div.og-js-colset-holder:gt(0)').hide();
                                });
                            }},
                            // switch colsets
                            {type: 'click', selector: form_id + ' .og-js-colset-tab', handler: function (e) {
                                e.preventDefault();
                                var $target = $(e.target),
                                    $tab = $target.is('.og-js-colset-tab') ? $target
                                        : $target.parents('.og-js-colset-tab:first'),
                                    active_cl = '.og-active', new_cl = '.og-js-new', tab_cl = '.og-js-colset-tab',
                                    clone_cl = '.og-js-clone', rem_cl = '.og-js-rem-colset',
                                    is_remove = $target.is(rem_cl) || $target.parent(rem_cl).length,
                                    is_active = $target.is(active_cl) || $target.parent(active_cl).length,
                                    is_clone = $target.is(clone_cl) || $target.parent(clone_cl).length,
                                    is_new = $target.is(new_cl) || $target.parent(new_cl).length,
                                    index, active_idx;
                                if (is_active) return false;
                                if (is_remove) return false;
                                if (is_new || is_clone) { // add a column set
                                    $(form_id + ' .og-js-empty-colsets').hide();
                                    return (function () {
                                        var $sec = $(form_id + ' section.og-js-colsets'), block, set;
                                        if (is_new) {
                                            set = {name: 'Set ' + (master[SETS].length + 1)}
                                            set[DEFP] = {};
                                        } else {
                                            active_idx = $(form_id + ' ' + tab_cl).index($(form_id + ' ' + active_cl));
                                            if (!~active_idx) return;
                                            set = $.extend(true, {}, form.compile().data[SETS][active_idx]);
                                            set.name = 'Cloned ' + set.name;
                                            set[DEFP] = set[DEFP] || {};
                                            if (set[COLS]) set[COLS] = arr(set[COLS]);
                                            if (set[SPEC]) set[SPEC] = arr(set[SPEC]);
                                        }
                                        master[SETS].push(set);
                                        column_sets.children.push(block = new_col_set(set, master[SETS].length - 1));
                                        block.html(function (html) {
                                            $sec.append($(html));
                                            $(form_id + ' .og-js-colset-tab').removeClass('og-active');
                                            $(form_id + ' .og-js-colset-tabs .og-js-new').before($(
                                                '<li><a class="og-tab og-js-colset-tab og-active" href="#"><div ' +
                                                'class="og-delete og-js-rem-colset"></div>' +
                                                '<span class="og-js-colset-tab-name">' + set.name + '</span></a></li>'
                                            ));
                                            $(form_id + ' .og-js-colset-holder').hide();
                                            $(form_id + ' .og-js-colset-holder:last').show();
                                            block.load();
                                        });
                                    })();
                                }
                                index = $(form_id + ' ' + tab_cl).index($tab);
                                $(form_id + ' .og-js-colset-holder').each(function (idx, set) {
                                    $(set)[idx === index ? 'show' : 'hide']();
                                });
                                $(form_id + ' .og-js-colset-tab').removeClass('og-active');
                                $tab.addClass('og-active');
                            }}
                        ]
                    }),
                    column_sets // form item_5
                );
                Array.prototype.push.apply(column_sets.children, master[SETS].map(new_col_set));
            })();
            form.dom();
        };
    }
});