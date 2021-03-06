package de.pangaea.ucum.v1.model;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PanQuantity {

	private String input = null;
	private String status = null;
	private String status_msg = null;
	private String ucum = null;
	private String fullname = null;
	private String canonicalunit = null;
	private String verbose_canonicalunit = null;
	private String dimension = null;
	//private List<String> qudt_quantity = null;
	
	private List<HashMap<String, Object>> qudt_quantity = null;
	//private HashMap<String, String> qudt_quantity = new HashMap<String, String>(); 
	private String ucum_quantity = null;

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getUcum() {
		return ucum;
	}

	public void setUcum(String ucum) {
		this.ucum = ucum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCanonicalunit() {
		return canonicalunit;
	}

	public void setCanonicalunit(String canonicalunit) {
		this.canonicalunit = canonicalunit;
	}

	public String getVerbosecanonicalunit() {
		return verbose_canonicalunit;
	}

	public void setVerbosecanonicalunit(String verbosecanonicalunit) {
		this.verbose_canonicalunit = verbosecanonicalunit;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getStatus_msg() {
		return status_msg;
	}

	public void setStatus_msg(String status_msg) {
		this.status_msg = status_msg;
	}

	public List<HashMap<String, Object>> getQudt_quantity() {
		return qudt_quantity;
	}

	public void setQudt_quantity(List<HashMap<String, Object>> qudt_quantities) {
		this.qudt_quantity = qudt_quantities;
	}

	public String getUcum_quantity() {
		return ucum_quantity;
	}

	public void setUcum_quantity(String ucum_quantity) {
		this.ucum_quantity = ucum_quantity;
	}

	@Override
	public String toString() {
		return "[Input=" + input + ", Ucum=" + ucum + ", Dimension=" + dimension + "]";
	}
}
