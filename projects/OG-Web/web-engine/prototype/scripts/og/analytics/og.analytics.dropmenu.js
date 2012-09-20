
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function () { 
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
                    return $dom.menu.find('select').first().focus(), opened = true, state = 'focused',
                        Dropmenu.emitEvent(events.focused, [Dropmenu]), Dropmenu;
                },
                open = function () {
                    return $dom.menu.show().blurkill(close), state = 'open', opened = true,
                        $dom.title.addClass('og-active'), Dropmenu.emitEvent(events.opened, [Dropmenu]), Dropmenu;
                },
                close = function () {
                    return ($dom.menu ? $dom.menu.hide() : null), state = 'closed', opened = false,
                        $dom.title.removeClass('og-active'), Dropmenu.emitEvent(events.closed, [Dropmenu]), Dropmenu;
                },
                title_click = function (event) {opened ? close() : (open(), focus());},
                add_click = function (event) {
                    var opt, len = opts.length;
                    return opt = $dom.opt_cp.clone(true).data("pos", opts.length), opts.push(opt),
                            opts[len].find('.number span').text(opts.length), $dom.menu_actions.before(opts[len]);
                },
                del_click = function (event) {
                    var elem = $(event.currentTarget).closest('.OG-dropmenu-options'), data = elem.data();
                    return elem.remove(), opts.splice(data.pos, 1), update_opt_nums(data.pos);
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
                $dom.menu = $('.OG-analytics-form-menu', $dom.cntr),
                $dom.menu_actions = $('.OG-dropmenu-actions', $dom.menu),
                $dom.opt = $('.OG-dropmenu-options', $dom.menu).data("pos", ((opts = []).push($dom.opt), opts.length-1)),
                $dom.add = $('.OG-link-add', $dom.menu).click(add_click),
                $dom.del = $('.og-icon-delete', $dom.opt).click(del_click),
                $dom.opt_cp = $dom.opt.clone(true),
                Dropmenu.$dom = $dom, Dropmenu;
        }
    }
});