
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
                add_click = function (event) {$menu_actions.before($opts.clone(true)[0]);},
                del_click = function (event) {$(event.srcElement).closest('.OG-dropmenu-options').remove();},
                $cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                $title = $('.og-option-title', $cntr).click(title_click),
                $menu = $('.OG-analytics-form-menu', $cntr),
                $menu_actions = $('.OG-dropmenu-actions', $menu),
                $add = $('.OG-link-add', $menu).click(add_click),
                $opts = $('.OG-dropmenu-options', $menu),
                $del = $('.og-icon-delete', $opts).click(del_click),
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