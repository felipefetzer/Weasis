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
package org.weasis.dicom.viewer2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.noos.xing.mydoggy.Content;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.SliderCineListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.TagElement;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MiniToolDockable;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomExplorer;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.viewer2d.dockable.DisplayTool;
import org.weasis.dicom.viewer2d.dockable.ImageTool;

public class View2dContainer extends ImageViewerPlugin<DicomImageElement> implements PropertyChangeListener {

    // TODO read and store models
    public static final GridBagLayoutModel VIEWS_2x1m2 =
        new GridBagLayoutModel(View2dContainer.class.getResourceAsStream("/config/layoutModel.xml"), new ImageIcon( //$NON-NLS-1$
            View2dContainer.class.getResource("/icon/22x22/layout1x2_c2.png"))); //$NON-NLS-1$

    public static final GridBagLayoutModel[] MODELS =
        { VIEWS_1x1, VIEWS_1x2, VIEWS_2x1, VIEWS_2x2_f2, VIEWS_2_f1x2, VIEWS_2x1m2, VIEWS_2x2, VIEWS_3x2, VIEWS_3x3,
            VIEWS_4x3, VIEWS_4x4 };

    // Static tools shared by all the View2dContainer instances, tools are registered when a container is selected
    // Do not initialize tools in a static block (order initialization issue with eventManager), use instead a lazy
    // initialization with a method.
    private static PluginTool[] toolPanels;
    private static WtoolBar statusBar = null;
    private static ViewerToolBar<DicomImageElement> toolBar;

    public View2dContainer() {
        this(VIEWS_1x1);
    }

    public View2dContainer(GridBagLayoutModel layoutModel) {
        super(EventManager.getInstance(), layoutModel, View2dFactory.NAME, View2dFactory.ICON, null);
        setSynchView(SynchView.DEFAULT_STACK);
    }

    @Override
    public JMenu fillSelectedPluginMenu(JMenu menuRoot) {
        if (menuRoot != null) {
            menuRoot.removeAll();
            menuRoot.setText(View2dFactory.NAME);
            ActionState viewingAction = eventManager.getAction(ActionW.VIEWINGPROTOCOL);
            if (viewingAction instanceof ComboItemListener) {
                menuRoot.add(((ComboItemListener) viewingAction).createMenu(Messages
                    .getString("View2dContainer.view_protocols"))); //$NON-NLS-1$
            }
            ActionState presetAction = eventManager.getAction(ActionW.PRESET);
            if (presetAction instanceof ComboItemListener) {
                menuRoot.add(((ComboItemListener) presetAction).createMenu(Messages
                    .getString("View2dContainer.presets"))); //$NON-NLS-1$
            }
            ActionState lutAction = eventManager.getAction(ActionW.LUT);
            if (lutAction instanceof ComboItemListener) {
                JMenu menu = ((ComboItemListener) lutAction).createMenu(Messages.getString("View2dContainer.lut")); //$NON-NLS-1$
                ActionState invlutAction = eventManager.getAction(ActionW.INVERSELUT);
                if (invlutAction instanceof ToggleButtonListener) {
                    menu.add(new JSeparator());
                    menu.add(((ToggleButtonListener) invlutAction).createMenu(Messages
                        .getString("View2dContainer.inv_lut"))); //$NON-NLS-1$
                }
                menuRoot.add(menu);
            }
            ActionState stackAction = eventManager.getAction(ActionW.SORTSTACK);
            if (stackAction instanceof ComboItemListener) {
                JMenu menu =
                    ((ComboItemListener) stackAction).createMenu(Messages.getString("View2dContainer.sort_stack")); //$NON-NLS-1$
                ActionState invstackAction = eventManager.getAction(ActionW.INVERSESTACK);
                if (invstackAction instanceof ToggleButtonListener) {
                    menu.add(new JSeparator());
                    menu.add(((ToggleButtonListener) invstackAction).createMenu(Messages
                        .getString("View2dContainer.inv_stack"))); //$NON-NLS-1$
                }
                menuRoot.add(menu);
            }
            ActionState rotateAction = eventManager.getAction(ActionW.ROTATION);
            if (rotateAction instanceof SliderChangeListener) {
                menuRoot.add(new JSeparator());
                JMenu menu = new JMenu(Messages.getString("View2dContainer.orientation")); //$NON-NLS-1$
                JMenuItem menuItem = new JMenuItem(Messages.getString("ResetTools.reset")); //$NON-NLS-1$
                final SliderChangeListener rotation = (SliderChangeListener) rotateAction;
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rotation.setValue(0);
                    }
                });
                menu.add(menuItem);
                menuItem = new JMenuItem(Messages.getString("View2dContainer.-90")); //$NON-NLS-1$
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rotation.setValue((rotation.getValue() - 90 + 360) % 360);
                    }
                });
                menu.add(menuItem);
                menuItem = new JMenuItem(Messages.getString("View2dContainer.+90")); //$NON-NLS-1$
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rotation.setValue((rotation.getValue() + 90) % 360);
                    }
                });
                menu.add(menuItem);
                menuItem = new JMenuItem(Messages.getString("View2dContainer.+180")); //$NON-NLS-1$
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rotation.setValue((rotation.getValue() + 180) % 360);
                    }
                });
                menu.add(menuItem);
                ActionState flipAction = eventManager.getAction(ActionW.FLIP);
                if (flipAction instanceof ToggleButtonListener) {
                    menu.add(new JSeparator());
                    menu.add(((ToggleButtonListener) flipAction).createMenu(Messages
                        .getString("View2dContainer.flip_h"))); //$NON-NLS-1$
                    menuRoot.add(menu);
                }
            }
            menuRoot.add(new JSeparator());
            menuRoot.add(ResetTools.createUnregisteredJMenu());

        }
        return menuRoot;
    }

    @Override
    public PluginTool[] getToolPanel() {
        if (toolPanels == null) {
            toolPanels = new PluginTool[3];
            toolPanels[0] = new MiniToolDockable(Messages.getString("View2dContainer.mini"), null) { //$NON-NLS-1$

                    @Override
                    public SliderChangeListener[] getActions() {

                        ArrayList<SliderChangeListener> listeners = new ArrayList<SliderChangeListener>(3);
                        ActionState seqAction = eventManager.getAction(ActionW.SCROLL_SERIES);
                        if (seqAction instanceof SliderChangeListener) {
                            listeners.add((SliderChangeListener) seqAction);
                        }
                        ActionState zoomAction = eventManager.getAction(ActionW.ZOOM);
                        if (zoomAction instanceof SliderChangeListener) {
                            listeners.add((SliderChangeListener) zoomAction);
                        }
                        ActionState rotateAction = eventManager.getAction(ActionW.ROTATION);
                        if (rotateAction instanceof SliderChangeListener) {
                            listeners.add((SliderChangeListener) rotateAction);
                        }
                        return listeners.toArray(new SliderChangeListener[listeners.size()]);
                    }
                };
            toolPanels[0].setHide(false);
            toolPanels[0].registerToolAsDockable();
            toolPanels[1] = new ImageTool(Messages.getString("View2dContainer.image_tools"), null); //$NON-NLS-1$
            toolPanels[1].registerToolAsDockable();
            toolPanels[2] = new DisplayTool(DisplayTool.BUTTON_NAME, null);
            toolPanels[2].registerToolAsDockable();
            eventManager.addSeriesViewerListener((SeriesViewerListener) toolPanels[2]);
            // toolPanels[3] = new DrawToolsDockable();
        }
        return toolPanels;
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            eventManager.setSelectedView2dContainer(this);

            MediaSeries<DicomImageElement> series = selectedImagePane.getSeries();
            if (series != null) {
                DataExplorerView dicomView = UIManager.getExplorerplugin(DicomExplorer.NAME);
                if (dicomView == null || !(dicomView.getDataExplorerModel() instanceof DicomModel)) {
                    return;
                }
                DicomModel model = (DicomModel) dicomView.getDataExplorerModel();
                model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.Select, this, null, series));
            }

        } else {
            eventManager.setSelectedView2dContainer(null);
        }
    }

    @Override
    public void close() {
        super.close();
        View2dFactory.closeSeriesViewer(this);

        GuiExecutor.instance().execute(new Runnable() {

            @Override
            public void run() {
                for (DefaultView2d v : view2ds) {
                    v.dispose();
                }
            }
        });

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent event = (ObservableEvent) evt;
            ObservableEvent.BasicAction action = event.getActionCommand();
            Object newVal = event.getNewValue();
            if (newVal instanceof SeriesEvent) {
                SeriesEvent event2 = (SeriesEvent) newVal;
                if (ObservableEvent.BasicAction.Add.equals(action)) {
                    SeriesEvent.Action action2 = event2.getActionCommand();
                    Object dcmSeries = event2.getSource();
                    Object param = event2.getParam();

                    if (SeriesEvent.Action.AddImage.equals(action2)) {
                        if (dcmSeries instanceof DicomSeries) {
                            DicomSeries dcm = (DicomSeries) dcmSeries;
                            DefaultView2d view2DPane = eventManager.getSelectedViewPane();
                            if (view2DPane.getSeries() == dcm) {
                                ActionState seqAction = eventManager.getAction(ActionW.SCROLL_SERIES);
                                if (seqAction instanceof SliderCineListener) {
                                    SliderCineListener sliceAction = (SliderCineListener) seqAction;
                                    int frameIndex = sliceAction.getValue();
                                    if (param instanceof Integer) {
                                        // Model contains display value, value-1 is the index
                                        // value of a sequence
                                        frameIndex = (Integer) param <= frameIndex ? frameIndex + 1 : frameIndex;
                                        int size = dcm.size();
                                        // When the action AddImage add the first image of the
                                        // series
                                        if (frameIndex > size) {
                                            frameIndex = size;
                                            // add again the series for registering listeners
                                            // (require at least one image)
                                            view2DPane.setSeries(dcm, -1);
                                        }
                                        sliceAction.setMinMaxValue(1, size, frameIndex);
                                    }
                                }
                            }
                        }
                    } else if (SeriesEvent.Action.loadImageInMemory.equals(action2)) {
                        if (dcmSeries instanceof DicomSeries) {
                            DicomSeries dcm = (DicomSeries) dcmSeries;
                            for (DefaultView2d<DicomImageElement> v : view2ds) {
                                if (dcm == v.getSeries()) {
                                    v.repaint(v.getInfoLayer().getPreloadingProgressBound());
                                }
                            }
                        }
                    }
                }
            } else if (ObservableEvent.BasicAction.Remove.equals(action)) {
                if (newVal instanceof DicomSeries) {
                    DicomSeries dicomSeries = (DicomSeries) newVal;
                    for (DefaultView2d<DicomImageElement> v : view2ds) {
                        MediaSeries<DicomImageElement> s = v.getSeries();
                        if (dicomSeries.equals(s)) {
                            v.setSeries(null);
                        }
                    }
                } else if (newVal instanceof MediaSeriesGroup) {
                    MediaSeriesGroup group = (MediaSeriesGroup) newVal;
                    // Patient Group
                    if (TagElement.PatientPseudoUID.equals(group.getTagID())) {
                        if (group.equals(getGroupID())) {
                            // Close the content of the plug-in
                            close();
                            Content content =
                                UIManager.toolWindowManager.getContentManager().getContent(this.getDockableUID());
                            if (content != null) {
                                // Close the window
                                UIManager.toolWindowManager.getContentManager().removeContent(content);
                            }
                        }
                    }
                    // Study Group
                    else if (TagElement.StudyInstanceUID.equals(group.getTagID())) {
                        if (event.getSource() instanceof DicomModel) {
                            DicomModel model = (DicomModel) event.getSource();
                            for (MediaSeriesGroup s : model.getChildren(group)) {
                                for (DefaultView2d<DicomImageElement> v : view2ds) {
                                    MediaSeries series = v.getSeries();
                                    if (s.equals(series)) {
                                        v.setSeries(null);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public DefaultView2d<DicomImageElement> createDefaultView() {
        return new View2d(eventManager);
    }

    @Override
    public JComponent createUIcomponent(String clazz) {
        if (View2d.class.getName().equals(clazz)) {
            return createDefaultView();
        }
        try {
            // FIXME use classloader.loadClass or injection
            Class cl = Class.forName(clazz);
            JComponent component = (JComponent) cl.newInstance();
            if (component instanceof SeriesViewerListener) {
                eventManager.addSeriesViewerListener((SeriesViewerListener) component);
            }
            return component;

        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }

        catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (ClassCastException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized WtoolBar getStatusBar() {
        return statusBar;
    }

    @Override
    public synchronized WtoolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new ViewerToolBar<DicomImageElement>(eventManager);
        }
        return toolBar;
    }

    @Override
    public Action[] getExportActions() {
        if (selectedImagePane != null) {
            return new Action[] { selectedImagePane.getExportToClipboardAction() };
        }
        return null;
    }
}