
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function () { 
        return function (config) {
            var Dropmenu = this, tmpl = config.tmpl, data = config.data || {}, state = 'closed', opened = false,
                focus = function () {
                    return $menu.find('select').first().focus(), opened = true, state = 'focused',
                        Dropmenu.emitEvent(evts.focused, [Dropmenu]), Dropmenu;
                },
                open = function () {
                    return $menu.show().blurkill(close), state = 'open', opened = true,
                        $title.addClass('og-active'), Dropmenu.emitEvent(evts.opened, [Dropmenu]), Dropmenu;
                },
                close = function () {
                    return ($menu ? $menu.hide() : null), state = 'closed', opened = false,
                        $title.removeClass('og-active'), Dropmenu.emitEvent(evts.closed, [Dropmenu]), Dropmenu;
                },
                title_click = function (event) {opened ? close() : (open(), focus());},
                add_click = function (event) {
                    var opt, len = opts.length;
                    return opt = $opt_cp.clone(true).data("pos", opts.length), opts.push(opt),
                            opts[len].find('.number span').text(opts.length), $menu_actions.before(opts[len]);
                },
                del_click = function (event) {
                    var elem = $(event.currentTarget).closest('.OG-dropmenu-options'), data = elem.data();
                    return elem.remove(), opts.splice(data.pos, 1), update_opt_nums(data.pos);
                },
                update_opt_nums = function (pos) {
                    for (var i = pos || 0, len = opts.length; i < len;){
                        opts[i].data("pos", i).find('.number span').text(i+=1);
                    }   
                },
                $cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                $title = $('.og-option-title', $cntr).click(title_click),
                $menu = $('.OG-analytics-form-menu', $cntr),
                $menu_actions = $('.OG-dropmenu-actions', $menu),
                $opt = $('.OG-dropmenu-options', $menu).data("pos", ((opts = []).push($opt), opts.length-1)),
                $add = $('.OG-link-add', $menu).click(add_click),
                $del = $('.og-icon-delete', $opt).click(del_click),
                $opt_cp = $opt.clone(true),
                evts = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed'
                };
            return Dropmenu = new EventEmitter(), Dropmenu.addListener(evts.open, open).addListener(evts.close, close)
                .addListener(evts.focus, focus), Dropmenu.state = function () {return state;},
                Dropmenu.opened = function () {return opened;}, Dropmenu;
        }
    }
});