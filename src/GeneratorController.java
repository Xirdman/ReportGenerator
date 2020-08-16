import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to work with data. Read data from file, read settings form file and generate reports
 */
public class GeneratorController {
    private static final String[] FILE_HEADER = {"Номер", "Дата", "ФИО"};

    private int pageWidth;
    private int pageHeight;
    private int numberWidth;
    private int fioWidth;
    private int dateWidth;
    private List<TaskData> taskData;

    private int rowCounter;
    int rowsToWrite;

    /**
     * Constructor of class
     */
    public GeneratorController() {
        pageWidth = 0;
        pageHeight = 0;
        numberWidth = 0;
        fioWidth = 0;
        dateWidth = 0;
        taskData = new ArrayList<>();
        rowsToWrite = 0;
    }

    /**
     * Method to set settings from XML file
     *
     * @param xmlFileName name of the XML file with settings
     * @throws CustomException throws custom exception if something went wrong
     */
    public void setSettingsViaXml(String xmlFileName) throws CustomException {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFileName);
            Node root = document.getDocumentElement();
            NodeList settings = root.getChildNodes();
            for (int i = 0; i < settings.getLength(); i++) {
                Node node = settings.item(i);
                NodeList innerList = node.getChildNodes();
                for (int j = 0; j < innerList.getLength(); j++) {
                    Node innerNode = innerList.item(j);
                    switch (innerNode.getNodeName()) {
                        case "height":
                            pageHeight = Integer.parseInt(innerNode.getTextContent());
                            break;
                        case "width":
                            pageWidth = Integer.parseInt(innerNode.getTextContent());
                            break;
                        case "column":
                            NodeList columnNodes = innerNode.getChildNodes();
                            Node columnTitle = columnNodes.item(1);
                            Node columnWidth = columnNodes.item(3);
                            switch (columnTitle.getTextContent()) {
                                case "Номер":
                                    numberWidth = Integer.parseInt(columnWidth.getTextContent());
                                    break;
                                case "ФИО":
                                    fioWidth = Integer.parseInt(columnWidth.getTextContent());
                                    break;
                                case "Дата":
                                    dateWidth = Integer.parseInt(columnWidth.getTextContent());
                                    break;
                            }
                            break;
                    }
                }
                if ((fioWidth + dateWidth + numberWidth + 10) > pageWidth)
                    throw new CustomException("Fio,Date and number cant be fitted with page width.\nPlease check settings");
                else {
                    //If in settings left empty space it will be used by fio field
                    fioWidth = pageWidth - 10 - fioWidth - dateWidth;
                }
                if (pageHeight < 3)
                    throw new CustomException("Page Height is to small\nPlease check settings");
            }
        } catch (ParserConfigurationException e) {
            throw new CustomException("Parser Configuration Exception occurred");
        } catch (SAXException e) {
            throw new CustomException("SAXException occurred");
        } catch (IOException e) {
            throw new CustomException("Input Output Exception occurred");
        }

    }

    /**
     * Method to load data for future report
     *
     * @param fileName name of file with formatted data to read
     * @throws CustomException throws custom exception if something went wrong
     */
    public void loadDataFromFile(String fileName) throws CustomException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                String[] parsedLineData = string.split("\t");
                TaskData singleTaskData = new TaskData(parsedLineData[0],
                        parsedLineData[1],
                        parsedLineData[2]);
                taskData.add(singleTaskData);
            }
        } catch (FileNotFoundException e) {
            throw new CustomException("File not found exception occurred while reading from source file");
        } catch (IOException e) {
            throw new CustomException("Input Output Exception occurred while reading from source file");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CustomException("Data source file not formatted correctly");
        }
    }

    /**
     * Method to generate report in file
     *
     * @param fileName name of file for report
     * @throws CustomException throws custom exception if something went wrong
     */
    public void writeDataToFile(String fileName) throws CustomException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            rowCounter = 0;
            writeHeader(bufferedWriter);
            for (TaskData taskData : taskData) {
                String toFile = makeStringToWrite(taskData.getFio(), taskData.getNumber(), taskData.getDate()).toString();
                if (rowCounter + rowsToWrite <= pageHeight) {
                    bufferedWriter.write(toFile);
                    rowCounter += rowsToWrite;
                    rowsToWrite = 0;
                } else {
                    writeNewPage(bufferedWriter);
                    bufferedWriter.write(toFile);
                    rowCounter += rowsToWrite;
                    rowsToWrite = 0;
                }
                printRowSplitter(bufferedWriter);
            }
        } catch (IOException e) {
            throw new CustomException("Input Output Exception occurred while write to data to file");
        }
    }

    private void writeHeader(BufferedWriter bufferedWriter) throws IOException {
        writeRowsToFile(bufferedWriter, FILE_HEADER[0], FILE_HEADER[1], FILE_HEADER[2]);
    }

    private void writeRowsToFile(BufferedWriter bufferedWriter, String numberValue, String dateValue, String fioValue) throws IOException {
        StringBuilder nameColumn = new StringBuilder(numberValue);
        StringBuilder dateColumn = new StringBuilder(dateValue);
        StringBuilder fioColumn = new StringBuilder(fioValue);
        while ((nameColumn.length() != 0) || (dateColumn.length() != 0) || (fioColumn.length() != 0)) {
            bufferedWriter.write("| ");
            nameColumn = writeSomething(bufferedWriter, nameColumn, numberWidth);

            bufferedWriter.write(" | ");
            dateColumn = writeSomething(bufferedWriter, dateColumn, dateWidth);

            bufferedWriter.write(" | ");
            fioColumn = writeSomething(bufferedWriter, fioColumn, fioWidth);

            bufferedWriter.write(" |\n");
            rowCounter++;
            checkRows(bufferedWriter);
        }
        printRowSplitter(bufferedWriter);
    }

    private StringBuilder writeSomething(BufferedWriter bufferedWriter, StringBuilder stringBuilder, int width) throws IOException {
        if (stringBuilder.length() > width) {
            String toFile = stringBuilder.substring(0, width);
            bufferedWriter.write(toFile);
            return stringBuilder.delete(0, width);
        } else {
            bufferedWriter.write(stringBuilder.toString());
            for (int i = stringBuilder.length(); i < width; i++)
                bufferedWriter.write(" ");
            return new StringBuilder();
        }
    }

    private void printRowSplitter(BufferedWriter bufferedWriter) throws IOException {
        for (int i = 0; i < pageWidth; i++) {
            bufferedWriter.write("-");
        }
        rowCounter++;
        bufferedWriter.write("\n");
    }

    private void checkRows(BufferedWriter bufferedWriter) throws IOException {
        if (rowCounter >= pageHeight) {
            writeNewPage(bufferedWriter);
        }
    }

    private void writeNewPage(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("~\n");
        rowCounter = 0;
        writeHeader(bufferedWriter);
    }

    private StringBuilder makeStringToWrite(String fio, String number, String date) {
        StringBuilder currentString = new StringBuilder("| ");
        StringBuilder numberBuilder = new StringBuilder(number);
        StringBuilder dateBuilder = new StringBuilder(date);
        StringBuilder fioBuilder = new StringBuilder(fio);
        if (number.length() <= numberWidth) {
            currentString.append(number);
            for (int i = number.length(); i < numberWidth; i++)
                currentString.append(" ");
            numberBuilder = new StringBuilder();
        } else {
            String toFile = numberBuilder.substring(0, numberWidth);
            currentString.append(toFile);
            numberBuilder.delete(0, numberWidth);
        }
        currentString.append(" | ");

        String[] dateArray = getFormattedDateString(date);
        int spaceLeft = dateWidth;
        boolean isSingleCharFromDateAdded = false;
        for (int i = 0; i < dateArray.length; i++) {
            if (dateArray[i].length() <= spaceLeft) {
                currentString.append(dateArray[i]);
                isSingleCharFromDateAdded = true;
                spaceLeft -= dateArray[i].length();
                dateBuilder.delete(0, dateArray[i].length());
            } else {
                break;
            }
        }
        if (!isSingleCharFromDateAdded) {
            currentString.append(dateBuilder.substring(0, dateWidth));
            spaceLeft = 0;
            dateBuilder.delete(0, dateWidth);
        }
        for (int i = 0; i < spaceLeft; i++)
            currentString.append(" ");

        currentString.append(" | ");
        String[] nameArray = getFormattedNameString(fio);
        spaceLeft = fioWidth;
        boolean isSingleCharFromNameAdded = false;
        for (int i = 0; i < nameArray.length; i++) {
            if (nameArray[i].length() <= spaceLeft) {
                currentString.append(nameArray[i]);
                isSingleCharFromNameAdded = true;
                spaceLeft -= nameArray[i].length();
                fioBuilder.delete(0, nameArray[i].length());
            } else {
                break;
            }
        }
        if (!isSingleCharFromNameAdded) {
            currentString.append(fioBuilder.substring(0, fioWidth));
            spaceLeft = 0;
            fioBuilder.delete(0, fioWidth);
        }
        for (int i = 0; i < spaceLeft; i++)
            currentString.append(" ");

        currentString.append(" |\n");
        rowsToWrite++;

        if ((fioBuilder.length() != 0) || (dateBuilder.length() != 0) || (numberBuilder.length() != 0)) {
            currentString.append(makeStringToWrite(fioBuilder.toString(), numberBuilder.toString(), dateBuilder.toString().trim()));
        }
        return currentString;
    }

    private String[] getFormattedDateString(String date) {
        if (date.contains("/")) {
            String[] result = date.split("/");
            for (int i = 0; i < result.length - 1; i++) {
                result[i] += "/";
            }
            return result;
        }
        if (date.contains(".")) {
            String[] result = date.split("\\.");
            for (int i = 0; i < result.length - 1; i++) {
                result[i] += ".";
            }
            return result;
        }
        return new String[]{date};
    }

    private String[] getFormattedNameString(String name) {
        if (name.contains(" ")) {
            String[] result = name.split("\\s");
            for (int i = 0; i < result.length - 1; i++)
                result[i] += " ";
        }
        return new String[]{name};
    }
}
