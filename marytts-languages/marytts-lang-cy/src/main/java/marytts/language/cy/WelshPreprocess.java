package marytts.language.cy;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

import com.ibm.icu.util.ULocale;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import marytts.util.io.FileUtils;

/**
 * @author Tristan Hamilton
 * 
 *         Processes cardinal and ordinal numbers.
 */
public class WelshPreprocess extends InternalModule {

	static final ULocale CY_LOCALE = new ULocale.Builder().setLanguage("cy").build();

	public WelshPreprocess() {
		super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, CY_LOCALE.toLocale());
	}

	// TODO : angen ail-ysgrifennu fel Java y cod canlynol o Festival CY :
	// https://github.com/techiaith/llais_festival/blob/master/Tokenisation/welshtoken.scm
	// 
	public MaryData process(MaryData d) throws Exception {
		Document doc = d.getDocument();		
		checkForNumbers(doc);		
		MaryData result = new MaryData(getOutputType(), d.getLocale());
		result.setDocument(doc);
		return result;
	}

	protected void checkForNumbers(Document doc) {

		TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
				new NameNodeFilter(MaryXML.TOKEN), false);

		Element t = null;
		while ((t = (Element) tw.nextNode()) != null) {

			if (MaryDomUtils.hasAncestor(t, MaryXML.SAYAS) || t.hasAttribute("ph") || t.hasAttribute("sounds_like")) {
				// ignore token
				continue;
			}

			String origText = MaryDomUtils.tokenText(t);

			System.out.println("Preprocess token : " + origText);
			if (MaryDomUtils.tokenText(t).matches(Welsh_Time.TIMEREGEX)) {				
				MaryDomUtils.setTokenText(t, expandTime(MaryDomUtils.tokenText(t)));
			}
			else if (MaryDomUtils.tokenText(t).matches("([0-9]+,)?([0-9]+,)?[0-9]+")) {
				MaryDomUtils.setTokenText(t, expandNumber(MaryDomUtils.tokenText(t))); 
			}

			// if token isn't ignored but there is no handling rule don't add MTU
			if (!origText.equals(MaryDomUtils.tokenText(t))) {
				MaryDomUtils.encloseWithMTU(t, origText, null);
			}

		}

	}

	protected String expandNumber(String number) {
		System.out.println("expandNumber : " + number);
		number = number.replace(",","");
		return Welsh_Base_10.tokenise(Integer.parseInt(number), false);
	}

	protected String expandTime(String time){
		System.out.println("expandTime : " + time);
		return Welsh_Time.tokenise(time);
	}
}
