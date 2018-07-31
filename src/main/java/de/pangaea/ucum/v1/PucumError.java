package de.pangaea.ucum.v1;

public enum PucumError {
	VALID_UNITS("200_VALID", "The unit is compliant with UCUM."), 
	BAD_UNITS_TRANSLATED("400_UNITS_TRANSLATED", "The input has been translated into a UCUM-compliant unit."),
	INVALID_UNITS("404_INVALID", "This unit is not compliant with UCUM."),
	QUANTITY_NOT_FOUND("206_QUANTITY_NOT_FOUND", "The dimension of the given units is missing, therefore its quantities are not found."),
	QUANTITY_FOUND("201_QUANTITY_FOUND", "The ucum and/or qudt quantity of the given units is found.");
	
	private final String id;
	private final String message;

	PucumError(String id, String message) {
		this.id = id;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
}