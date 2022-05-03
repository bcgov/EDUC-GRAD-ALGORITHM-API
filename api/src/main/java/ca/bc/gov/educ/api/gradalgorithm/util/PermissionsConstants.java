package ca.bc.gov.educ.api.gradalgorithm.util;

public class PermissionsConstants {
	private PermissionsConstants() {}
	public static final String _PREFIX = "hasAuthority('";
	public static final String _SUFFIX = "')";

	public static final String RUN_GRAD_ALGORITHM = _PREFIX + "SCOPE_RUN_GRAD_ALGORITHM" + _SUFFIX;
}
