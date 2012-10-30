/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ag_opts = [], $query = $('.aggregation-selection', $dom.toggle), sel_val, sel_pos, $parent,
                $select, $checkbox, default_sel_txt = 'select aggregation type...', del_s = '.og-icon-delete',
                options_s = '.OG-dropmenu-options', select_s = 'select', checkbox_s = '.og-option :checkbox',
                process_ag_opts = function () {
                    if(ag_opts.length) {
                        var i = 0, arr = [], query;
                        ag_opts.sort(menu.sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                            if (i > 0) arr[i++] = $dom.toggle_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        query = arr.reduce(function (a, v) {return a += v.val ? v.val : v;}, '');
                        $query.html(query);
                    } else $query.text(default_sel_txt);
                },
                remove_entry = function (entry) {
                    if (ag_opts.length === 1) return ag_opts.length = 0;
                    ag_opts.splice(entry, 1);
                },
                reset_query = function () {
                    return $query.text(default_sel_txt), $type_select.val(default_sel_txt).focus(), remove_entry();
                },
                delete_handler = function (entry) {
                    if (menu.opts.length === 1) {
                        if ($checkbox) $checkbox[0].disabled = true;
                        $query.text(default_sel_txt);
                        $select.val(default_sel_txt).focus();
                        return remove_entry();
                    }
                    if($select !== undefined) menu.delete_handler($parent);
                    for (var i = ~entry ? entry : sel_pos, len = ag_opts.length; i < len; ag_opts[i++].pos-=1);
                    if (~entry) {
                        remove_entry(entry);
                        process_ag_opts();
                    }
                },
                add_handler = function () {
                    if (data.length === opts.length) return;
                    menu.add_handler();
                },
                select_handler = function (entry) {
                    if (sel_val === default_sel_txt) {
                        remove_entry(entry);
                        if ($checkbox) $checkbox[0].disabled = true;
                        if (ag_opts.length === 0) return $query.html(default_sel_txt);
                    } else if (~entry) ag_opts[entry].val = sel_val;
                    else {
                        ag_opts.splice(sel_pos, 0, {pos:sel_pos, val:sel_val, required_field:false});
                        $checkbox[0].disabled = false;
                    }
                    process_ag_opts();
                },
                checkbox_handler = function (entry) {
                    if ($checkbox[0].checked && ~entry) ag_opts[entry].required_field = true;
                    else if (!$checkbox[0].checked && ~entry) ag_opts[entry].required_field = false;
                    $checkbox.focus();
                },
                menu_handler = function (event) {
                    var $elem = $(event.srcElement || event.target), entry;
                        $parent = $elem.parents(options_s);
                        $select = $parent.find(select_s);
                        $checkbox = $parent.find(checkbox_s);
                        sel_val = $select.val();
                        sel_pos = $parent.data('pos');
                        entry = ag_opts.pluck('pos').indexOf(sel_pos);
                    if ($elem.is(menu.$dom.add)) return menu.stop(event), add_handler();
                    if ($elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                    if ($elem.is($checkbox)) return checkbox_handler(entry); 
                    if ($elem.is($select)) return select_handler(entry);
                    if ($elem.is('button')) return menu.button_handler($elem.text());
                };
            menu.replay_query = function (config) {
                menu.opts.forEach(function (option, index) {
                    option.remove();
                });
                menu.opts.length = 0;
                ag_opts = [];
                config.aggregators.forEach(function (entry, index) {
                    add_handler();
                    $(select_s, menu.opts[index]).val(entry.val);
                    $(checkbox_s, menu.opts[index])[0].disabled = false;
                    ag_opts[index] = {
                        pos: index,
                        val: entry.val,
                        required_field: entry.required_field   
                    };
                    process_ag_opts();
                });
            };
            menu.get_query = function () {
                return ag_opts.pluck('val');
            };
            menu.destroy = function () {

            };
            $dom.toggle_prefix.append('<span>Aggregated by</span>');
            if ($dom.menu) {
                $dom.menu.on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                         .on('change', 'select', menu_handler);
             }
            return menu;
        };
    }
});