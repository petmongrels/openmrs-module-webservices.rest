package org.openmrs.module.webservices.rest.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class EncounterControllerTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see EncounterController#create(SimpleObject,WebRequest,HttpServletResponse)
	 * @verifies create a new encounter
	 */
	@Test
	public void createEncounter_shouldCreateANewEncounter() throws Exception {
		int before = Context.getEncounterService().getAllEncounters(null).size();
		String json = "{\"location\":\"3890\", \"encounterType\": \"61ae96f4-6afe-4351-b6f8-cd4fc383cce1\", \"encounterDatetime\": \"2011-01-15\", \"patient\": \"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\", \"provider\":\"ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562\"}";
		SimpleObject post = new ObjectMapper().readValue(json, SimpleObject.class);
		Object newPatient = new EncounterController().create(post, emptyRequest(), new MockHttpServletResponse());
		Assert.assertNotNull(newPatient);
		Assert.assertEquals(before + 1, Context.getEncounterService().getAllEncounters(null).size());
	}
	
	/**
	 * @see EncounterController#find(String,WebRequest,HttpServletResponse)
	 * @verifies return no results if there are no matching encounters
	 */
	@Test
	public void findEncounters_shouldReturnNoResultsIfThereAreNoMatchingEncounters() throws Exception {
		List<Object> results = new EncounterController()
		        .search("noencounter", emptyRequest(), new MockHttpServletResponse());
		Assert.assertEquals(0, results.size());
	}
	
	/**
	 * @see EncounterController#find(String,WebRequest,HttpServletResponse)
	 * @verifies find matching encounters
	 */
	@Test
	public void findEncounters_shouldFindMatchingEncounters() throws Exception {
		List<Object> results = new EncounterController().search("Test", emptyRequest(), new MockHttpServletResponse());
		Assert.assertEquals(0, results.size());
	}
	
	/**
	 * @see EncounterController#getEncounter(String,WebRequest)
	 * @verifies get a default representation of a encounter
	 */
	@Test
	public void getEncounter_shouldGetADefaultRepresentationOfAEncounter() throws Exception {
		Object result = new EncounterController().retrieve("6519d653-393b-4118-9c83-a3715b82d4ac", emptyRequest());
		Assert.assertNotNull(result);
		Assert.assertEquals("6519d653-393b-4118-9c83-a3715b82d4ac", PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "encounterType"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "patient"));
	}
	
	/**
	 * @see EncounterController#purge(String,WebRequest,HttpServletResponse)
	 * @verifies fail to purge a encounter with dependent data
	 */
	@Test(expected = Exception.class)
	public void purgeEncounter_shouldNotPurgeAEncounterWithDependentData() throws Exception {
		int size = Context.getEncounterService().getEncountersByPatient(new Patient(7)).size();
		new EncounterController().purge("6519d653-393b-4118-9c83-a3715b82d4ac", emptyRequest(),
		    new MockHttpServletResponse());
		Assert.assertEquals(size - 1, Context.getEncounterService().getEncountersByPatient(new Patient(7)).size());
	}
	
	/**
	 * @see EncounterController#update(String,SimpleObject,WebRequest,HttpServletResponse)
	 * @verifies change a property on a encounter
	 */
	@Test
	public void updateEncounter_shouldChangeAPropertyOnAEncounter() throws Exception {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleObject post = new ObjectMapper().readValue("{\"encounterDatetime\":\"" + df.format(now) + "\"}",
		    SimpleObject.class);
		Object editedPatient = new EncounterController().update("6519d653-393b-4118-9c83-a3715b82d4ac", post,
		    emptyRequest(), new MockHttpServletResponse());
		Assert.assertEquals(df.format(now), df.format(Context.getEncounterService().getEncounter(3).getEncounterDatetime()));
	}
	
	/**
	 * @see EncounterController#delete(String,String,WebRequest,HttpServletResponse)
	 * @verifies void a encounter
	 */
	@Test
	public void voidEncounter_shouldVoidAEncounter() throws Exception {
		Encounter enc = Context.getEncounterService().getEncounter(3);
		Assert.assertFalse(enc.isVoided());
		new EncounterController().delete("6519d653-393b-4118-9c83-a3715b82d4ac", "unit test", emptyRequest(),
		    new MockHttpServletResponse());
		enc = Context.getEncounterService().getEncounter(3);
		Assert.assertTrue(enc.isVoided());
		Assert.assertEquals("unit test", enc.getVoidReason());
	}
	
	private WebRequest emptyRequest() {
		return new ServletWebRequest(new MockHttpServletRequest());
	}
}
