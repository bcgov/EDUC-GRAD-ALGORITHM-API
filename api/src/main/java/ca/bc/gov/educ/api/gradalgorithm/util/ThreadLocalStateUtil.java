package ca.bc.gov.educ.api.gradalgorithm.util;

public class ThreadLocalStateUtil {
    private static InheritableThreadLocal<String> transaction = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> user = new InheritableThreadLocal<String>();
    private static final InheritableThreadLocal<String> requestSource = new InheritableThreadLocal<String>();

    /**
     * Set the current correlationID for this thread
     *
     * @param correlationID
     */
    public static void setCorrelationID(String correlationID){
        transaction.set(correlationID);
    }

    /**
     * Get the current correlationID for this thread
     *
     * @return the correlationID, or null if it is unknown.
     */
    public static String getCorrelationID() {
        return transaction.get();
    }

    /**
     * Set the requestSource for this thread
     *
     * @param reqSource
     */
    public static void setRequestSource(String reqSource){
        requestSource.set(reqSource);
    }
    /**
     * Get the requestSource for this thread
     *
     * @return the requestSource, or null if it is unknown.
     */
    public static String getRequestSource() {
        return requestSource.get();
    }

    /**
     * Set the current user for this thread
     */
    public static void setCurrentUser(String currentUser){
        user.set(currentUser);
    }
    /**
     * Get the current user for this thread
     *
     * @return the username of the current user, or null if it is unknown.
     */
    public static String getCurrentUser() {
        return user.get();
    }

    public static void clear() {
        transaction.remove();
        user.remove();
        requestSource.remove();
    }
}
