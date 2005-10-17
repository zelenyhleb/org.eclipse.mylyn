/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.java.tests;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.core.IMylarElement;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.ui.IViewPart;

/**
 * @author Mik Kersten
 */
public class RefactoringTest extends AbstractJavaContextTest {

	private IViewPart view;
		
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		view = PackageExplorerPart.openInActivePerspective();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDelete() throws CoreException, InvocationTargetException, InterruptedException {
    	IMethod method = type1.createMethod("public void deleteMe() { }", null, true, null);
        monitor.selectionChanged(view, new StructuredSelection(method));
        IMylarElement node = MylarPlugin.getContextManager().getElement(method.getHandleIdentifier());
        assertTrue(node.getDegreeOfInterest().isInteresting()); 
        project.build();
        TestProgressMonitor monitor = new TestProgressMonitor();
        method.delete(true, monitor);
        if (!monitor.isDone()) Thread.sleep(100);		
        IMylarElement deletedNode = MylarPlugin.getContextManager().getElement(method.getHandleIdentifier());
        assertFalse(deletedNode.getDegreeOfInterest().isInteresting());
	}

	/**
	 * Limitation: only interest of compilation unit is preserved
	 */
	public void testTypeRename() throws CoreException, InterruptedException, InvocationTargetException {
		monitor.selectionChanged(view, new StructuredSelection(type1));
        IMylarElement node = MylarPlugin.getContextManager().getElement(type1.getHandleIdentifier());
        IMylarElement parentNode = MylarPlugin.getContextManager().getElement(type1.getParent().getHandleIdentifier());
        assertTrue(node.getDegreeOfInterest().isInteresting()); 
        assertTrue(parentNode.getDegreeOfInterest().isInteresting()); 
        
        project.build();
        TestProgressMonitor monitor = new TestProgressMonitor();
        type1.rename("NewName", true, monitor);
        if (!monitor.isDone()) Thread.sleep(100);
        ICompilationUnit unit = (ICompilationUnit)p1.getChildren()[0];
        IType newType = (IType)unit.getTypes()[0];
//        IMylarElement newNode = MylarPlugin.getContextManager().getElement(newType.getHandleIdentifier());
        IMylarElement newParentNode = MylarPlugin.getContextManager().getElement(newType.getParent().getHandleIdentifier());
//        assertTrue(newNode.getDegreeOfInterest().isInteresting()); 
        assertTrue(newParentNode.getDegreeOfInterest().isInteresting()); 
                
//        IMylarElement oldNode = MylarPlugin.getContextManager().getElement(node.getHandleIdentifier());
        IMylarElement oldParentNode = MylarPlugin.getContextManager().getElement(parentNode.getHandleIdentifier());
//        assertFalse(oldNode.getDegreeOfInterest().isInteresting()); 
        assertFalse(oldParentNode.getDegreeOfInterest().isInteresting()); 
	}
	
	public void testMethodRename() throws CoreException, InterruptedException, InvocationTargetException {
    	IMethod method = type1.createMethod("public void refactorMe() { }", null, true, null);
          
        assertTrue(method.exists());
        assertEquals(1, type1.getMethods().length);
        
        monitor.selectionChanged(view, new StructuredSelection(method));
        IMylarElement node = MylarPlugin.getContextManager().getElement(method.getHandleIdentifier());
        assertTrue(node.getDegreeOfInterest().isInteresting()); 
        
        project.build();
        TestProgressMonitor monitor = new TestProgressMonitor();
        method.rename("refactored", true, monitor);
        if (!monitor.isDone()) Thread.sleep(100);
        IMethod newMethod = type1.getMethods()[0];
        assertTrue(newMethod.getElementName().equals("refactored"));
        IMylarElement newNode = MylarPlugin.getContextManager().getElement(newMethod.getHandleIdentifier());
        assertTrue(newNode.getDegreeOfInterest().isInteresting()); 
        
        IMylarElement goneNode = MylarPlugin.getContextManager().getElement(node.getHandleIdentifier());
        assertFalse(goneNode.getDegreeOfInterest().isInteresting()); 
	}
}
