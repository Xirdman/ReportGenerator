/**
 * Class for data
 * @author Matveev Alexander
 */
public class TaskData {
    private String fio;
    private String date;
    private String number;

    /**
     * Constructor for data
     * @param number number of record
     * @param date date from record
     * @param fio fio from record
     */
    public TaskData(String number, String date, String fio) {
        this.fio = fio;
        this.date = date;
        this.number = number;
    }

    /**
     * Method to get fio
     * @return fio from record
     */
    public String getFio() {
        return fio;
    }
    /**
     * Method to get date from record
     * @return fio from record
     */
    public String getDate() {
        return date;
    }
    /**
     * Method to get number from record
     * @return number from record
     */
    public String getNumber() {
        return number;
    }
}
