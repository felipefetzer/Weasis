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

import java.text.DecimalFormat;

import org.weasis.core.api.Messages;

/**
 * The Class DecFormater.
 * 
 * @author Nicolas Roduit
 */
public class DecFormater {

    private static DecimalFormat df1 = new DecimalFormat("#,##0.##"); // format avec 2 chiffres après la virgule //$NON-NLS-1$
    private static DecimalFormat df2 = new DecimalFormat("#,##0.####"); // format avec 4 chiffres après la virgule //$NON-NLS-1$
    private static DecimalFormat df3 = new DecimalFormat("#,###"); // format avec 0 chiffres après la virgule //$NON-NLS-1$
    private static DecimalFormat df4 = new DecimalFormat("0.####E0"); // format scientifique avec 4 chiffres après la //$NON-NLS-1$
    // virgule
    private static DecimalFormat df5 = new DecimalFormat("###0.#"); // format avec 1 chiffres après la virgule //$NON-NLS-1$
    private static DecimalFormat df6 = new DecimalFormat("#,##0.########"); // format avec 8 chiffres après la virgule //$NON-NLS-1$

    public static String twoDecimal(double val) {
        return df1.format(val);
    }

    public static String oneDecimalUngroup(double val) {
        return df5.format(val);
    }

    public static String zeroDecimal(double val) {
        return df3.format(val);
    }

    public static String fourDecimal(double val) {
        return df2.format(val);
    }

    public static String heightDecimal(double val) {
        return df6.format(val);
    }

    public static String scientificFormat(double val) {
        return df4.format(val);
    }
}