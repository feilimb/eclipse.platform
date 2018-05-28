/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ui.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tips.core.internal.TipManager;
import org.eclipse.tips.ui.internal.util.ResourceManager;

/**
 * The dialog containing the tips.
 *
 */
@SuppressWarnings("restriction")
public class TipDialog extends Dialog {

	/**
	 * When passed as style, the default style will be used which is
	 * <p>
	 * (SWT.RESIZE | SWT.SHELL_TRIM)
	 */
	public static final int DEFAULT_STYLE = -1;
	private TipManager fTipManager;
	private TipComposite fTipComposite;
	private int fShellStyle;
	private IDialogSettings fDialogSettings;

	public TipDialog(Shell parentShell, TipManager tipManager, int shellStyle, IDialogSettings dialogSettings) {
		super(parentShell);
		fTipManager = tipManager;
		fDialogSettings = dialogSettings;
		fShellStyle = (shellStyle == DEFAULT_STYLE) ? (SWT.RESIZE | SWT.SHELL_TRIM) : shellStyle;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return fDialogSettings;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		fixLayout(parent);
		Composite area = (Composite) super.createDialogArea(parent);
		fixLayout(area);
		fTipComposite = new TipComposite(area, SWT.NONE);
		fixLayout(fTipComposite);
		getShell().setText(Messages.TipDialog_0);
		fTipComposite.addDisposeListener(event -> close());
		return area;
	}

//	private Point getLocation() {
//		Shell parentShell = getParentShell();
//		if (parentShell == null) {
//			return null;
//		}
//		int absx = parentShell.getSize().x / 2 - getShell().getSize().x / 2;
//		int absy = parentShell.getSize().y / 2 - getShell().getSize().y / 2;
//		absy = absy > 20 ? 20 : absy;
//		return new Point(parentShell.getLocation().x + absx, parentShell.getLocation().y + absy);
//	}

	@Override
	protected void createButtonsForButtonBar(Composite pParent) {
	}

	@Override
	protected Control createButtonBar(Composite pParent) {
		Control bar = super.createButtonBar(pParent);
		// fixLayout((Composite) bar);
		bar.setLayoutData(GridDataFactory.swtDefaults().hint(1, 1).create());
		return bar;
	}

	@Override
	protected int getShellStyle() {
		return fShellStyle;
	}

	private void fixLayout(Composite parent) {
		((GridLayout) parent.getLayout()).marginHeight = 0;
		((GridLayout) parent.getLayout()).marginBottom = 0;
		((GridLayout) parent.getLayout()).marginLeft = 0;
		((GridLayout) parent.getLayout()).marginRight = 0;
		((GridLayout) parent.getLayout()).marginWidth = 0;
		((GridLayout) parent.getLayout()).marginTop = 0;
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
	}

	@Override
	public int open() {
		setBlockOnOpen(false);
		int result = super.open();
		if (result == Window.OK) {
			fTipComposite.setTipManager(fTipManager);
		}
		return result;
	}

	@Override
	protected void configureShell(Shell pNewShell) {
		super.configureShell(pNewShell);
		Image pluginImage = ResourceManager.getPluginImage("org.eclipse.tips.ui", "icons/lightbulb.png"); //$NON-NLS-1$//$NON-NLS-2$
		if (pluginImage != null) {
			pNewShell.setImage(pluginImage);
		}
	}
}