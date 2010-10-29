/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.gui.util;

import java.util.ArrayList;

import javax.swing.DefaultButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class ComboItemListener implements ListDataListener, ChangeListener, ActionState {

    protected final ActionW action;
    protected final ArrayList<JComponent> itemList;
    protected final DefaultComboBoxModel model;
    private boolean enable;

    public ComboItemListener(ActionW action, Object[] objects) {
        super();
        this.action = action;
        enable = true;
        itemList = new ArrayList<JComponent>();
        model = new DefaultComboBoxModel(objects);
        model.addListDataListener(this);
    }

    public void contentsChanged(ListDataEvent e) {
        itemStateChanged(model.getSelectedItem());
    }

    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }

    public void stateChanged(ChangeEvent evt) {
    }

    public void enableAction(boolean enabled) {
        this.enable = enabled;
        for (JComponent c : itemList) {
            c.setEnabled(enabled);
        }
    }

    @Override
    public String toString() {
        return action.getTitle();
    }

    public ActionW getActionW() {
        return action;
    }

    public abstract void itemStateChanged(Object object);

    /**
     * Register a component and add at the same the ItemListener
     * 
     * @param slider
     */
    public void registerComponent(JComponent component) {
        if (!itemList.contains(component)) {
            itemList.add(component);
            if (component instanceof JComboBox) {
                ((JComboBox) component).setModel(model);
                ((JComboBox) component).setEnabled(enable);
            }
            if (component instanceof JMenu) {
                setUnregisteredRadioMenu((JMenu) component);
            }
        }
    }

    public void unregisterJComponent(JComponent component) {
        itemList.remove(component);
        if (component instanceof JComboBox) {
            ((JComboBox) component).setModel(new DefaultComboBoxModel());
        }
        if (component instanceof JMenu) {
            ((JMenu) component).setModel(new DefaultButtonModel());
        }
    }

    public synchronized Object[] getAllItem() {
        Object[] array = new Object[model.getSize()];
        for (int i = 0; i < array.length; i++) {
            array[i] = model.getElementAt(i);
        }
        return array;
    }

    public synchronized Object getSelectedItem() {
        return model.getSelectedItem();
    }

    public synchronized void setSelectedItem(Object object) {
        model.setSelectedItem(object);
    }

    public synchronized void setSelectedItemWithoutTriggerAction(Object object) {
        model.removeListDataListener(this);
        model.setSelectedItem(object);
        model.addListDataListener(this);
    }

    public synchronized void setDataList(Object[] objects) {
        if (objects != null && objects.length > 0) {
            Object oldSelection = model.getSelectedItem();
            model.removeListDataListener(this);
            model.removeAllElements();
            boolean oldSelectionStillExist = false;

            for (Object object : objects) {
                model.addElement(object);
                if (object == oldSelection) {
                    oldSelectionStillExist = true;
                }
            }

            for (JComponent c : itemList) {
                if (c instanceof JMenu) {
                    setUnregisteredRadioMenu((JMenu) c);
                }
            }

            model.addListDataListener(this);
            if (oldSelection != null && oldSelectionStillExist) {
                model.setSelectedItem(oldSelection);
            } else if (objects[0] == model.getSelectedItem()) {
                itemStateChanged(model.getSelectedItem());
            } else {
                model.setSelectedItem(objects[0]);
            }
        }
    }

    protected JMenu setUnregisteredRadioMenu(JMenu menu) {
        GroupRadioMenu radioMenu = new GroupRadioMenu(model);
        return radioMenu.fillMenu(menu);
    }

    public JMenu createUnregisteredRadioMenu(String title) {
        GroupRadioMenu radioMenu = new GroupRadioMenu(model);
        JMenu menu = radioMenu.createMenu(title);
        if (!enable) {
            menu.setEnabled(false);
        }
        return menu;
    }

    public JComboBox createCombo() {
        final JComboBox combo = new JComboBox();
        registerComponent(combo);
        return combo;
    }

    public JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        registerComponent(menu);
        return menu;
    }
}