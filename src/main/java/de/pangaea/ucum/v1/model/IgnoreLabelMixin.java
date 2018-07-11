package de.pangaea.ucum.v1.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class IgnoreLabelMixin {

	@JsonIgnore
	public abstract String getCanonicalunit();

	@JsonIgnore
	public abstract String getVerbosecanonicalunit();

	@JsonIgnore
	public abstract String getDimension();

	@JsonIgnore
	public abstract List<String> getQuantities();

	@JsonIgnore
	public abstract String getFullname();
}
