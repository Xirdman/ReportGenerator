/**
 * Class for custom exception
 * @author Matveev Alexander
 */
public class CustomException extends RuntimeException {
    /**
     * Constructor for exception class
     * @param message message of the exception
     */
    public CustomException(String message) {
        super(message);
    }
}
