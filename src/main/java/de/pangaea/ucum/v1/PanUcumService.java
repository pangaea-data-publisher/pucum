package de.pangaea.ucum.v1;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fhir.ucum.Converter;
import org.fhir.ucum.Decimal;
import org.fhir.ucum.ExpressionParser;
import org.fhir.ucum.Pair;
import org.fhir.ucum.Term;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumModel;
import org.fhir.ucum.Unit;
import org.fhir.ucum.special.Registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;

import de.pangaea.ucum.v1.model.IgnoreLabelMixin;
import de.pangaea.ucum.v1.model.PanQuantity;

@Path("/api")
public class PanUcumService {
	private static final Logger logger = LogManager.getLogger(PanUcumService.class);
	private static UcumEssenceService ucumInst = PanUcumApp.getUcumSvc();
	// private HashMap<String, String> pangUcumMappings =
	// PanUcumApp.getPangUcumMapping();
	private UcumModel model = PanUcumApp.getUcumModel();
	private Registry handlers = new Registry();
	private static RegularExParser regularParser = new RegularExParser();
	private static ReadContext jsonContext = PanUcumApp.getJsonContext();

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
		
		String backup_of_u = units.trim();
		String u = units.trim();
		if (u.contains("#")) {
			u = u.replace("#", "n");
		}
		if (u.contains("\u00b5")) { //special character (micro) using Unicode
			u = u.replace("\u00b5", "u");
		}
		String udec = checkDecimalUnit(u);
		if (udec != null) {
			u = udec;
		}
		logger.info("Validate : "+u);

		StatusType status = null;
		String statusId = null;
		String statusMsg = null;
		String ucum = null;
		boolean bad_request = false;
		// Term term = null;

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.addMixIn(PanQuantity.class, IgnoreLabelMixin.class);
		PanQuantity pan = new PanQuantity();
		pan.setInput(backup_of_u);

		String validation = ucumInst.validate(u);

		if (validation == null) { // valid ucum
			ucum = u;
		} else {
			String unitFormatted = regularParser.runRegExpression(u);
			String secondValidation = ucumInst.validate(unitFormatted);
			if (secondValidation == null) {
				ucum = unitFormatted;
				bad_request = true;
			} else {
				statusMsg = validation;
			}
		}

		String json = null;
		if (ucum != null) {
			pan.setUcum(ucum);
			status = Response.Status.OK;
			if (bad_request) {
				statusId = PucumError.BAD_UNITS_TRANSLATED.getId();
				statusMsg = PucumError.BAD_UNITS_TRANSLATED.getMessage();
			} else {
				statusId = PucumError.VALID_UNITS.getId();
				statusMsg = PucumError.VALID_UNITS.getMessage();
			}
		} else {
			status = Response.Status.NOT_FOUND;
			statusId = PucumError.INVALID_UNITS.getId();
		}

		pan.setStatus(statusId);
		pan.setStatus_msg(statusMsg);
		//logger.info(pan.toString());
		
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
	public Response getUcumDefinition(@PathParam("ucum") String units) {
		String backup_of_u = units.trim();
		String u = units.trim();
		
		if (u.contains("#")) {
			u = u.replace("#", "n");
		}
		if (u.contains("\u00b5")) { //special character (micro) using Unicode
			u = u.replace("\u00b5", "u");
		}
		String udec = checkDecimalUnit(u);
		if (udec != null) {
			u = udec;
		}
		
		logger.info("Quantity Desc: "+u);
		Status status = null;
		String statusId = null;
		String statusMsg = null;
		String ucum = null;
		Term term = null;
		String ucumQuantities = null;
		//ArrayList<String> qudtQuantities = new ArrayList<String>();
		List<HashMap<String, Object>> qudtQuantities = new ArrayList<HashMap<String, Object>>(); 
		PanQuantity pan = new PanQuantity();
		pan.setInput(backup_of_u);
		
		String validation = ucumInst.validate(u);
		
		//System.out.println(validation);
		if (validation == null) { // valid ucum
			ucum = u;
		} else {
			String unitFormatted = regularParser.runRegExpression(u);
			//System.out.println("SS2 :"+unitFormatted);
			String secondValidation = ucumInst.validate(unitFormatted);
			if (secondValidation == null) {
				ucum = unitFormatted;
			} else {
				statusMsg = validation;
			}
		}

		if (ucum != null) {
			pan.setUcum(ucum);
			status = Response.Status.OK;
			try {
				term = new ExpressionParser(model).parse(ucum);
			} catch (UcumException e) {
				logger.debug("UcumService ExpressionParser exception: " + e.getMessage());
			}

			// describe ucum, i.e., full name, canonicalunits, quantity, dimension
			try {
				pan.setFullname(ucumInst.analyse(ucum));
			} catch (UcumException e) {
				logger.debug("UcumService Analyse Exception:" + e.getMessage());
			}
		//System.out.println("A :"+ucumInst.getModel().getUnit(ucum).getProperty());//<property>
			

			
			String canonUnit = null;
			try {
				canonUnit = ucumInst.getCanonicalUnits(ucum);
			} catch (UcumException e1) {
				logger.debug("UcumService getCanonicalUnits Exception:" + e1.getMessage());
			}

			if (canonUnit != null && !canonUnit.isEmpty()) {
				pan.setCanonicalunit(canonUnit);
				try {
					pan.setVerbosecanonicalunit(getVerboseCanonicalUnits(ucum));
				} catch (UcumException e2) {
					logger.debug("UcumService getVerboseCanonicalUnits Exception:" + e2.getMessage());
				}
			}

			Unit isUnit = model.getUnit(ucum);
			if (isUnit != null) {
				ucumQuantities = model.getUnit(ucum).getProperty();
			}
			pan.setUcum_quantity(ucumQuantities);
			
			String dimensions = null;
			dimensions = getDimensions(term);

			if (dimensions != null && !dimensions.isEmpty()) {
				// logger.debug("DIMENSION found!");
				qudtQuantities = getQUDTQuantities(dimensions, ucum, canonUnit);
				pan.setDimension(dimensions);
			} else {
				// search quantity by ucum code
				//logger.warn("DIMENSION cannot be determined for the units : " + ucum);
				qudtQuantities = getQuantitiesByUnit(ucum, canonUnit);
			}
			pan.setQudt_quantity(qudtQuantities);

			if (qudtQuantities == null && ucumQuantities == null) {
				statusId = PucumError.QUANTITY_NOT_FOUND.getId();
				statusMsg = PucumError.QUANTITY_NOT_FOUND.getMessage();
			} else {
				statusId = PucumError.QUANTITY_FOUND.getId();
				statusMsg = PucumError.QUANTITY_FOUND.getMessage();
			}
		} else {
			status = Response.Status.NOT_FOUND;
			statusId = PucumError.INVALID_UNITS.getId();
		}

		pan.setStatus(statusId);
		pan.setStatus_msg(statusMsg);
		return Response.status(status).entity(pan).build();
	}

	/*
	 * private String replaceSpecialChar(String input) { String output = null; if
	 * (pangUcumReplacements.containsKey(input)) { String val =
	 * pangUcumReplacements.get(input); output = input.replace(input, val); } return
	 * output; }
	 */

	public String getDimensions(Term term) {
		String dimension = null;
		try {
			dimension = new PanExpressionComposer().compose(new Converter(model, handlers).convert(term), false);
		} catch (Exception e) {
			logger.debug("getDimensions Exception:" + e.getMessage());
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

	private String checkDecimalUnit(String decimal) {
		// convert 0.001 -> 10*-3, 100 -> 10*2
		String n = null;
		try {
			// checking valid float using parseInt() method
			float f = Float.parseFloat(decimal);
			NumberFormat formatter = new DecimalFormat();
			formatter = new DecimalFormat("0.#E0");
			String exp = formatter.format(f);
			if (exp.startsWith("1E")) {
				n = 10 + "*" + exp.substring(exp.lastIndexOf("E") + 1);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return n;
	}

	private ArrayList<HashMap<String, Object>> getQUDTQuantities(String dimension, String unit, String canonUnit) {
		//ArrayList<String> quantitiesFinal = null;
		ArrayList<HashMap<String, Object>> quantitiesFinal = new ArrayList<HashMap<String, Object>> ();
		List<HashMap<String, String>> jsnArr = null;
		HashMap<String, HashMap<String,Object>> mymap = new HashMap<>();

		// 1. find quantity by dimension and units
		List<String> subDimList = Arrays.asList(dimension.split("\\."));
		PermutationIterator<String> permIterator = new PermutationIterator<String>((Collection<String>) subDimList);
		while (permIterator.hasNext()) {
			List<String> obj = (List<String>) permIterator.next();
			String dim = String.join(".", obj);
			String pthDim = "$.Quantitites[?(@.DimensionFormatted == \"" + dim + "\")]";
			jsnArr = jsonContext.read(pthDim);
			for (int i = 0; i < jsnArr.size(); i++) {
				Map<String, String> dimensionMap = (HashMap<String, String>) jsnArr.get(i);
				String q = dimensionMap.get("Quantity");
				String u = dimensionMap.get("Ucum");
				String d = dimensionMap.get("DimensionFormatted");
				//int term_id = Integer.parseInt(dimensionMap.get("pang_term_id"));
				String term_id = dimensionMap.get("pang_term_id");
				//HashMap<String, String> concat = new HashMap<String, String>() {{
			        //put(term_id, q);
			    //}};
				if (u != null) {
					//mymap.put(u, concat);
					HashMap<String, Object> temp_quan = new HashMap<>();
					temp_quan.put("id", Integer.parseInt(term_id));
					temp_quan.put("name", q);
				    mymap.put(u, temp_quan);
				}
			}
		}
		//String ucumExists = mymap.get(unit);
		HashMap<String, Object> ucumExists = mymap.get(unit);
		if (ucumExists == null) {
			ucumExists = mymap.get(canonUnit);
		}
		
		if (ucumExists != null) {
			logger.debug("Found quantity(s) by DIMENSION-UNIT Combination:" + ucumExists);
			//ArrayList<HashMap<String, Object>> quantities = new ArrayList<HashMap<String, Object>>();
			quantitiesFinal.add(ucumExists);
			//HashMap<String, String> quantities = ucumExists;
			//quantitiesFinal = ucumExists;
		} else {
			// 2. try to find quantities by units only
			//ArrayList<String> quantitiesByUnt = getQuantitiesByUnit(unit, canonUnit);
			//HashMap<String, String> quantitiesByUnt = getQuantitiesByUnit(unit, canonUnit);
			ArrayList<HashMap<String, Object>> quantitiesByUnt = getQuantitiesByUnit(unit, canonUnit);
			
			if (quantitiesByUnt.size() > 0) {
				logger.debug("Found quantity(s) by UNITS Only:" + unit + " , " + canonUnit);
				quantitiesFinal = quantitiesByUnt;
			} else {
				logger.debug("Found quantity(s) by DIMENSION Only :" + dimension);
				// 3. return all quantities by dimension only
				if (mymap.isEmpty() == false) {
					quantitiesFinal = new ArrayList<HashMap<String, Object>>(new HashSet<>(mymap.values()));
					//Collection<HashMap<Integer, String>> listOfMaps = mymap.values();
					//HashMap<String, String> tempMap = new HashMap<String, String>();
					//for (Iterator<HashMap<String, String>> iterator = mymap.values().iterator(); iterator.hasNext();) {
						//tempMap.putAll(iterator.next());
					//}
					//quantitiesFinal = tempMap;
				}
			}
		}
		return quantitiesFinal;
	}


	private ArrayList<HashMap<String, Object>> getQuantitiesByUnit(String unit, String canonUnit) {
		Set<HashMap<String, Object>> quantities = new HashSet<HashMap<String, Object>>();
		//HashMap<String, String> quantities = new HashMap<String, String>(); 
	
		if (StringUtils.isNotEmpty(unit)) {
			String pthUnit = "$.Quantitites[?(@.Ucum == \"" + unit+ "\")]";
			List<HashMap<String, String>> jsnArr = jsonContext.read(pthUnit);
			for (int i = 0; i < jsnArr.size(); i++) { 
				Map<String, String> m = jsnArr.get(i);
				//String q = m.get("Quantity");
				//String term_id = m.get("pang_term_id");
				// String u = m.get("Ucum");
				// String d = m.get("DimensionFormatted");
				//quantities.add(q);
				//quantities.put(term_id, q);
				HashMap<String, Object> temp_quan = new HashMap<String, Object>() ;
				temp_quan.put("id", Integer.parseInt(m.get("pang_term_id")));
				temp_quan.put("name", m.get("Quantity"));
			    quantities.add(temp_quan);
			}
		}
		// C.m-3 same as C/m3 (if units does not return quantities, try with canon units)
		if(quantities.isEmpty() && StringUtils.isNotEmpty(canonUnit)) { 
			String pthUnit2 = "$.Quantitites[?(@.Ucum == \"" + canonUnit+"\")]";
			List<HashMap<String, String>> jsnArr = jsonContext.read(pthUnit2);
			//System.out.println("pthUnit2 "+pthUnit2);
			for (int i = 0; i < jsnArr.size(); i++) {
				HashMap<String, String> m = jsnArr.get(i);
				//String q = m.get("Quantity");
				//String term_id = m.get("pang_term_id");
				//quantities.add(q);
				//quantities.put(term_id, q);
				HashMap<String, Object> temp_quan = new HashMap<String, Object>() ;
				temp_quan.put("id", Integer.parseInt(m.get("pang_term_id")));
				temp_quan.put("name", m.get("Quantity"));
			    quantities.add(temp_quan);
			}
		} 
		/*
		 * pthUnit = "$.Quantitites[?(@.Ucum == \"" + unit + "\" || @.Ucum == \"" +
		 * canonUnit + "\")]";
		 */

		//return new ArrayList<String>(quantities);
		return new ArrayList<HashMap<String, Object>> (quantities);
	}

}