package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

public class IOConsoleDocumentAdapter implements IDocumentAdapter, IDocumentListener {
    private static final String DELIMITER = System.getProperty("line.separator"); //$NON-NLS-1$;
    
    private int consoleWidth = -1;
    private ArrayList textChangeListeners;
    private IDocument document;
    private ArrayList lines;
    
    public IOConsoleDocumentAdapter(IDocument doc, int width) {
        textChangeListeners = new ArrayList();
        consoleWidth = width;
        document = doc;
        document.addDocumentListener(this);
        lines = new ArrayList();
        calculateLines();
    }
    
    private void calculateLines() {
        lines.clear();
        try {
            int docLines = document.getNumberOfLines();
            String line = null;
            for (int i = 0; i<docLines; i++) {
                int offset = document.getLineOffset(i);
                int length = document.getLineLength(i);
                
                while (length > 0) {
                    int wrappedLength = consoleWidth > 0 ? Math.min(consoleWidth, length) : length;
                    line = document.get(offset, wrappedLength);
                    lines.add(line);
                    offset += wrappedLength;
                    length -= wrappedLength;
                }
            }
            if (line != null && line.endsWith(DELIMITER)) {
                lines.add(""); //$NON-NLS-1$
            }
            
        } catch (BadLocationException e) {
        }
        
        if (lines.size() == 0) {
            lines.add(""); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentAdapter#setDocument(org.eclipse.jface.text.IDocument)
     */
    public void setDocument(IDocument doc) {
        document = doc;
        if (document != null) {
            calculateLines();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#addTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
     */
    public synchronized void addTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		if (!textChangeListeners.contains(listener)) {
			textChangeListeners.add(listener);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#removeTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
     */
    public synchronized void removeTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		textChangeListeners.remove(listener);   
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getCharCount()
     */
    public int getCharCount() {
        return document.getLength();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLine(int)
     */
    public String getLine(int lineIndex) {
        return (String) lines.get(lineIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineAtOffset(int)
     */
    public int getLineAtOffset(int offset) {
        //offset can be greater than length when user is deleting.
        if (offset >= document.getLength()) {
            return lines.size()-1;
        }
        
        int len = 0;
        int line = -1;
        while (len <= offset) {
            line++;
            String s = (String) lines.get(line);
            len += s.length();
        }
        return line > -1 ? line : 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineCount()
     */
    public int getLineCount() {
        return lines.size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineDelimiter()
     */
    public String getLineDelimiter() {
        return DELIMITER;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getOffsetAtLine(int)
     */
    public int getOffsetAtLine(int lineIndex) {
        int offset = 0;
        for (int i = 0; i< lineIndex; i++) {
            String s = (String) lines.get(i);
            offset += s.length();
        }
        return offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getTextRange(int, int)
     */
    public String getTextRange(int start, int length) {
        try {
            return document.get(start, length);
        } catch (BadLocationException e) {
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#replaceTextRange(int, int, java.lang.String)
     */
    public void replaceTextRange(int start, int replaceLength, String text) {
        try {
            document.replace(start, replaceLength, text);
        } catch (BadLocationException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#setText(java.lang.String)
     */
    public synchronized void setText(String text) {
        TextChangedEvent changeEvent = new TextChangedEvent(this);
        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textSet(changeEvent);
        }    
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public synchronized void documentAboutToBeChanged(DocumentEvent event) {
        try {
        TextChangingEvent changeEvent = new TextChangingEvent(this);
        changeEvent.start = event.fOffset;
        changeEvent.newText = (event.fText == null ? "" : event.fText); //$NON-NLS-1$
        changeEvent.replaceCharCount = event.fLength;
        changeEvent.newCharCount = (event.fText == null ? 0 : event.fText.length());
        
        int replacedLineCount = 0;
        String replacedText = document.get(event.fOffset, event.fLength);
        if (replacedText.length() > 0) {
            String[] replacedLines = replacedText.split(DELIMITER);
            for (int i = 0; i < replacedLines.length; i++) {
                String line = replacedLines[i];
                int len = line.length();
                if (len < consoleWidth) {
                    replacedLineCount++;
                } else {
                    replacedLineCount =+ consoleWidth%len;
                }
            }
        }
        changeEvent.replaceLineCount= replacedLineCount;
        
        int newLineCount = 0;
        String newText = event.fText;
        if (newText.length() > 0) {
            String[] newLines = newText.split(DELIMITER);
            if (consoleWidth <= 0) { //no wrapping
                newLineCount = newLines.length;
            } else {
                for (int i = 0; i < newLines.length; i++) {
                    String line = newLines[i];
                    int len = line.length();
                    if (len == 0 || len < consoleWidth) {
                        newLineCount++;
                    } else {
                        newLineCount += consoleWidth%len;
                    }
                }
            }
        }
        changeEvent.newLineCount = newLineCount;

        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textChanging(changeEvent);
        }
        } catch (BadLocationException e){
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public synchronized void documentChanged(DocumentEvent event) {
        calculateLines();
        TextChangedEvent changeEvent = new TextChangedEvent(this);

        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textChanged(changeEvent);
        }
    }

    /**
     * @param consoleWidth2
     */
    public void setWidth(int width) {
        consoleWidth = width;
        calculateLines();
        TextChangedEvent changeEvent = new TextChangedEvent(this);
        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textSet(changeEvent);
        }
    }
}
