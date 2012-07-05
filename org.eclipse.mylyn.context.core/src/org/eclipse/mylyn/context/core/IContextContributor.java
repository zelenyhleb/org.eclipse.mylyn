/*******************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Schmidt - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.context.core;

import java.io.InputStream;

/**
 * A ContextContributor may be used to put additional data to Mylyn context. As the ContextContributor is in charge of
 * serialization and deserialization of provided data, the data type is not limited.
 * 
 * @since 3.9
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContextContributor extends IContextListener {

	/**
	 * Provides data which should be added to the given context.
	 * 
	 * @param context
	 *            context that is going to be saved
	 * @return an InputStream with context related data or null
	 */
	public InputStream getDataAsStream(IInteractionContext context);

	/**
	 * @return an unique identifier for this ContextContributor
	 */
	public String getIdentifier();
}
