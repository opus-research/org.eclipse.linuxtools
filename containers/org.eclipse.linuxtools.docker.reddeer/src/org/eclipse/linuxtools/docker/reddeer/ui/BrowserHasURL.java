/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.linuxtools.docker.reddeer.ui;

import org.hamcrest.Matcher;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.eclipse.ui.browser.BrowserEditor;

/**
 * Wait condition which returns true if a given browser has the specified URL.
 */
public class BrowserHasURL extends AbstractWaitCondition {

	private BrowserView browser;
	private BrowserEditor browserEditor;
	private String expectedURL;
	private Matcher<String> expectedURLMatcher;
	
	/**
	 * Construct a condition with a given browser view and expected URL.
	 * 
	 * @param browser Browser view
	 * @param expectedURL Expected URL
	 */
	public BrowserHasURL(BrowserView browser,String expectedURL){
		this.browser = browser;
		this.expectedURL = expectedURL;
	}
	
	/**
	 * Construct a condition with a given browser view and URL matcher.
	 * 
	 * @param browser Browser view
	 * @param expectedURLMatcher URL matcher
	 */
	public BrowserHasURL(BrowserView browser,Matcher<String> expectedURLMatcher){
		this.browser = browser;
		this.expectedURLMatcher = expectedURLMatcher;
	}
	
	/**
	 * Construct a condition with a given browser editor and expected URL.
	 * 
	 * @param browser Browser editor
	 * @param expectedURL Expected URL
	 */
	public BrowserHasURL(BrowserEditor browser,String expectedURL){
		this.browserEditor = browser;
		this.expectedURL = expectedURL;
	}
	
	/**
	 * Construct a condition with a given browser editor and URL matcher.
	 *
	 * @param browser Browser editor
	 * @param expectedURLMatcher the expected url matcher
	 */
	public BrowserHasURL(BrowserEditor browser,Matcher<String> expectedURLMatcher){
		this.browserEditor = browser;
		this.expectedURLMatcher = expectedURLMatcher;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.reddeer.common.condition.WaitCondition#test()
	 */
	@Override
	public boolean test() {
		if (expectedURLMatcher != null){
			if(browser != null){
				return expectedURLMatcher.matches(browser.getPageURL());
			} else {
				return expectedURLMatcher.matches(browserEditor.getPageURL());
			}
		} else {
			if(browser != null){
				return browser.getPageURL().equals(expectedURL);
			} else {
				return browserEditor.getPageURL().equals(expectedURL);
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.reddeer.common.condition.AbstractWaitCondition#description()
	 */
	@Override
	public String description() {
		if(expectedURLMatcher != null){
			return "browser is pointed to URL: "+expectedURLMatcher.toString();
		}
		return "browser is pointed to URL: "+expectedURL;
	}
	}