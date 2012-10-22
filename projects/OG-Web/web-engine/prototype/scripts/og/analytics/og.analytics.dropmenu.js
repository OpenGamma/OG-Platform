$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: [],
    obj: function () {
        var events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed'
            },
            /**
             * TODO AG: Must provide Getters/Setters for static instance properties as these should really be private
             * and not accessible directly via the instance.
             */
            DropMenu = function (config) {
                var menu = this, tmpl = config.tmpl, dummy_s = '<wrapper>', data = config.data || {};
                menu.state = 'closed';
                menu.opened = false;
                menu.data = data;
                menu.events = events;
                menu.$dom = {};
                menu.$dom.cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data)));
                menu.$dom.toggle = $('.og-menu-toggle', menu.$dom.cntr);
                // Start Move to menu class
                menu.$dom.toggle_prefix = $(dummy_s);
                menu.$dom.toggle_infix = $(dummy_s);
                menu.$dom.menu = $('.og-menu', menu.$dom.cntr);
                menu.$dom.menu_actions = $('.og-menu-actions', menu.$dom.menu);
                menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu);
                menu.$dom.opt.data('pos', ((menu.opts = []).push(menu.$dom.opt), menu.opts.length-1));
                menu.$dom.add = $('.OG-link-add', menu.$dom.menu);
                menu.$dom.opt_cp = menu.$dom.opt.clone(true);
                // End Move to menu class
                menu.addListener(events.open, menu.open.bind(menu))
                    .addListener(events.close, menu.close.bind(menu))
                    .addListener(events.focus, menu.focus.bind(menu));
                return menu;
            };
        $.extend(true, DropMenu.prototype, EventEmitter.prototype);
        DropMenu.prototype.constructor = DropMenu;
        DropMenu.prototype.focus = function () {
            return this.opened = true, this.state = 'focused', this.emitEvent(events.focused, [this]), this;
        };
        DropMenu.prototype.open = function () {
            if (this.$dom.menu) {
                return this.$dom.menu.show()/*.blurkill(this.close.bind(this))*/, this.state = 'open', 
                this.opened = true, this.$dom.toggle.addClass('og-active'), this.emitEvent(events.opened, [this]), this;
            }
        };
        DropMenu.prototype.close = function () {
            if (this.$dom.menu) {
                return this.$dom.menu.hide(), this.state = 'closed', this.opened = false,
                    this.$dom.toggle.removeClass('og-active'), this.emitEvent(events.closed, [this]), this;
            }
        };
        DropMenu.prototype.menu_handler = function () {
            throw new Error ('Inheriting class needs to extend this method');
        };
        DropMenu.prototype.toggle_handler = function () {
            return this.opened ? this.close() : (this.open(), this.focus());
        };
        /** START Move to menu class **/
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
        /** END Move to menu class **/
        DropMenu.prototype.stop = function (event) {
            event.stopPropagation();
            event.preventDefault();
        };
        DropMenu.prototype.status = function () {
            return this.state;
        };
        DropMenu.prototype.is_opened = function () {
            return this.opened;
        };
        return DropMenu;
    }
});