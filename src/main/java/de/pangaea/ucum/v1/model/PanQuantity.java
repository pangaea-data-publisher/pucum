package de.pangaea.ucum.v1.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PanQuantity {

	private String input = null;
	private String status = null;
	private String status_msg = null;
	private String ucum = null;
	private String fullname = null;
	private String canonicalunit = null;
	private String verbosecanonicalunit = null;
	private String dimension = null;
	private List<String> quantities = null;
	//private List<String> basequantities = null;

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
		return verbosecanonicalunit;
	}

	public void setVerbosecanonicalunit(String verbosecanonicalunit) {
		this.verbosecanonicalunit = verbosecanonicalunit;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public List<String> getQuantities() {
		return quantities;
	}

	public void setQuantities(List<String> quantities) {
		this.quantities = quantities;
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
	
	/*public List<String> getBasequantities() {
		return basequantities;
	}

	public void setBasequantities(List<String> basequantities) {
		this.basequantities = basequantities;
	}*/
	
	@Override
	public String toString() {
		return "[Input=" + input + ", Ucum=" + ucum + ", Dimension=" + dimension + "]";
	}
}


