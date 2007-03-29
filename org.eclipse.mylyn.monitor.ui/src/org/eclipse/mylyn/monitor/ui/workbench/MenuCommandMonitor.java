/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.monitor.ui.workbench;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.monitor.core.InteractionEvent;
import org.eclipse.mylar.monitor.ui.MylarMonitorUiPlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Leah Findlater and Mik Kersten
 */
public class MenuCommandMonitor implements Listener {
	
	public static final String MENU_ITEM_ID = "item.label.";

	public static final String MENU_ITEM_SELECTED = "menu";

	public static final String TOOLBAR_ITEM_SELECTED = "toolbar";

	public static final String MENU_PATH_DELIM = "/";

	public void handleEvent(Event event) {
		try {
			if (!(event.widget instanceof Item))
				return;
			Item item = (Item) event.widget;
			if (item.getData() == null)
				return;
			Object target = event.widget.getData();
			String id = null;
			String delta = null;
			if (target instanceof IContributionItem) {
				id = ((IContributionItem) target).getId();
			}
			if (id == null && target instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) target).getAction();
				if (action.getId() != null) {
					id = action.getId();
				} else {
					id = action.getClass().getName();
				}
			} else if (id == null) {
				id = target.getClass().getName();
			}
				
			if (item instanceof MenuItem) {
				MenuItem menu = (MenuItem) item;
				Menu parentMenu = menu.getParent();
				String location = "";
				if (parentMenu != null) {
					while (parentMenu.getParentItem() != null) {
						location = parentMenu.getParentItem().getText() + MENU_PATH_DELIM + location;
						parentMenu = parentMenu.getParentMenu();
					}
				}
				if (id == null) {
					return;
					// TODO: would be good to put back this info in some form
					// but it can contain private data, bug 178604
					
//					if (id == null) 
//						id = "null";
//					String itemText = obfuscateItemText(item.getText());
//					id = id + "$" + MENU_ITEM_ID + location + itemText;
				}

				delta = MENU_ITEM_SELECTED;
			} else if (item instanceof ToolItem) {
				// TODO: would be good to put back this info in some form
				// but it can contain private data, bug 178604
				// ToolItem tool = (ToolItem) item;
				// if (id == null)
				// 	 id = "null";
				// id = id + "$" + MENU_ITEM_ID + '.' + tool.getToolTipText();
				delta = TOOLBAR_ITEM_SELECTED;
			}
			InteractionEvent interactionEvent = InteractionEvent.makeCommand(id, delta);
			MylarMonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);

		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Could not log selection", false);
		}
	}

//	/**
//	 * TODO: generalize this to other resources whose names are private
//	 */
//	private String obfuscateItemText(String text) {
//		if (text.indexOf(".java") != -1 || text.indexOf(".xml") != -1) {
//			return MylarMonitorUiPlugin.OBFUSCATED_LABEL;
//		} else {
//			return text;
//		}
//	}
}
