$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: ['og.common.util.ui.DropMenu'],
    obj: function () {
        var events = {
                queryselected: 'dropmenu:queryselected',
                querycancelled: 'dropmenu:querycancelled'
            },
            DropMenu = function (config) {
                /**
                * TODO AG: Must provide Getters/Setters for static instance properties as these should 
                * really be private and not accessible directly via the instance.
                */
            var menu = new og.common.util.ui.DropMenu(config),
                menu.$dom.toggle_prefix = $(dummy_s);
                menu.$dom.toggle_infix = $(dummy_s).append('<span>then</span>');
                menu.$dom.menu_actions = $('.og-menu-actions', menu.$dom.menu);
                menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu);
                menu.$dom.opt.data('pos', ((menu.opts = []).push(menu.$dom.opt), menu.opts.length-1));
                menu.$dom.add = $('.OG-link-add', menu.$dom.menu);
                menu.$dom.opt_cp = menu.$dom.opt.clone(true);
                if ($dom.toggle) $dom.toggle.on('click', menu.toggle_handler);
                if ($dom.menu) {
                    $dom.menu.on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu.menu_handler)
                             .on('change', 'select', menu.menu_handler);
                 }
                return menu;
            };
        DropMenu.prototype.toggle_handler = function (event){
            this.opts[this.opts.length-1].find('select').first().focus();
            this.toggle_handler();
        };
        DropMenu.prototype.add_handler = function () {
            return len = this.opts.length, opt = this.$dom.opt_cp.clone(true).data("pos", this.opts.length),
                this.opts.push(opt), this.$dom.add.focus(), this.opts[len].find('.number span').text(this.opts.length), 
                this.$dom.menu_actions.before(this.opts[len]);
        };
        DropMenu.prototype.del_handler = function (elem) {
            var data = elem.data();
            if (this.opts.length === 1) return;
            this.opts.splice(data.pos, 1);
            elem.remove();
            this.update_opt_nums(data.pos);
            if (data.pos < this.opts.length) this.opts[data.pos].find('select').first().focus();
            else this.opts[data.pos-1].find('select').first().focus();
        };
        DropMenu.prototype.update_opt_nums = function (pos) {
            for (var i = pos || 0, len = this.opts.length; i < len;)
                this.opts[i].data('pos', i).find('.number span').text(i+=1);
        };
        DropMenu.prototype.button_handler = function (val) {
            if (val === 'OK') menu.emitEvent(menu.events.close).emitEvent(events.queryselected, [menu]);
            else if (val === 'Cancel') menu.emitEvent(menu.events.close).emitEvent(events.querycancelled, [menu]);
        };
        return DropMenu;
    }
});