/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Itema AS - Typo fix, corrected lazy initialisation of field
 *******************************************************************************/

package org.eclipse.mylyn.builds.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.builds.core.IBuildElement;
import org.eclipse.mylyn.builds.core.IBuildModel;
import org.eclipse.mylyn.builds.core.IBuildPlan;
import org.eclipse.mylyn.builds.core.IBuildServer;
import org.eclipse.mylyn.builds.core.spi.BuildConnector;
import org.eclipse.mylyn.builds.internal.core.BuildServer;
import org.eclipse.mylyn.builds.ui.spi.BuildConnectorUi;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.internal.builds.ui.BuildConnectorDescriptor;
import org.eclipse.mylyn.internal.builds.ui.BuildsUiInternal;
import org.eclipse.mylyn.internal.builds.ui.BuildsUiPlugin;
import org.eclipse.mylyn.internal.builds.ui.commands.OpenHandler;
import org.eclipse.mylyn.internal.builds.ui.notifications.BuildsServiceNotification;
import org.eclipse.mylyn.internal.builds.ui.view.BuildsView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author Steffen Pingel
 * @author Torkild U. Resheim
 */
public class BuildsUi {

	private static HashMap<String, BuildConnectorDescriptor> descriptorByKind;

	public synchronized static BuildConnector getConnector(String connectorKind) {
		BuildConnectorDescriptor descriptor = getConnectorDescriptorByKind().get(connectorKind);
		return (descriptor != null) ? descriptor.getCoreDelegate() : null;
	}

	public synchronized static BuildConnectorUi getConnectorUi(String connectorKind) {
		BuildConnectorDescriptor descriptor = getConnectorDescriptorByKind().get(connectorKind);
		return (descriptor != null) ? descriptor.getUiDelegate() : null;
	}

	public synchronized static IBuildModel getModel() {
		return BuildsUiInternal.getModel();
	}

	private synchronized static HashMap<String, BuildConnectorDescriptor> getConnectorDescriptorByKind() {
		if (descriptorByKind != null) {
			return descriptorByKind;
		}

		descriptorByKind = new HashMap<String, BuildConnectorDescriptor>();

		MultiStatus result = new MultiStatus(BuildsUiPlugin.ID_PLUGIN, 0, "Build connectors failed to load.", null); //$NON-NLS-1$

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint connectorsExtensionPoint = registry.getExtensionPoint(BuildsUiPlugin.ID_PLUGIN + ".connectors");
		IExtension[] extensions = connectorsExtensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				BuildConnectorDescriptor descriptor = new BuildConnectorDescriptor(element);
				IStatus status = descriptor.validate();
				if (status.isOK()) {
					descriptorByKind.put(descriptor.getConnectorKind(), descriptor);
				} else {
					result.add(status);
				}
			}
		}

		if (!result.isOK()) {
			StatusManager.getManager().handle(result);
		}

		return descriptorByKind;
	}

	public static IBuildServer createServer(String connectorKind) {
		return BuildsUiInternal.createServer(connectorKind, new RepositoryLocation());
	}

	public static BuildConnector getConnector(IBuildServer server) {
		return getConnector(((BuildServer) server).getConnectorKind());
	}

	public static BuildConnectorUi getConnectorUi(IBuildServer server) {
		return getConnectorUi(((BuildServer) server).getConnectorKind());
	}

	public static void serverDiscovered(String title, String description) {
		BuildsServiceNotification notification = new BuildsServiceNotification(title, description);
		NotificationsUi.getService().notify(Collections.singletonList(notification));
	}

	public static void open(IBuildElement element) {
		Assert.isNotNull(element);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IBuildElement openElement = null;
				if (element instanceof IBuildPlan) {
					IBuildPlan plan = (IBuildPlan) element;
					if (plan.getLastBuild() != null) {
						openElement = plan.getLastBuild();
					}
				}
				if (openElement == null) {
					openElement = element;
				}
				List<IEditorPart> parts = OpenHandler.openBuildElements(page, Collections.singletonList(openElement));
				if (parts.size() > 0) {
					return;
				}
			}
		}
		// fall back to opening builds view
		BuildsView.openInActivePerspective();
	}

	/**
	 * Returns a list of existing server locations. It is safe to call this method from any thread.
	 */
	public static List<RepositoryLocation> getServerLocations() {
		final List<RepositoryLocation> locations = new ArrayList<RepositoryLocation>();
		final IBuildModel model = BuildsUi.getModel();
		BuildsUiInternal.getOperationService().getRealm().syncExec(new Runnable() {
			public void run() {
				List<IBuildServer> servers = model.getServers();
				for (IBuildServer server : servers) {
					locations.add(server.getLocation());
				}
			}
		});
		return locations;
	}

}
