package org.jasig.portal.resourceserver.utils.taglib;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

import org.mockito.Mockito;

/**
 * TestResourceIncludeTag provides unit tests for the resource inclusion
 * JSP tags.
 * 
 * @author Jen Bourey
 */
public class TestResourceIncludeTag extends TestCase {
	
	private static final String RESOURCE_CONTEXT_INIT_PARAM = "resourceContextPath";
	private static final String DEFAULT_RESOURCE_CONTEXT = "/ResourceServingWebapp";
	private static final String CURRENT_CONTEXT = "/TestContext";
	
	/*
	 * Create the ResourceIncludeTag to test, as well as necessary mock objects
	 */
	private ResourceIncludeTag tag = new ResourceIncludeTag();
	private final PageContext pageContext = Mockito.mock(PageContext.class);
	private final ServletContext servletContext = Mockito.mock(ServletContext.class);
	private final JspWriter jspWriter = Mockito.mock(JspWriter.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// set our tag to produce a mock ServletContext
		tag.setPageContext(pageContext);
		Mockito.when(pageContext.getServletContext()).thenReturn(servletContext);

		// give us a current context path
		Mockito.when(servletContext.getContextPath()).thenReturn(CURRENT_CONTEXT);
		
		// set our mock JspWriter
		Mockito.when(pageContext.getOut()).thenReturn(jspWriter);

	}


	/**
	 * Test returning a local resource URL if the resource serving webapp
	 * context is unavailable.
	 * 
	 * @throws JspException
	 */
	public void testGetLocalUrl() throws JspException {
		tag.setValue("/test/resource");
		tag.doStartTag();
		
		assert(tag.getUrl().equals(CURRENT_CONTEXT.concat("/test/resource")));
		
	}
	
	/**
	 * Test returning a ResourceServingWebapp url with no init parameter
	 * in the web.xml to define the path.
	 * 
	 * @throws JspException
	 */
	public void testGetResourceWebappUrl() throws JspException {

		tag.setValue("/test/resource");

		Mockito
			.when(servletContext.getContext(DEFAULT_RESOURCE_CONTEXT))
			.thenReturn(servletContext);
		
		tag.doStartTag();
		
		assert(tag.getUrl().equals(DEFAULT_RESOURCE_CONTEXT.concat("/test/resource")));

	}
	
	/**
	 * Test returning a ResourceServingWebapp url when the webapp's context 
	 * has been specified as a non-default url.
	 * 
	 * @throws JspException
	 */
	public void testGetOverriddenResourceUrl() throws JspException {

		tag.setValue("/test/resource");
		
		Mockito
			.when(servletContext.getInitParameter(RESOURCE_CONTEXT_INIT_PARAM))
			.thenReturn("/OverrideResourceWebapp");
		Mockito
			.when(servletContext.getContext("/OverrideResourceWebapp"))
			.thenReturn(servletContext);
		
		tag.doStartTag();

		assert(tag.getUrl().equals("/OverrideResourceWebapp".concat("/test/resource")));

	}
	
	/**
	 * Ensure that if the tag gracefully handles missing trailing slashes from
	 * the resource webapp context or resource path strings.
	 * 
	 * @throws JspException
	 */
	public void testAddSlashesAsNecessary() throws JspException {
		
		Mockito.when(servletContext.getContext(DEFAULT_RESOURCE_CONTEXT))
			.thenReturn(servletContext);

		// set both the resource string and the specified resource server context
		// without leading forward slashes
		tag.setValue("test/resource");
		Mockito.when(servletContext.getInitParameter(RESOURCE_CONTEXT_INIT_PARAM))
			.thenReturn(DEFAULT_RESOURCE_CONTEXT.substring(1));
		tag.doStartTag();
		
		// tag should properly handle missing slashes
		assert(tag.getUrl().equals(DEFAULT_RESOURCE_CONTEXT.concat("/test/resource")));
		
	}
	
	/**
	 * Ensure that the url is saved to the appropriate variable is set when 
	 * a variable name is specified.
	 * 
	 * @throws JspException
	 */
	public void testSetVariable() throws JspException {
		tag.setValue("/test/resource");
		tag.setVar("var");
		tag.doStartTag();
		tag.doEndTag();
		
		Mockito.verify(pageContext, Mockito.times(1)).setAttribute("var", CURRENT_CONTEXT.concat("/test/resource"));
	}

	/**
	 * Ensure that the url is printed out when no variable name is specified.
	 * 
	 * @throws JspException
	 * @throws IOException
	 */
	public void testUnSetVariable() throws JspException, IOException {
		tag.setValue("/test/resource");
		tag.doStartTag();
		tag.doEndTag();
		
		Mockito.verify(jspWriter, Mockito.times(1)).print(CURRENT_CONTEXT.concat("/test/resource"));
	}


}