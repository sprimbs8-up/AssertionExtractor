package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.io.FileWriter;
import java.io.IOException;

public class ErrorChecker {

    private boolean errorFree = true;

    private boolean append = false;


    private ErrorChecker() {
    }

    private static final ErrorChecker instance = new ErrorChecker();

    public static ErrorChecker getInstance() {
        return instance;
    }

    public void setError() {
        errorFree = false;
    }

    public void resetError() {
        errorFree = true;
    }

    public boolean errorOccurred() {
        return !errorFree;
    }

    public void currentInstance(String instance){
        try
        {
            String filename= "MyFile.txt";
            FileWriter fw = new FileWriter(filename,append); //the true will append the new data
            fw.write(instance+"\n");//appends the string to the file
            append = true;
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}
