package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Installs previously selected products and components.
 */
import java.lang.reflect.InvocationTargetException;import org.eclipse.core.internal.boot.update.IComponentDescriptor;import org.eclipse.core.internal.boot.update.IComponentEntryDescriptor;import org.eclipse.core.internal.boot.update.IManifestDescriptor;import org.eclipse.core.internal.boot.update.IProductDescriptor;import org.eclipse.core.internal.boot.update.UpdateManagerConstants;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.viewers.ColumnLayoutData;import org.eclipse.jface.viewers.ColumnWeightData;import org.eclipse.jface.viewers.TableLayout;import org.eclipse.jface.wizard.WizardPage;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.TableTree;import org.eclipse.swt.custom.TableTreeItem;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.TableColumn;import org.eclipse.update.internal.core.UMSessionManagerSession;import org.eclipse.update.internal.core.UpdateManagerException;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardPageURLInstalling extends WizardPage {
	protected boolean _bInitialized = false;
	protected UMWizard _wizard = null;
	protected TableTree _tableTreeItems = null;
	protected Button _buttonInstall = null;
	protected Label _labelLocation = null;
	protected UMWizardTreeItem[] _items = null;
	protected UMSessionManagerSession _session = null;
	protected IManifestDescriptor _descriptor = null;
	/**
	 */
	public UMWizardPageURLInstalling(UMWizard wizard, String strName) {
		super(strName);
		_wizard = wizard;

		this.setTitle(UpdateManagerStrings.getString("S_Install_Components"));
		this.setDescription(UpdateManagerStrings.getString("S_The_following_items_will_be_installed"));
	}
	/**
	 * 
	 */
	public void connectToTree(UMWizardTreeItem item) {

		// Remove all existing tree items
		//-------------------------------
		_tableTreeItems.removeAll();

		TableTreeItem treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

		if (item._strName != null)
			treeItem.setText(0, item._strName);

		if (item._strVersionCurrent != null)
			treeItem.setText(1, item._strVersionCurrent);

		if (item._strVendorName != null)
			treeItem.setText(2, item._strVendorName);

		treeItem.setData(item);

		// Create child tree items
		//------------------------
		if (item._vectorChildren != null) {
			for (int i = 0; i < item._vectorChildren.size(); ++i) {
				connectToTree((UMWizardTreeItem) item._vectorChildren.elementAt(i), treeItem);
			}
		}

		treeItem.setExpanded(true);
	}
	/**
	 * Connects items to a new tree widget.
	 */
	public void connectToTree(UMWizardTreeItem item, TableTreeItem treeItemParent) {

		// All of these should be component entries
		//-----------------------------------------
		TableTreeItem treeItem = new TableTreeItem(treeItemParent, SWT.NULL);
		treeItem.setText(0, item._strName);
		treeItem.setData(item);
	}
	/**
	 */
	public void createControl(Composite compositeParent) {
		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		compositeContent.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		compositeContent.setLayoutData(gridData);

		// Label: Location
		//----------------
		Label label = new Label(compositeContent, SWT.NULL);
		label.setText(UpdateManagerStrings.getString("S_Source_location") + ": ");

		_labelLocation = new Label(compositeContent, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		_labelLocation.setLayoutData(gridData);

		// Tree: Installable
		//------------------
		_tableTreeItems = new TableTree(compositeContent, SWT.FULL_SELECTION | SWT.BORDER);

		String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Version"), UpdateManagerStrings.getString("S_Provider")};
		int[] iColumnWeight = { 60, 20, 20 };
		TableLayout layoutTable = new TableLayout();

		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(_tableTreeItems.getTable(), SWT.NONE);
			tableColumn.setText(columnTitles[i]);
			ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
			layoutTable.addColumnData(cLayout);
		}
		_tableTreeItems.getTable().setLinesVisible(true);
		_tableTreeItems.getTable().setHeaderVisible(true);
		_tableTreeItems.getTable().setLayout(layout);
		_tableTreeItems.getTable().setLayout(layoutTable);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		_tableTreeItems.setLayoutData(gridData);

		setControl(compositeContent);
	}
	/**
	 * 
	 */
	public void doInstalls() {

		if (_descriptor == null)
			return;

		IManifestDescriptor[] descriptors = new IManifestDescriptor[1];

		descriptors[0] = _descriptor;

		try {
			_session = _wizard._updateManager.createSession(descriptors, true);

			// Install the product/components
			//-------------------------------
			IRunnableWithProgress operation = new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {
					try {
						// Do
						//---
						_wizard._updateManager.executeSession(_session, progressMonitor);

						// Undo
						//-----
						if (_session.getStatus().equals(UpdateManagerConstants.STATUS_FAILED) == true) {
							_wizard._updateManager.executeSessionUndo(_session, progressMonitor);
						}

						// Update launch information
						//--------------------------
						_wizard._updateManager.updateLaunchInfoAndRegistry(_session);

						// Cleanup staging area
						//---------------------
						_wizard._updateManager.cleanup();
					}
					catch (UpdateManagerException ex) {
					}
				}
			};

			try {
				_wizard.getContainer().run(true, true, operation);
			}
			catch (InterruptedException e) {
			}
			catch (InvocationTargetException e) {
			}
		}
		catch (UpdateManagerException ex) {
		}
	}
	/**
	 */
	public UMSessionManagerSession getSession() {
		return _session;
	}
	/**
	 * Obtains a list of registered component URLs from the local update registry.
	 * Obtains a list of bookmarked URLs from the persistent data.
	 * Creates a tree for all of the URLs.
	 */
	protected void initializeContent() {

		// Obtain the descriptor to be installed from the install page
		//------------------------------------------------------------
		UMWizardPageURLInstallable pageInstallable = (UMWizardPageURLInstallable) _wizard.getPage("installable");

		_descriptor = pageInstallable.getDescriptor();

		_tableTreeItems.removeAll();

		UMWizardTreeItem umTreeItemRoot = null;

		// Initialize with product
		//------------------------
		if (_descriptor instanceof IProductDescriptor) {

			UMWizardTreeItem itemProduct = umTreeItemRoot = new UMWizardTreeItem();
			itemProduct._iType = UpdateManagerConstants.TYPE_PRODUCT;
			itemProduct._strName = ((IProductDescriptor) _descriptor).getLabel();
			itemProduct._strVendorName = ((IProductDescriptor) _descriptor).getProviderName();
			itemProduct._strVersionCurrent = ((IProductDescriptor) _descriptor).getVersionStr();
			itemProduct._descriptorCurrent = _descriptor;

			// Component entries of the product
			//---------------------------------
			IComponentEntryDescriptor[] descriptorsEntry = ((IProductDescriptor) _descriptor).getComponentEntries();
			UMWizardTreeItem itemComponentEntry = null;

			for (int j = 0; j < descriptorsEntry.length; ++j) {

				// Display only mandatory or selected entries
				//-------------------------------------------
				if (descriptorsEntry[j].isOptionalForInstall() == false || descriptorsEntry[j].isSelected() == true) {
					itemComponentEntry = new UMWizardTreeItem();
					itemComponentEntry._iType = UpdateManagerConstants.TYPE_COMPONENT_ENTRY;
					itemComponentEntry._strName = descriptorsEntry[j].getLabel();
					itemComponentEntry._strId = descriptorsEntry[j].getUniqueIdentifier();
					itemComponentEntry._strVersionAvailable = descriptorsEntry[j].getVersionStr();
					itemComponentEntry._descriptorEntry = descriptorsEntry[j];

					itemProduct.addChildItem(itemComponentEntry);
				}
			}
		}

		// Initialize with component
		//--------------------------
		else if (_descriptor instanceof IComponentDescriptor) {

			UMWizardTreeItem itemComponent = umTreeItemRoot = new UMWizardTreeItem();

			itemComponent._iType = UpdateManagerConstants.TYPE_COMPONENT;
			itemComponent._strDescription = ((IComponentDescriptor) _descriptor).getDescription();
			itemComponent._strName = ((IComponentDescriptor) _descriptor).getLabel();
			itemComponent._strId = ((IComponentDescriptor) _descriptor).getUniqueIdentifier();
			itemComponent._strVendorName = ((IComponentDescriptor) _descriptor).getProviderName();
			itemComponent._strVersionCurrent = ((IComponentDescriptor) _descriptor).getVersionStr();
			itemComponent._descriptorCurrent = _descriptor;
		}

		// Create tree widget items
		//-------------------------
		connectToTree(umTreeItemRoot);

		if (_descriptor != null) {
			_labelLocation.setText(_descriptor.getUMRegistry().getRegistryBaseURL().toExternalForm());
			setPageComplete(true);
		}
		return;
	}
	/**
	 * 
	 */
	public void setVisible(boolean bVisible) {

		if (bVisible == true)
			initializeContent();

		super.setVisible(bVisible);
	}
}