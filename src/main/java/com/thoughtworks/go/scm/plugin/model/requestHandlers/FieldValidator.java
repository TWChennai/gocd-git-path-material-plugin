package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import java.util.Map;

public interface FieldValidator {
	public void validate(Map<String, Object> fieldValidation);
}
