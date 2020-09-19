package de.pangaea.ucum.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

import javax.ws.rs.ApplicationPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumModel;
import org.glassfish.jersey.server.ResourceConfig;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

@ApplicationPath("/v1")
public class PanUcumApp extends ResourceConfig {
  private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

  public static final String PROPERTIES_FILE = "config.properties";
  
	private final Properties properties = new Properties();
	private final URL ucumEssenceFile;
	private final URL quantityFile;
	private final URL mappingFile;
	private final UcumEssenceService ucumSvc;
	private final HashMap<String, String> pangUcumMapping;
	private final UcumModel ucumModel;
	private final ReadContext jsonContext;
	 
	public PanUcumApp() throws IOException, UcumException {
		final ClassLoader classLoader = getClass().getClassLoader();
    
		// Read the configuration file
		try (InputStream inputStream = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
      properties.load(inputStream);
		}

		// Add a package used to scan for components //
		// packages(this.getClass().getPackage().getName());
		ucumEssenceFile = classLoader.getResource(properties.getProperty("ucum_essence_file"));
		quantityFile = classLoader.getResource(properties.getProperty("quantity_file"));

		mappingFile = classLoader.getResource(properties.getProperty("mapping_file"));
		// Read mapping file : unitscorr29062018.txt
		pangUcumMapping = new HashMap<String, String>();
		//FileReader fileReader;
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mappingFile.openStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length >= 2) {
					String key = parts[0];
					String value = parts[1];
					// ignored String source = parts[2];
					pangUcumMapping.put(key, value);
				} else {
					logger.warn("Ignoring line: " + line);
				}
			}
		}

		// read qudt json file
		try (InputStream in = quantityFile.openStream()) {
			jsonContext = JsonPath.parse(in);
		}
		
    try (InputStream in = ucumEssenceFile.openStream()) {
      ucumSvc = new UcumEssenceService(in);
      ucumModel = ucumSvc.getModel();
    }
    
    // start the app:
    final String pkg = this.getClass().getPackage().getName();
    packages(pkg, pkg.concat(".model"));
    register(new PanUcumService(this));
	}

	public UcumEssenceService getUcumSvc() {
		return ucumSvc;
	}

	public HashMap<String, String> getPangUcumMapping() {
		return pangUcumMapping;
	}

	public org.fhir.ucum.UcumModel getUcumModel() {
		return ucumModel;
	}

	public ReadContext getJsonContext() {
		return jsonContext;
	}

}