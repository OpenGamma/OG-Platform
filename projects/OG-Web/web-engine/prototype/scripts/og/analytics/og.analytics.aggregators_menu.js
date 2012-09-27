
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ag_opts = [], $ag_selection = $('.aggregation-selection', $dom.title),
                default_sel_txt = 'select aggregation type...', options_s = '.OG-dropmenu-options';
                $dom.title_prefix.append('<span>Aggregated by</span>');
                $dom.title_infix.append('<span>then</span>');
                process_ag_opts = function () {
                    if(ag_opts.length) {
                        var i = 0, arr = [], query, str;
                        ag_opts.sort(sort_ag_opts).forEach(function (entry) {
                            if (i > 0) arr[i++] = $dom.title_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        query = arr.reduce(function (a, v) {return a += v.val ? v.val : v;}, '');
                        $ag_selection.html(query);
                    }
                },
                sort_ag_opts = function (a, b) {
                    if (a.pos < b.pos) return -1;
                    if (a.pos > b.pos) return 1;
                    return 0;
                }
                replace_entry = function (pos, val) {
                    ag_opts[pos].val = val;
                },
                remove_entry = function (pos) {
                    if (ag_opts.length === 1) {
                        return $ag_selection.find('select').val(default_sel_txt).focus(), 
                            $ag_selection.text(default_sel_txt), ag_opts = [];
                    }
                    ag_opts.splice(pos, 1);
                },
                del_handler = function (elem) {
                    var parent = elem.parents(options_s), pos = parent.data('pos'), entry, 
                        checkbox = parent.find('.og-option :checkbox');
                    if (menu.opts.length === 1) {
                        return checkbox.attr('disabled', true), $ag_selection.html(default_sel_txt), 
                            parent.find('select').val(default_sel_txt).focus(),
                            remove_entry();
                    }
                    entry = ag_opts.pluck('pos').indexOf(pos);
                    menu.del_handler(parent); 
                    update_opts_positions(entry !== -1 ? entry : pos);
                    if (entry !== -1) {
                        remove_entry(entry);
                        process_ag_opts();
                    }
                },
                update_opts_positions = function (entry) {
                    for (var i = entry, len = ag_opts.length; i < len;)
                        ag_opts[i++].pos-=1;
                }
                add_handler = function () {
                    if (data.length === opts.length) return;
                    menu.add_handler(); 
                };
                select_handler = function (elem) {
                    var parent = elem.parents(options_s), pos = parent.data('pos'), text, val = elem.val(),
                        entry = ag_opts.pluck('pos').indexOf(pos), checkbox = parent.find('.og-option :checkbox');
                    if (val === default_sel_txt) {
                        remove_entry(entry);
                        checkbox.attr('disabled', true);
                        if (ag_opts.length === 0) return $ag_selection.html(default_sel_txt);
                    }
                    else if (entry !== -1) replace_entry(entry, val);
                    else {
                        ag_opts.splice(pos, 0, {pos:pos, val:val, required_field:false});
                        checkbox.removeAttr('disabled');
                    }
                    process_ag_opts();
                },
                checkbox_handler = function (elem) {
                    var parent = elem.closest(options_s), pos = parent.data('data'),
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    if (elem.checked && entry !== -1) ag_opts[entry].required_field = true;
                    else if (!elem.checked && entry !== -1) ag_opts[entry].required_field = false;
                    elem.focus();
                },
                menu_handler = function (event) {
                    var elem = $(event.target);
                    if (elem.is(menu.$dom.add)) add_handler();
                    if (elem.is('.og-icon-delete')) del_handler(elem);
                    if (elem.is(':checkbox')) checkbox_handler(elem);
                    if (elem.is('select')) select_handler(elem);
                    menu.stop(event);
                };
            $dom.title.on('click', menu.title_handler.bind(menu));
            $dom.menu.on('click', menu_handler).on('change', menu_handler);
            return menu;
        };
    }
});