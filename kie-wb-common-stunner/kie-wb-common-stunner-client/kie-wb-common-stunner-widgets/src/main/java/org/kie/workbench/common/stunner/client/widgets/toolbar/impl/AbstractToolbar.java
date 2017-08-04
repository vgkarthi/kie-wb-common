/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.client.widgets.toolbar.impl;

import java.util.LinkedList;
import java.util.List;

import org.kie.workbench.common.stunner.client.widgets.toolbar.Toolbar;
import org.kie.workbench.common.stunner.client.widgets.toolbar.ToolbarCommand;
import org.kie.workbench.common.stunner.client.widgets.toolbar.ToolbarView;
import org.kie.workbench.common.stunner.client.widgets.toolbar.command.AbstractToolbarCommand;
import org.kie.workbench.common.stunner.client.widgets.toolbar.item.AbstractToolbarItem;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;

public abstract class AbstractToolbar<S extends ClientSession> implements Toolbar<S> {

    private final List<ToolbarCommand<? super S>> commands = new LinkedList<>();
    private final List<AbstractToolbarItem<S>> items = new LinkedList<>();
    private final ToolbarView<AbstractToolbar> view;

    protected AbstractToolbar(final ToolbarView<AbstractToolbar> view) {
        this.view = view;
        view.init(this);
    }

    protected abstract AbstractToolbarItem<S> newToolbarItem();

    @SuppressWarnings("unchecked")
    public void initialize(final S session) {
        commands.stream()
                .forEach(command -> {
                    final AbstractToolbarItem<S> toolbarItem = newToolbarItem();
                    toolbarItem.setUUID(((AbstractToolbarCommand) command).getUuid());
                    getView().addItem(toolbarItem.asWidget());
                    items.add(toolbarItem);
                    toolbarItem.show(this,
                                     session,
                                     (AbstractToolbarCommand<S, ?>) command,
                                     command::execute);
                });
        afterDraw();
        show();
    }

    public void addCommand(final ToolbarCommand<? super S> item) {
        commands.add(item);
    }

    @Override
    public void disable(final ToolbarCommand<S> command) {
        final AbstractToolbarItem<S> item = getItem(command);
        if (null != item) {
            item.disable();
        }
    }

    @Override
    public void enable(final ToolbarCommand<S> command) {
        final AbstractToolbarItem<S> item = getItem(command);
        if (null != item) {
            item.enable();
        }
    }

    @Override
    public void clear() {
        commands.clear();
        items.clear();
        getView().clear();
    }

    @Override
    public void destroy() {
        commands.clear();
        items.clear();
        getView().destroy();
    }

    @Override
    public ToolbarView<? extends Toolbar> getView() {
        return view;
    }

    protected ToolbarCommand<? super S> getCommand(final int index) {
        return commands.get(index);
    }

    @SuppressWarnings("unchecked")
    protected AbstractToolbarItem<S> getItem(final ToolbarCommand<?> command) {
        return items.stream()
                .filter(command::equals)
                .findFirst()
                .orElse(null);
    }

    private void afterDraw() {
        commands.forEach(ToolbarCommand::refresh);
    }

    private void show() {
        getView().show();
    }
}