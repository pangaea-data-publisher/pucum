package de.pangaea.ucum.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.fhir.ucum.Converter;
import org.fhir.ucum.ExpressionParser;
import org.fhir.ucum.Term;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumModel;
import org.fhir.ucum.special.Registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import de.pangaea.ucum.v1.model.IgnoreLabelMixin;
import de.pangaea.ucum.v1.model.PanQuantity;

@Path("/api")
public class PanUcumService {
	private static final Logger logger = Logger.getLogger(PanUcumService.class);
	private static UcumEssenceService ucumInst = PanUcumApp.getUcumSvc();
	private HashMap<String, String> pangUcumMappings = PanUcumApp.getPangUcumMapping();
	private HashMap<String, String> pangUcumReplacements = PanUcumApp.getReplacement();
	private HashMap<String, String> dimensionMappings = PanUcumApp.getDimensionMapping();
	private UcumModel model = PanUcumApp.getUcumModel();
	private Registry handlers = new Registry();

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getStartingPage() {
		String output = "<h1>Hello World!<h1>" + "<p>RESTful Service is running ... <br>Ping @ " + new Date().toString()
				+ "</p<br>";
		return Response.status(200).entity(output).build();
	}

	@GET
	@Path("validate/{uom : (.+)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateUCUMUnit(@PathParam("uom") String units) {
		String u = units.trim();
		Status status = null;
		String statusMsg = null;
		String ucum = null;
		boolean bad_request = false;
		Term term = null;

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.addMixIn(PanQuantity.class, IgnoreLabelMixin.class);
		PanQuantity pan = new PanQuantity();
		pan.setInput(u);

		try {
			// status = ucumInst.validate(u);
			term = new ExpressionParser(model).parse(u);
		} catch (Exception e) {
			logger.debug("Unit parsing exception: " + e.getMessage());
			statusMsg = e.getMessage();
		}

		// valid uom
		if (term != null) {
			ucum = u;
		} else {
			// invalid uom therefore try to find the ucum-compliant units from pangaea
			// mappings
			ucum = pangUcumMappings.get(u);
			bad_request = true;
		}

		String json = null;
		if (ucum != null) {
			pan.setUcum(ucum);
			if (bad_request) {
				status = Response.Status.BAD_REQUEST;
				statusMsg = "Invalid unit has been translated into an UCUM-compliant unit.";
			} else {
				status = Response.Status.OK;
			}
		} else {
			status = Response.Status.NOT_FOUND;
		}

		pan.setStatus(status.name());
		pan.setStatus_msg(statusMsg);

		try {
			json = objectMapper.writeValueAsString(pan);
		} catch (JsonProcessingException e) {
			logger.debug("Json writeValueAsString :" + e.getMessage());
		}
		return Response.status(status).entity(json).build();
	}
	
	@GET
	@Path("quantity/{ucum : (.+)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUcumDefinition(@PathParam("ucum") String ucumcode) {
		String u = ucumcode.trim();
		Status status = null;
		String statusMsg = null;
		String ucum = null;
		boolean bad_request = false;
		Term term = null;

		PanQuantity pan = new PanQuantity();
		pan.setInput(u);

		try {
			term = new ExpressionParser(model).parse(u);
		} catch (Exception e) {
			logger.debug("Unit parsing exception: " + e.getMessage());
			statusMsg = e.getMessage();
		}

		// valid uom
		if (term != null) {
			ucum = u;
		} else {
			// invalid uom therefore try to find the ucum-compliant units from pangaea
			// mappings
			ucum = pangUcumMappings.get(u);
			bad_request = true;
		}

		if (ucum != null) {
			pan.setUcum(ucum);
			if (bad_request) {
				status = Response.Status.BAD_REQUEST;
				statusMsg = "Invalid unit has been translated into an UCUM-compliant unit.";
			} else {
				status = Response.Status.OK;
			}
			
			// describe ucum, i.e., full name, canonicalunits, quantity, dimension
			try {
				pan.setFullname(ucumInst.analyse(ucum));
			} catch (UcumException e) {
				logger.debug("UcumService Analyse Exception:" + e.getMessage());
			}
			String canonUnit = null;
			try {
				canonUnit = ucumInst.getCanonicalUnits(ucum);
			} catch (UcumException e1) {
				logger.debug("UcumService getCanonicalUnits Exception:" + e1.getMessage());
			}
			if(canonUnit != null)
			{
				pan.setCanonicalunit(canonUnit);
				try {
					pan.setVerbosecanonicalunit(getVerboseCanonicalUnits(ucum));
				} catch (UcumException e2) {
					logger.debug("UcumService getVerboseCanonicalUnits Exception:" + e2.getMessage());
				}
				
				String dimensions = null;
				dimensions = getSortedDimensions(ucum);
				if (dimensions != null) {
					pan.setDimension(dimensions);
					ArrayList<String> quantities = PanUcumApp.getQuantitiesByDimensionUnits(dimensions);
					if (quantities != null) {
						pan.setQuantities(quantities);
					} else {
						logger.debug("No QUANTITY found :" + ucum);
					}
				} else {
					logger.debug("No DIMENSION not found :" + ucum);
				}
			}
		} else {
			status = Response.Status.NOT_FOUND;
		}

		pan.setStatus(status.name());
		pan.setStatus_msg(statusMsg);
		return Response.status(status).entity(pan).build();
	}


	private String replaceSpecialChar(String input) {
		String output = null;
		if (pangUcumReplacements.containsKey(input)) {
			String val = pangUcumReplacements.get(input);
			output = input.replace(input, val);
		}
		return output;
	}

	public String getSortedDimensions(String unit) {
		String dimension = null;
		try {
			Term term = new ExpressionParser(model).parse(unit);
			dimension = new PanExpressionComposer().compose(new Converter(model, handlers).convert(term), false);
			/*
			 * if (dimension.contains(".")) { List<String> parts =
			 * Arrays.asList(dimension.split("\\.")); Collections.sort(parts); dimension =
			 * String.join(".", parts); }
			 */
		} catch (Exception e) {
			logger.debug("getSortedDimensions Exception:" + e.getMessage());
		}
		return dimension;
	}

	public String getVerboseCanonicalUnits(String unit) throws UcumException {
		try {
			Term term = new ExpressionParser(model).parse(unit);
			return new PanExpressionComposer().compose(new Converter(model, handlers).convert(term), false, true);
		} catch (Exception e) {
			throw new UcumException("Error processing " + unit + ": " + e.getMessage(), e);
		}
	}

}