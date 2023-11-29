package psiprobe.tools.logging.logback13;

/**
 * Wraps a Logback logger factory from a given web application class loader.
 *
 * <p>
 * All Logback classes are loaded via the given class loader and not via psi-probe's own class
 * loader. For this reasons, all methods on Logback objects are invoked via reflection.
 * </p>
 * <p>
 * This way, we can even handle different versions of Logback embedded in different WARs.
 * </p>
 */
public class SLF4JProviderBindingException extends Exception {
    public SLF4JProviderBindingException(String message) {
        super(message);
    }
}
