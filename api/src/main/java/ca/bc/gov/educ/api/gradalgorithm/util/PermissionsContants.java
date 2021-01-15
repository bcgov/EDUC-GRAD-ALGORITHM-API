package ca.bc.gov.educ.api.gradalgorithm.util;

public interface PermissionsContants {
	String _PREFIX = "#oauth2.hasAnyScope('";
	String _SUFFIX = "')";

	String RUN_GRAD_ALGORITHM = _PREFIX + "RUN_GRAD_ALGORITHM" + _SUFFIX;
}
