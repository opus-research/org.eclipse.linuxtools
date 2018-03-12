/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

public class CreateVMPageModel extends BaseDatabindingModel {

	public static final String VM_NAME = "VMName";
	public static final String BOX_NAME = "boxName";
	public static final String BOX_LOC = "boxLoc";

	private String vmName;
	private String boxLoc;
	private String boxName;

	public String getVMName() {
		return vmName;
	}

	public String getBoxName() {
		return boxName;
	}

	public String getBoxLoc() {
		return boxLoc;
	}

	public void setVMName(final String vmName) {
		firePropertyChange(VM_NAME, this.vmName, this.vmName = vmName);
	}

	public void setBoxName(final String boxName) {
		firePropertyChange(BOX_NAME, this.boxName, this.boxName = boxName);
	}

	public void setBoxLoc(final String boxLoc) {
		firePropertyChange(BOX_LOC, this.boxLoc, this.boxLoc = boxLoc);
	}

}
