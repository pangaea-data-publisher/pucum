package de.pangaea.ucum.v1.model;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class IgnoreLabelMixin {

	@JsonIgnore
	public abstract String getCanonicalunit();

	@JsonIgnore
	public abstract String getVerbosecanonicalunit();

	@JsonIgnore
	public abstract String getDimension();

	@JsonIgnore
	public abstract HashMap<String, String> getQudt_quantity();

	@JsonIgnore
	public abstract String getUcum_quantity();

	@JsonIgnore
	public abstract String getFullname();
}
