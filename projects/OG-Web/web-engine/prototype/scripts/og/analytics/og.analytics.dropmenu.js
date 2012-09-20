
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function (module) {
        return function (config) {
            var Dropmenu = this, tmpl = config.tmpl, data = config.data || {}, state = 'closed', opened = false, opts,
                $dom, events = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed'
                },
                focus = function () {
                    return opts[opts.length-1].find('select').first().focus(), opened = true, state = 'focused',
                        Dropmenu.emitEvent(events.focused, [Dropmenu]), Dropmenu;
                },
                open = function () {
                    return $dom.menu.show(), state = 'open', opened = true,
                        $dom.title.addClass('og-active'), Dropmenu.emitEvent(events.opened, [Dropmenu]), Dropmenu;
                },
                close = function () {
                    return ($dom.menu ? $dom.menu.hide() : null), state = 'closed', opened = false,
                        $dom.title.removeClass('og-active'), Dropmenu.emitEvent(events.closed, [Dropmenu]), Dropmenu;
                },
                title_click = function (event) {
                    event.stopPropagation();
                    opened ? close() : (open(), focus());
                },
                menu_click = function (event) {
                    var i = j = 0, ilen = opts.length, jlen, cntrls;
                    for (; i < ilen; i+=1) {
                        for (cntrls = opts[i].find('select, checkbox'), jlen =  cntrls.length; j < jlen; j+=1){
                            if (cntrls.is(':focus')) console.log('focused');
                        }
                    }
                    // $(event.currentTarget).find('select').focus();
                },
                add_click = function (event) {
                    event.stopPropagation();
                    event.preventDefault();
                    var opt, len = opts.length;
                    return opt = $dom.opt_cp.clone(true).data("pos", opts.length), opts.push(opt), $dom.add.focus(),
                            opts[len].find('.number span').text(opts.length), $dom.menu_actions.before(opts[len]);
                },
                del_click = function (event) {
                    event.stopPropagation();
                    var elem = $(event.currentTarget).closest('.OG-dropmenu-options'), data = elem.data();
                    opts.splice(data.pos, 1);
                    elem.remove();
                    update_opt_nums(data.pos);
                    if (data.pos < opts.length) opts[data.pos].find('select').first().focus();
                    else opts[data.pos-1].find('select').first().focus();
                },
                update_opt_nums = function (pos) {
                    for (var i = pos || 0, len = opts.length; i < len;){
                        opts[i].data("pos", i).find('.number span').text(i+=1);
                    }   
                };
            return Dropmenu = new EventEmitter(), Dropmenu.state = function () {return state;},
                Dropmenu.opened = function () {return opened;}, Dropmenu.addListener(events.open, open)
                    .addListener(events.close, close).addListener(events.focus, focus), $dom = {},
                $dom.cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                $dom.title = $('.og-option-title', $dom.cntr).click(title_click),
                $dom.menu = $('.OG-analytics-form-menu', $dom.cntr).click(menu_click),
                $dom.menu_actions = $('.OG-dropmenu-actions', $dom.menu),
                $dom.opt = $('.OG-dropmenu-options', $dom.menu),
                $dom.opt.data("pos", ((opts = []).push($dom.opt), opts.length-1)),
                $dom.add = $('.OG-link-add', $dom.menu).click(add_click),
                $dom.del = $('.og-icon-delete', $dom.opt).click(del_click),
                $dom.opt_cp = $dom.opt.clone(true),
                Dropmenu.$dom = $dom, Dropmenu;
        }
    }
});