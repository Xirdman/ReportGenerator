/**
 * Entry point class
 *
 * @author Matveev Alexander
 */
public class Generator {
    /**
     * Entry point of program
     *
     * @param args arguments of generator. First argument is name of xml settings file. Second argument for data source file. Third argument name of result data file
     */
    public static void main(String[] args) {
        GeneratorController myController = new GeneratorController();
        try {
            myController.setSettingsViaXml(args[0]);
            myController.loadDataFromFile(args[1]);
            myController.writeDataToFile(args[2]);
        } catch (CustomException e) {
            System.out.println("Error:\n" + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid number of arguments");
        }
    }
}
