package org.jdom2.test.cases.xpath;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.*;
import org.jdom2.test.util.UnitTestUtil;
import org.jdom2.xpath.XPath;
import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractTestXPath {
	
	private final Document doc = new Document();
	
	private final Comment doccomment = new Comment("doc comment");
	private final ProcessingInstruction docpi = new ProcessingInstruction("jdomtest", "doc");
	
	private final Element main = new Element("main");
	private final Attribute mainatt = new Attribute("atta", "vala");
	private final Comment maincomment = new Comment("main comment");
	private final ProcessingInstruction mainpi = new ProcessingInstruction("jdomtest", "pi data");
	private final Text maintext1 = new Text(" space1 ");
	private final Element child1emt = new Element("child");
	private final Text child1text = new Text("child1text");
	private final Text maintext2 = new Text(" space2 ");
	private final Element child2emt = new Element("child");
	
	private final Namespace child3nsa = Namespace.getNamespace("c3nsa", "jdom:c3nsa");
	private final Namespace child3nsb = Namespace.getNamespace("c3nsb", "jdom:c3nsb");
	private final Element child3emt = new Element("child", child3nsa);
	private final Attribute child3attint = new Attribute("intatt", "-123", child3nsb);
	private final Attribute child3attdoub = new Attribute("doubatt", "-123.45", child3nsb);
	private final Text child3txt = new Text("c3text");
	
	private final String mainvalue = " space1 child1text space2 c3text";
	
	public AbstractTestXPath() {
		doc.addContent(doccomment);
		doc.addContent(docpi);
		doc.addContent(main);
		main.setAttribute(mainatt);
		main.addContent(maincomment);
		main.addContent(mainpi);
		main.addContent(maintext1);
		child1emt.addContent(child1text);
		main.addContent(child1emt);
		main.addContent(maintext2);
		main.addContent(child2emt);
		child3emt.setAttribute(child3attint);
		child3emt.setAttribute(child3attdoub);
		child3emt.addContent(child3txt);
		main.addContent(child3emt);
	}
	
	/**
	 * Create an instance of an XPath.
	 * Override this method to create the type of XPath instance we want to test.
	 * The method should add the supplied Namspace keys, and set the given variables
	 * on the XPath.
	 * @param path
	 * @param variables
	 * @param namespaces
	 * @return
	 * @throws JDOMException
	 */
	abstract XPath buildPath(String path) throws JDOMException;
	
	private XPath setupXPath(String path, Map<String, Object> variables, Namespace... namespaces) {
		
		XPath xpath = null;
		
		try {
			xpath = buildPath(path);
			
			assertTrue(xpath != null);
			
			assertFalse(xpath.equals(null));
			assertFalse(xpath.equals(new Object()));
			UnitTestUtil.checkEquals(xpath, xpath);
			
			assertEquals("getXPath()", path, xpath.getXPath());
			
			
			if (variables != null) {
				for (Map.Entry<String, Object> me : variables.entrySet()) {
					xpath.setVariable(me.getKey(), me.getValue());
				}
			}
			for (Namespace n : namespaces) {
				xpath.addNamespace(n);
			}
		} catch (JDOMException jde) {
			jde.printStackTrace();
			fail("Unable to create XPath " + path);
		}
		return xpath;
		
	}
	
	/**
	 * A mechanism for exercising the XPath system.
	 * @param xpath The xpath to run.
	 * @param context The context on which to run the XPath
	 * @param string What we expect the 'xpath' string value of the result to be (or null to skip test).
	 * @param number What we expect the xpath to resolve the result to be as an xpath 'Number' (or null to skip test);
	 * @param expect The nodes we expect from the XPath selectNodes query
	 */
	private static void checkXPath(XPath xpath, Object context, String value, Number number, Object...expect) {
		try {
			
			// Check the selectNodes operation.
			List<?> result = xpath.selectNodes(context);
			if (result == null) {
				fail ("Got a null result from selectNodes()");
			}
			String sze = result.size() == expect.length ? "" : 
				(" Also Different Sizes: expect=" + expect.length + " actual=" + result.size());
			int pos = 0;
			for (Object o : result) {
				if (pos > expect.length) {
					fail ("Results contained additional content at position " + 
							pos + " for xpath '" + xpath + "': " + o + sze);
				}
				if (o != expect[pos]) {
					assertEquals("Failed result at position " + pos + 
							" for xpath '" + xpath + "'." + sze, expect[pos], o);
				}
				pos++;
			}
			if (pos < expect.length) {
				fail ("Results are missing " + (expect.length - pos) + 
						" content at position " + pos + " for xpath '" + xpath + 
						"'. First missing content is: " + expect[pos] + sze);
			}
			
			// Check the selectSingleNode operation.
			Object o = xpath.selectSingleNode(context);
			if (expect.length == 0 && o != null) {
				fail("Expected XPath.selectSingleNode() to return nothing, " +
						"but it returned " + o + sze);
			}
			if (expect.length > 0 && o == null) {
				fail("XPath.selectSingleNode() returned nothing, but it should " +
						"have returned " + expect[0] + sze);
			}
			if (expect.length > 0 && o != expect[0]) {
				fail("XPath.selectSingleNode() was expected to return " + 
						expect[0] + "' but instead it returned '" + o + "'" + sze);
			}
			
			// Check the getValue() operation
			String gotstring = xpath.valueOf(context);
			if (value != null) {
				assertEquals("Checking valueOf()", value, gotstring);
			}
			
			// check numberValue()
			if (number == null) {
				// we do the check, ignore the result, including exceptions.
				try {
					xpath.numberValueOf(context);
					// Great too!
				} catch (JDOMException jde) {
					// OK, ignore it....
				} catch (Exception e) {
					e.printStackTrace();
					fail ("Expecting a value or  JDOMException from numberValueOf(), but got " + e.getClass());
				}
			} else {
				Number gotval = xpath.numberValueOf(context);
				if (!number.equals(gotval)) {
					assertEquals("Numbers fail to compare!", number, gotval);
				}
			}
			
			if (expect.length == 0 && o != null) {
				fail("Expected XPath.selectSingleNode() to return nothing, " +
						"but it returned " + o);
			}
			if (expect.length > 0 && o == null) {
				fail("XPath.selectSingleNode() returned nothing, but it should " +
						"have returned " + expect[0]);
			}
			if (expect.length > 0 && o != expect[0]) {
				fail("XPath.selectSingleNode() was expected to return " + 
						expect[0] + "' but instead it returned '" + o + "'");
			}
			
		} catch (JDOMException e) {
			e.printStackTrace();
			fail("Could not process XPath '" + xpath + 
					"'. Failed with: " + e.getClass() + 
					": " + e.getMessage());
		}
	}
	
	private void checkXPath(String xpath, Object context, String value, Object...expect) {
		checkXPath(setupXPath(xpath, null), context, value, null, expect);
	}

	private void checkComplexXPath(String xpath, Object context, Map<String, Object> variables, 
			Collection<Namespace> namespaces, String value, Number number, Object...expect) {
		Namespace[] nsa = namespaces == null ? new Namespace[0] : namespaces.toArray(new Namespace[0]);
		checkXPath(setupXPath(xpath, variables, nsa), context, value, number, expect);
	}

	@Test
	public void testSerialization() throws JDOMException {
		XPath xpath = XPath.newInstance("//main");
		XPath xser  = UnitTestUtil.deSerialize(xpath);
		assertTrue(xpath != xser);
		// TODO JaxenXPath has useless equals(). See issue #43
		// UnitTestUtil.checkEquals(xpath, xser);
		assertEquals(xpath.toString(), xser.toString());
	}
	
	@Test
	public void testSelectDocumentDoc() throws JDOMException {
		checkXPath("/", doc, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentMain() throws JDOMException {
		checkXPath("/", main, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentAttr() throws JDOMException {
		checkXPath("/", child3attint, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentPI() throws JDOMException {
		checkXPath("/", mainpi, mainvalue, doc);
	}

	@Test
	public void testSelectDocumentText() throws JDOMException {
		checkXPath("/", child1text, mainvalue, doc);
	}

	@Test
	public void testSelectMainByName() throws JDOMException {
		checkXPath("main", doc, mainvalue, main);
	}

	@Test
	public void testSelectMainFromDoc() throws JDOMException {
		checkXPath("//main", doc, mainvalue, main);
	}

	@Test
	public void testAncestorsFromRoot() throws JDOMException {
		checkXPath("ancestor::node()", doc, "");
	}

	@Test
	public void testAncestorsFromMain() throws JDOMException {
		checkXPath("ancestor::node()", main, mainvalue, doc);
	}

	@Test
	public void testAncestorsFromChild() throws JDOMException {
		checkXPath("ancestor::node()", child1emt, mainvalue, doc, main);
	}

	@Test
	public void testAncestorOrSelfFromRoot() throws JDOMException {
		checkXPath("ancestor-or-self::node()", doc, mainvalue, doc);
	}

	@Test
	public void testAncestorOrSelfFromMain() throws JDOMException {
		checkXPath("ancestor-or-self::node()", main, mainvalue, doc, main);
	}

	@Test
	public void testAncestorOrSelfFromChild() throws JDOMException {
		checkXPath("ancestor-or-self::node()", child1emt, mainvalue, doc, main, child1emt);
	}
	
	
	/* *************************************
	 * XPath Variable tests.
	 * ************************************* */

	@Test
	public void testSetVariable() {
		HashMap<String,Object> hm = new HashMap<String, Object>();
		String attval = mainatt.getValue();
		hm.put("valvar", attval);
		checkComplexXPath("//@*[string() = $valvar]", doc, hm, null, attval, null, mainatt);
	}

	/* *************************************
	 * XPath namespace tests.
	 * ************************************* */
	@Test
	public void testAttributeNamespaceAsNumberToo() {
		checkComplexXPath("//@c3nsb:intatt", child3emt, null, null, 
				"-123", Double.valueOf(-123), child3attint);
		checkComplexXPath("//@c3nsb:doubatt", child3emt, null, null, 
				"-123.45", Double.valueOf(-123.45), child3attdoub);
	}

	@Test
	public void testAddNamespaceNamespace() {
		checkComplexXPath("//c3nsa:child", doc, null, Collections.singleton(child3nsa),
				child3emt.getValue(), null, child3emt);
	}

	@Test
	// This fails the Jaxen Builder because the returned attributes are not in document order.
	public void testAttributesNamespace() {
		checkComplexXPath("//@*[namespace-uri() = 'jdom:c3nsb']", doc, null, null, 
				"-123", Integer.valueOf(-123), child3emt.getAttributes().toArray());
	}
	
	@Test
	public void testXPathDefaultNamespacesFromElement() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3emt, null, null, 
				"-123", Double.valueOf(-123), child3attint);
	}
	
	@Test
	public void testXPathDefaultNamespacesFromAttribute() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3attdoub, null, null, 
				"-123", Integer.valueOf(-123), child3attint);
	}
	
	@Test
	public void testXPathDefaultNamespacesFromText() {
		// the significance here is that the c3nsb namespace should already be
		// available because it is in scope on the 'context' element.
		// so, there should be no need to re-declare it for the xpath.
		checkComplexXPath("//@c3nsb:*[string() = '-123']", child3txt, null, null, 
				"-123", Integer.valueOf(-123), child3attint);
	}
	
	/* *******************************
	 * Negative TestCases
	 * ******************************* */
	
	@Test
	public void testNegativeBrokenPath() {
		try {
			XPath.newInstance("//badaxis::dummy");
			fail("Expected a JDOMException");
		} catch (JDOMException jde) {
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
	}

	@Test
	public void testNegativeBrokenExpression() {
		final String path = "//node()[string() = $novar]";
		XPath xp = null; 
		try {
			xp = XPath.newInstance(path);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not expecting an exception!");
		}
		assertEquals(xp.getXPath(), path);
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.selectSingleNode(doc);
			fail("Expected a JDOMException");
		} catch (JDOMException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.selectNodes(doc);
			fail("Expected a JDOMException");
		} catch (JDOMException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.valueOf(doc);
			fail("Expected a JDOMException");
		} catch (JDOMException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
		try {
			// we have not declared a value for $novar, so, expect a failure.
			xp.numberValueOf(doc);
			fail("Expected a JDOMException");
		} catch (JDOMException jde) {
			//System.out.println(jde.getMessage());
			// good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected a JDOMException but got " + e.getClass());
		}
		
	}

}
