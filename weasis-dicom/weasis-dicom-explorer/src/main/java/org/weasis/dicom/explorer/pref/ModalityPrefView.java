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
package org.weasis.dicom.explorer.pref;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.media.data.TagElement;
import org.weasis.dicom.codec.InfoViewElementPanel;
import org.weasis.dicom.codec.display.CornerDisplay;
import org.weasis.dicom.codec.display.CornerInfoData;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.display.ModalityInfoData;
import org.weasis.dicom.explorer.Messages;

public class ModalityPrefView extends AbstractItemDialogPage implements DragGestureListener {

    public final static HashMap<Integer, TagElement> tagList = new HashMap<Integer, TagElement>();
    static {
        // Patient
        fillMap(TagElement.PatientID);
        fillMap(TagElement.PatientName);
        fillMap(TagElement.PatientBirthDate);
        fillMap(TagElement.PatientBirthTime);
        fillMap(TagElement.PatientSex);
        fillMap(TagElement.IssuerOfPatientID);
        fillMap(TagElement.PatientComments);

        // Study
        fillMap(TagElement.StudyID);
        fillMap(TagElement.StudyDate);
        fillMap(TagElement.StudyTime);
        fillMap(TagElement.StudyDescription);
        fillMap(TagElement.AccessionNumber);
        fillMap(TagElement.ReferringPhysicianName);
        fillMap(TagElement.ModalitiesInStudy);
        fillMap(TagElement.NumberOfStudyRelatedInstances);
        fillMap(TagElement.NumberOfStudyRelatedSeries);
        fillMap(TagElement.StudyStatusID);
        fillMap(TagElement.ProcedureCodeSequence);

        // Series
        fillMap(TagElement.SeriesInstanceUID);
        fillMap(TagElement.Modality);
        fillMap(TagElement.SeriesDate);
        fillMap(TagElement.SeriesDescription);
        fillMap(TagElement.RetrieveAETitle);
        fillMap(TagElement.InstitutionName);
        fillMap(TagElement.InstitutionalDepartmentName);
        fillMap(TagElement.StationName);
        fillMap(TagElement.Manufacturer);
        fillMap(TagElement.ManufacturerModelName);
        fillMap(TagElement.ReferencedPerformedProcedureStepSequence);
        fillMap(TagElement.SeriesNumber);
        fillMap(TagElement.PreferredPlaybackSequencing);
        fillMap(TagElement.CineRate);
        fillMap(TagElement.Laterality);
        fillMap(TagElement.BodyPartExamined);
        fillMap(TagElement.NumberOfSeriesRelatedInstances);
        fillMap(TagElement.PerformedProcedureStepStartDate);
        fillMap(TagElement.PerformedProcedureStepStartTime);
        fillMap(TagElement.RequestAttributesSequence);

        // Instance
        fillMap(TagElement.ImageType);
        fillMap(TagElement.ImageComments);
        fillMap(TagElement.ContrastBolusAgent);
        fillMap(TagElement.TransferSyntaxUID);
        fillMap(TagElement.InstanceNumber);
        fillMap(TagElement.SOPInstanceUID);
        fillMap(TagElement.SOPClassUID);
        fillMap(TagElement.ScanningSequence);
        fillMap(TagElement.SequenceVariant);
        fillMap(TagElement.ScanOptions);
        fillMap(TagElement.RepetitionTime);
        fillMap(TagElement.EchoTime);
        fillMap(TagElement.InversionTime);
        fillMap(TagElement.EchoNumbers);
        fillMap(TagElement.GantryDetectorTilt);
        fillMap(TagElement.ConvolutionKernel);
        fillMap(TagElement.FlipAngle);
        fillMap(TagElement.SliceLocation);
        fillMap(TagElement.SliceThickness);
        fillMap(TagElement.AcquisitionDate);
        fillMap(TagElement.AcquisitionTime);

        fillMap(TagElement.ImagePositionPatient);
        fillMap(TagElement.ImageOrientationPatient);
        fillMap(TagElement.ImageOrientationPlane);
        fillMap(TagElement.PixelSpacing);
        fillMap(TagElement.WindowWidth);
        fillMap(TagElement.WindowCenter);

        fillMap(TagElement.RescaleSlope);
        fillMap(TagElement.RescaleIntercept);

        fillMap(TagElement.SmallestImagePixelValue);
        fillMap(TagElement.LargestImagePixelValue);
        fillMap(TagElement.PixelPaddingValue);
        fillMap(TagElement.NumberOfFrames);
        fillMap(TagElement.PixelPaddingRangeLimit);
        fillMap(TagElement.OverlayRows);

        fillMap(TagElement.SamplesPerPixel);
        fillMap(TagElement.MonoChrome);
        fillMap(TagElement.PhotometricInterpretation);

        fillMap(TagElement.Rows);
        fillMap(TagElement.Columns);
        fillMap(TagElement.BitsAllocated);
        fillMap(TagElement.BitsStored);
        fillMap(TagElement.PixelRepresentation);
    }
    private JList list;
    private DragSource ds;
    private JComboBox cornercomboBox;
    private JComboBox modalitycomboBox;
    private JList jListPosition;
    private JScrollPane scrollPane_1;

    private static ModalityInfoData[] infos = null;

    private final InfoViewElementPanel cornerView = new InfoViewElementPanel();

    private transient final ItemListener changeViewListener = new ItemListener() {

        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                changCornerView();
            }
        }
    };

    public ModalityPrefView() {
        setTitle(Messages.getString("ModalityPrefView.annotations")); //$NON-NLS-1$
        initNorthPanel();
        initGUI();
        jListPosition.setListData(new Vector(tagList.values()));
        changCornerView();
    }

    private static void fillMap(TagElement tag) {
        tagList.put(tag.getId(), tag);
    }

    public void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        jListPosition = new JList();
        // jListPosition.setPreferredSize(new Dimension(50, 30));
        jListPosition.setBorder(new EmptyBorder(5, 5, 5, 5));
        jListPosition.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(jListPosition, DnDConstants.ACTION_COPY, this);

        final JPanel panel_corner = new JPanel();
        final FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panel_corner.setLayout(flowLayout);
        add(panel_corner);

        final JLabel cornerLabel = new JLabel();
        cornerLabel.setText(Messages.getString("ModalityPrefView.corner")); //$NON-NLS-1$
        panel_corner.add(cornerLabel);

        cornercomboBox = new JComboBox(CornerDisplay.values());
        cornercomboBox.addItemListener(changeViewListener);
        panel_corner.add(cornercomboBox);
        final JPanel panel_1 = new JPanel();
        final GridBagLayout gridBagLayout = new GridBagLayout();

        panel_1.setLayout(gridBagLayout);
        add(panel_1);

        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel_1.add(cornerView, gridBagConstraints);

        final JTabbedPane tabbedPane = new JTabbedPane();
        scrollPane_1 = new JScrollPane();
        scrollPane_1.setPreferredSize(new Dimension(70, 30));
        final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
        gridBagConstraints_1.weighty = 1.0;
        gridBagConstraints_1.weightx = 0.5;
        gridBagConstraints_1.fill = GridBagConstraints.BOTH;
        gridBagConstraints_1.gridx = 1;
        gridBagConstraints_1.gridy = 0;
        scrollPane_1.setViewportView(jListPosition);
        tabbedPane.add(Messages.getString("ModalityPrefView.dcm_el"), scrollPane_1); //$NON-NLS-1$
        panel_1.add(tabbedPane, gridBagConstraints_1);

        final JScrollPane scrollPane = new JScrollPane();
        tabbedPane.addTab(Messages.getString("ModalityPrefView.other_val"), null, scrollPane, null); //$NON-NLS-1$
        list = new JList();
        scrollPane.setPreferredSize(new Dimension(70, 30));
        scrollPane.setViewportView(list);

        final JButton propertiesButton = new JButton();
        propertiesButton.setText(Messages.getString("ModalityPrefView.prop")); //$NON-NLS-1$
        final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
        gridBagConstraints_4.insets = new Insets(5, 0, 5, 0);
        gridBagConstraints_4.gridy = 1;
        gridBagConstraints_4.gridx = 0;
        panel_1.add(propertiesButton, gridBagConstraints_4);

        final JButton jButtonFormat = new JButton();
        final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
        gridBagConstraints_3.insets = new Insets(5, 0, 5, 0);
        gridBagConstraints_3.gridy = 1;
        gridBagConstraints_3.gridx = 1;
        panel_1.add(jButtonFormat, gridBagConstraints_3);
        jButtonFormat.setText(Messages.getString("ModalityPrefView.format")); //$NON-NLS-1$
        jButtonFormat.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TagElement element = (TagElement) jListPosition.getSelectedValue();
                if (element != null) {
                    String result =
                        JOptionPane.showInputDialog(ModalityPrefView.this, element.getName(), element.getFormat());
                    if (result != null) {
                        if (result.indexOf("$V") != -1) { //$NON-NLS-1$
                            element.setFormat(result);
                        } else if (result.trim().equals("")) { //$NON-NLS-1$
                            element.setFormat(null);
                        } else {
                            JOptionPane.showMessageDialog(ModalityPrefView.this,
                                Messages.getString("ModalityPrefView.not_valid"), Messages.getString("ModalityPrefView.disp_format"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
        });
        jButtonFormat.setToolTipText(Messages.getString("ModalityPrefView.elem_disp")); //$NON-NLS-1$

    }

    public void initNorthPanel() {
        final JPanel panel_modality = new JPanel();
        final FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panel_modality.setLayout(flowLayout);
        add(panel_modality);

        final JLabel modalityLabel = new JLabel();
        modalityLabel.setText(Messages.getString("ModalityPrefView.template")); //$NON-NLS-1$
        panel_modality.add(modalityLabel);
        modalitycomboBox = new JComboBox(loadDefaultModalityDisplayPreferences());
        modalitycomboBox.addItemListener(changeViewListener);
        panel_modality.add(modalitycomboBox);

    }

    public static synchronized ModalityInfoData[] loadDefaultModalityDisplayPreferences() {
        if (infos == null) {
            Modality[] modalities = Modality.values();
            infos = new ModalityInfoData[modalities.length - 1];
            for (int i = 0; i < infos.length; i++) {
                infos[i] = new ModalityInfoData(modalities[i + 1]);
                TagElement[] disElements = infos[i].getCornerInfo(CornerDisplay.TOP_LEFT).getInfos();
                disElements[0] = TagElement.PatientName;
                disElements[1] = TagElement.PatientBirthDate;
                disElements[2] = TagElement.PatientID;
                disElements[2].setFormat(Messages.getString("ModalityPrefView.id")); //$NON-NLS-1$
                disElements[3] = TagElement.PatientSex;
                disElements[3].setFormat(Messages.getString("ModalityPrefView.sex")); //$NON-NLS-1$

                disElements = infos[i].getCornerInfo(CornerDisplay.TOP_RIGHT).getInfos();
                disElements[0] = TagElement.InstitutionName;
                disElements[1] = TagElement.StudyID;
                disElements[1].setFormat(Messages.getString("ModalityPrefView.study")); //$NON-NLS-1$
                disElements[2] = TagElement.StudyDescription;
                disElements[2].setFormat(Messages.getString("ModalityPrefView.study_des")); //$NON-NLS-1$
                disElements[3] = TagElement.AcquisitionDate;
                disElements[3].setFormat(Messages.getString("ModalityPrefView.acq")); //$NON-NLS-1$
                disElements[4] = TagElement.AcquisitionTime;
                disElements[4].setFormat(Messages.getString("ModalityPrefView.acq")); //$NON-NLS-1$

                disElements = infos[i].getCornerInfo(CornerDisplay.BOTTOM_RIGHT).getInfos();
                disElements[2] = TagElement.SeriesNumber;
                disElements[2].setFormat(Messages.getString("ModalityPrefView.series_nb")); //$NON-NLS-1$
                disElements[3] = TagElement.ContrastBolusAgent;
                disElements[4] = TagElement.SeriesDescription;
                disElements[4].setFormat(Messages.getString("ModalityPrefView.series_desc")); //$NON-NLS-1$
                disElements[5] = TagElement.SliceThickness;
                disElements[5].setFormat(Messages.getString("ModalityPrefView.thick")); //$NON-NLS-1$
                disElements[6] = TagElement.SliceLocation;
                disElements[6].setFormat(Messages.getString("ModalityPrefView.location")); //$NON-NLS-1$
            }
        }
        return infos;
    }

    public static ModalityInfoData getModlatityInfos(Modality mod) {
        if (infos == null) {
            loadDefaultModalityDisplayPreferences();
        }
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getModality().equals(mod)) {
                return infos[i];
            }
        }
        return infos[0];
    }

    @Override
    public void closeAdditionalWindow() {

    }

    @Override
    public void resetoDefaultValues() {
    }

    public void changCornerView() {
        ModalityInfoData info = (ModalityInfoData) modalitycomboBox.getSelectedItem();
        if (info != null) {
            CornerInfoData corner = info.getCornerInfo((CornerDisplay) cornercomboBox.getSelectedItem());
            cornerView.setCorner(corner);
        }
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        Component component = dge.getComponent();
        Transferable t = null;
        if (component instanceof JList) {
            t = (TagElement) ((JList) component).getSelectedValue();
        }
        if (t != null) {
            try {
                dge.startDrag(null, t, null);
                return;
            } catch (RuntimeException re) {
            }
        }
    }

    public static TagElement getInfoElement(int id) {
        return tagList.get(id);
    }

}