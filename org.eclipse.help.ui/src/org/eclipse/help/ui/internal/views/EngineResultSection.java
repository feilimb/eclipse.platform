/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.search.federated.ISearchEngineResult;
import org.eclipse.help.ui.internal.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;
import org.osgi.framework.Bundle;

public class EngineResultSection {
	private SearchResultsPart part;

	private EngineDescriptor desc;

	private ArrayList hits;

	private Section section;

	private FormText searchResults;

	private ImageHyperlink prevLink;

	private ImageHyperlink nextLink;

	private boolean needsUpdating;

	private FederatedSearchSorter sorter;

	private int HITS_PER_PAGE = 10;

	private static final String HREF_PREV = "__prev__";

	private static final String HREF_NEXT = "__next__";
	private static final String HREF_PROGRESS = "__progress__";
	private static final String PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView";

	private int resultOffset = 0;

	public EngineResultSection(SearchResultsPart part,
			EngineDescriptor desc) {
		this.part = part;
		this.desc = desc;
		hits = new ArrayList();
		sorter = new FederatedSearchSorter();
	}
	
	public boolean hasControl(Control control) {
		return searchResults.equals(control);
	}

	public boolean matches(EngineDescriptor desc) {
		return this.desc == desc;
	}

	public Control createControl(Composite parent, final FormToolkit toolkit) {
		section = toolkit.createSection(parent, Section.CLIENT_INDENT
				| Section.COMPACT | Section.TWISTIE | Section.EXPANDED);
		// section.marginHeight = 10;
		createFormText(section, toolkit);
		section.setClient(searchResults);
		updateSectionTitle();
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				if (needsUpdating)
					asyncUpdateResults(true);
			}

			public void expansionStateChanged(ExpansionEvent e) {
			}
		});
		return section;
	}

	private void createFormText(Composite parent, FormToolkit toolkit) {
		searchResults = toolkit.createFormText(parent, true);
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		searchResults.marginHeight = 5;
		String topicKey = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
		String nwKey = IHelpUIConstants.IMAGE_NW;
		String searchKey = IHelpUIConstants.IMAGE_HELP_SEARCH;
		searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
		searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
		searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
		searchResults.setColor("summary", parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_DARK_SHADOW));
		searchResults.setImage(ISharedImages.IMG_TOOL_FORWARD, PlatformUI
				.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_TOOL_FORWARD));
		searchResults.setImage(ISharedImages.IMG_TOOL_BACK, PlatformUI
				.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_TOOL_BACK));
		searchResults.setImage(desc.getId(), desc.getIconImage());
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (HREF_NEXT.equals(href)) {
					resultOffset += HITS_PER_PAGE;
					asyncUpdateResults(false);
				} else if (HREF_PREV.equals(href)) {
					resultOffset -= HITS_PER_PAGE;
					asyncUpdateResults(false);
				} else if (HREF_PROGRESS.equals(href)) {
					showProgressView();
				} else
					part.doOpenLink(e.getHref());
			}
		});
		initializeText();
		needsUpdating = true;
	}
	
	private void initializeText() {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.views");
		if (bundle!=null) {
			StringBuffer buff = new StringBuffer();
			buff.append("<form>");
			buff.append("<p><a href=\"");
			buff.append(HREF_PROGRESS);
			buff.append("\" alt=\"");
			buff.append("Show Progress View");
			buff.append("\">");
			buff.append("Search in progress...");
			buff.append("</a></p></form>");
			searchResults.setText(buff.toString(), true, false);
		}
		else {
			searchResults.setText("Search in progress...", false, false);
		}
	}
	
	private void showProgressView() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window!=null) {
			IWorkbenchPage page = window.getActivePage();
			if (page!=null) {
				try {
					page.showView(PROGRESS_VIEW);
				}
				catch (PartInitException e) {
					HelpUIPlugin.logError("Error opening the progress view", e);
				}
			}
		}
	}

	public synchronized void add(ISearchEngineResult match) {
		hits.add(match);
		asyncUpdateResults(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
	 */
	public synchronized void add(ISearchEngineResult[] matches) {
		for (int i = 0; i < matches.length; i++)
			hits.add(matches[i]);
		asyncUpdateResults(false);
	}

	private void asyncUpdateResults(boolean now) {
		Runnable runnable = new Runnable() {
			public void run() {
				BusyIndicator.showWhile(section.getDisplay(), new Runnable() {
					public void run() {
						updateResults(true);
					}
				});
			}
		};
		if (now)
			section.getDisplay().syncExec(runnable);
		else
			section.getDisplay().asyncExec(runnable);
	}

	void updateResults(boolean reflow) {
		updateSectionTitle();
		/*
		 * if (!section.isExpanded()) { needsUpdating=true; return; }
		 */
		ISearchEngineResult[] results = (ISearchEngineResult[]) hits
				.toArray(new ISearchEngineResult[hits.size()]);
		if (part.getShowCategories())
			sorter.sort(null, results);
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		IHelpResource oldCat = null;
		boolean earlyExit = false;
		addNavigation(buff);

		for (int i = resultOffset; i < hits.size(); i++) {
			if (i - resultOffset == HITS_PER_PAGE) {
				break;
			}
			ISearchEngineResult hit = results[i];
			IHelpResource cat = hit.getCategory();
			if (part.getShowCategories()
					&& cat != null
					&& (oldCat == null || !oldCat.getLabel().equals(
							cat.getLabel()))) {
				buff.append("<p>");
				if (cat.getHref() != null) {
					buff.append("<a bold=\"true\" href=\"");
					buff.append(escapeSpecialChars(cat.getHref()));
					buff.append("\">");
					buff.append(cat.getLabel());
					buff.append("</a>");
				} else {
					buff.append("<b>");
					buff.append(cat.getLabel());
					buff.append("</b>");
				}
				buff.append("</p>");
				oldCat = cat;
			}
			int indent = part.getShowCategories() && cat != null ? 26 : 21;
			int bindent = part.getShowCategories() && cat != null ? 5 : 0;
			buff
					.append("<li indent=\"" + indent + "\" bindent=\"" + bindent + "\" style=\"image\" value=\""); //$NON-NLS-1$
			buff.append(desc.getId());
			buff.append("\">"); //$NON-NLS-1$
			buff.append("<a href=\""); //$NON-NLS-1$
			buff.append(escapeSpecialChars(hit.getHref()));
			buff.append("\""); //$NON-NLS-1$
			if (hit.getCategory() != null) {
				buff.append(" alt=\""); //$NON-NLS-1$
				buff.append(hit.getCategory().getLabel());
				buff.append("\""); //$NON-NLS-1$
			}
			buff.append(">"); //$NON-NLS-1$
			buff.append(hit.getLabel());
			buff.append("</a>"); //$NON-NLS-1$
			if (part.getShowDescription()) {
				String summary = getSummary(hit);
				if (summary != null) {
					buff.append("<br/>");
					buff.append("<span color=\"summary\">");
					buff.append(summary);
					buff.append("</span>");
					buff.append("...");
				}
			}
			/*
			 * buff.append(" <a href=\""); //$NON-NLS-1$ buff.append("nw:");
			 * //$NON-NLS-1$ buff.append(hit.getHref()); buff.append("\"> <img
			 * href=\""); //$NON-NLS-1$ buff.append(IHelpUIConstants.IMAGE_NW);
			 * buff.append("\" alt=\""); //$NON-NLS-1$
			 * buff.append(HelpUIResources.getString("SearchResultsPart.nwtooltip"));
			 * //$NON-NLS-1$ buff.append("\""); //$NON-NLS-1$ buff.append("/>");
			 * //$NON-NLS-1$ buff.append(" </a>"); //$NON-NLS-1$
			 */
			buff.append("</li>"); //$NON-NLS-1$
		}
		addNavigation(buff);
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		section.layout();
		if (reflow)
			part.reflow();
	}

	private void addNavigation(StringBuffer buff) {
		if (hits.size() > HITS_PER_PAGE) {
			buff.append("<p>");
			if (resultOffset > 0) {
				// add prev
				buff.append("<a href=\"");
				buff.append(HREF_PREV);
				buff.append("\">");
				buff.append("<img href=\"");
				buff.append(ISharedImages.IMG_TOOL_BACK);
				buff.append("\"/>");
				buff.append(" Prev");
				buff.append("</a>   ");
			}
			if (hits.size() - resultOffset > HITS_PER_PAGE) {
				// add next
				buff.append("<a href=\"");
				buff.append(HREF_NEXT);
				buff.append("\">");
				buff.append("Next ");
				buff.append("<img href=\"");
				buff.append(ISharedImages.IMG_TOOL_FORWARD);
				buff.append("\"/>");
				buff.append("</a>");
			}
			buff.append("</p>");
		}
	}

	private String getSummary(ISearchEngineResult hit) {
		String desc = hit.getDescription();
		if (desc != null) {
			String edesc = escapeSpecialChars(desc);
			if (!edesc.equals(hit.getLabel())) {
				String label = hit.getLabel();
				if (edesc.length() > label.length()) {
					String ldesc = edesc.substring(0, label.length());
					if (ldesc.equalsIgnoreCase(label))
						edesc = edesc.substring(label.length() + 1);
				}
				return edesc;
			}
		}
		return null;
	}

	private String escapeSpecialChars(String value) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '&':
				buf.append("&amp;"); //$NON-NLS-1$
				break;
			case '<':
				buf.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				buf.append("&gt;"); //$NON-NLS-1$
				break;
			case '\'':
				buf.append("&apos;"); //$NON-NLS-1$
				break;
			case '\"':
				buf.append("&quot;"); //$NON-NLS-1$
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}

	private void updateSectionTitle() {
		if (hits.size() == 1)
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hit", desc.getLabel(), ""
							+ hits.size()));
		else if (hits.size() <= HITS_PER_PAGE)
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hits", desc.getLabel(),
					"" + hits.size()));
		else {
			int from = (resultOffset + 1);
			int to = (resultOffset + HITS_PER_PAGE);
			to = Math.min(to, hits.size());
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hitsRange", desc
							.getLabel(), "" + from, "" + to, "" + hits.size()));
		}
	}

	public void dispose() {
		searchResults.setMenu(null);
		section.setMenu(null);
		section.dispose();
	}
}