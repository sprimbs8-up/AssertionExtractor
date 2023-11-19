package de.uni_passau.fim.se2.assertion_exctractor.utils;

import me.tongfei.progressbar.ProgressBar;

public final class ProgressBarContainer {
    private static ProgressBarContainer instance;

    private ProgressBar progressBar;

    private ProgressBarContainer() {
    }

    public static ProgressBarContainer getInstance() {
        if (instance == null) {
            instance = new ProgressBarContainer();
        }
        return instance;
    }

    public void setProgressBar(String description, int counter){
        progressBar = new ProgressBar(description,counter);
    }

    public void notifyStart(){
        progressBar.start();
    }

    public void notifyStep(){
        progressBar.step();
    }

    public void notifyStop(){
        progressBar.stop();
    }

    public int getTotalCount(){
        return (int) progressBar.getMax();
    }
}
