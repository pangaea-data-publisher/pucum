package de.pangaea.ucum.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumModel;
import org.glassfish.jersey.server.ResourceConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.sun.tools.sjavac.Log;


@ApplicationPath("/v1")
public class PanUcumApp extends ResourceConfig {

	public static final String PROPERTIES_FILE = "config.properties";
	public static Properties properties = new Properties();
	private static final Logger logger = Logger.getLogger(PanUcumApp.class);
	private static String ucumEssenceFilePath;
	private static String quantityFilePath;
	private static File mappingFile = null;
	private static UcumEssenceService ucumSvc = null;
	private static HashMap<String, String> pangUcumMapping = null;
	private static UcumModel ucumModel = null;
	private static XPath xpath = null;
	private static Document doc = null;

	public PanUcumApp() {
		//packages("de.pangaea.ucum.v1");
		//register(PanUcumService.class);
		BasicConfigurator.configure();
		ClassLoader classLoader = getClass().getClassLoader();
		// Read the configuration file
		properties = readProperties();
		// Add a package used to scan for components //
		// packages(this.getClass().getPackage().getName());
		ucumEssenceFilePath = classLoader.getResource(properties.getProperty("ucum_essence_file")).getPath();
		quantityFilePath = classLoader.getResource(properties.getProperty("quantity_file")).getPath();

		//String mappingFileDecoded = decodeFilePath(classLoader.getResource(properties.getProperty("mapping_file")).getPath());
		String mappingFileDecoded = classLoader.getResource(properties.getProperty("mapping_file")).getPath();
		mappingFile = new File(mappingFileDecoded);
		// Read mapping file : unitscorr29062018.txt
		pangUcumMapping = new HashMap<String, String>();
		FileReader fileReader;
		try {
			fileReader = new FileReader(mappingFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length >= 2) {
					String key = parts[0];
					String value = parts[1];
					pangUcumMapping.put(key, value);
				} else {
					logger.debug("Ignoring line: " + line);
				}
			}
			fileReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * File file = new File(quantityFile); JAXBContext jaxbContext = null; try {
		 * jaxbContext = JAXBContext.newInstance(Quantities.class); Unmarshaller
		 * jaxbUnmarshaller = jaxbContext.createUnmarshaller(); qudt = (Quantities)
		 * jaxbUnmarshaller.unmarshal(file); } catch (JAXBException e) {
		 * logger.debug("JAXBException: " + e.getMessage()); }
		 */

		// Read qudt xml file quantities.xml
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			String quantityFileDecoded = decodeFilePath(quantityFilePath);
			doc = builder.parse(quantityFileDecoded);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
		} catch (ParserConfigurationException e1) {
			logger.debug("ParserConfigurationException: " + e1.getMessage());
		} catch (SAXException e2) {
			logger.debug("SAXException: " + e2.getMessage());
		} catch (IOException e3) {
			logger.debug("IOException: " + e3.getMessage());
		}

	}

	private static String decodeFilePath(String path) {
		String fileDecoded = null;
		try {
			fileDecoded = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e4) {
			logger.debug("UnsupportedEncodingException: " + e4.getMessage());
		}
		return fileDecoded;
	}

	private HashMap<String, String> parseMapping(String m) {
		HashMap<String, String> mappings = new HashMap<String, String>();
		String[] tokens = m.split(";");
		for (int i = 0; i < tokens.length; i++) {
			String[] to_from = tokens[i].split(":");
			mappings.put(to_from[0], to_from[1]);
		}
		return mappings;
	}

	private Properties readProperties() {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		if (inputStream != null) {
			try {
				properties.load(inputStream);
			} catch (IOException e) {
				logger.debug("IOException: " + e.getMessage());
			}
		}
		return properties;
	}

	public static UcumEssenceService getUcumSvc() {
		if (ucumSvc == null) {
			try {
				FileInputStream fis = null;
				try {
					String ucumEssenceFileDecoded = decodeFilePath(ucumEssenceFilePath);
					fis = new FileInputStream(ucumEssenceFileDecoded);
				} catch (FileNotFoundException e) {
					logger.debug("FileNotFoundException: " + e.getMessage());
				}
				ucumSvc = new UcumEssenceService(fis);
				ucumModel = ucumSvc.getModel();
			} catch (UcumException e) {
				logger.debug("UcumException: " + e.getMessage());
			}
		}
		return ucumSvc;
	}

	public static ArrayList<String> getQuantitiesByDimensionUnits(String dimension) {
		Set<String> quantities = new HashSet<String>();
		List<String> subDimList = Arrays.asList(dimension.split("\\."));
		PermutationIterator<String> permIterator = new PermutationIterator<String>((Collection<String>) subDimList);
		while (permIterator.hasNext()) {
			List<String> obj = (List<String>) permIterator.next();
			String result = String.join(".", obj);
			try {
				String expression = "quantities/quantity[dimension='" + result + "']/name";
				NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					quantities.add(nodeList.item(i).getTextContent());
				}
			} catch (XPathExpressionException e) {
				logger.debug("XPathExpressionException: " + e.getMessage());
			}
		}
		return new ArrayList<String>(quantities);
	}

	public void setUcumSvc(UcumEssenceService service) {
		ucumSvc = service;
	}

	public static HashMap<String, String> getPangUcumMapping() {
		return pangUcumMapping;
	}

	public void setPangUcumMapping(HashMap<String, String> mapping) {
		pangUcumMapping = mapping;
	}

	public static org.fhir.ucum.UcumModel getUcumModel() {
		return ucumModel;
	}

	public static void setUcumModel(org.fhir.ucum.UcumModel ucumModel) {
		PanUcumApp.ucumModel = ucumModel;
	}

}