import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

//This is the class that collects samples of the nodes logical clocks at predefine intervals of logical time
public class Sampler implements Runnable {

    private CountDownLatch countDownLatch;
    private List<Future<?>> dnFutureList;
    private List<DetectionNodeQues> detectionNodesQues;
    private List<String> dnSamplerList;

    public Sampler (CountDownLatch countDownLatch, List<Future<?>> dnFutureList, List<DetectionNodeQues> detectionNodesQues) {
        this.countDownLatch = countDownLatch;
        this.dnFutureList = dnFutureList;
        this.detectionNodesQues = detectionNodesQues;
        this.dnSamplerList = new ArrayList<>();

    }

    @Override
    public void run() {

        //variable step controls the intervals of cumulative nodes logical time when a sample is collected
        int step = 500;
        boolean allDone;
        int logicalClocksProgress = 0;
        int cumulativeLogicalClocksValue;
        takeASample();
        do {

            //Method areAllTheThreadsDoneWorking checks if there is still a thread working and returns false if there is
            allDone = areAllTheThreadsDoneWorking();

            //if there is still one thread working than it checks if enough cumulative nodes logical time passed
            //to collect a new sample
            if(!allDone) {

                //the progress of the nodes cumulative logical time is calculated using the formula:
                //cumulativeLogicalClocksValue = getCumulativeLogicalClocksValue()/step and since both step and the returned value of
                //getCumulativeLogicalClocksValue method are of int type the quotient value won't change until "step" amount
                //of logical time passes
                cumulativeLogicalClocksValue = getCumulativeLogicalClocksValue()/step;
                if (logicalClocksProgress < cumulativeLogicalClocksValue) {

                    //if cumulativeLogicalClocksValue is greater than the logicalClocksProgress
                    //than it means more then "step" logical units has passed since the last sample was taken
                    //the logicalClocksProgress updates its value with the new cumulativeLogicalClocksValue
                    //and a new sample is taken
                    logicalClocksProgress = cumulativeLogicalClocksValue;
                    takeASample();
                }
            }
        } while (!allDone);

        //When all threads are done, a last sample with the final value for the node logical clocks is taken
        takeASample();

        displayResults();

        countDownLatch.countDown();
    }

    //Method displayResults print the results both on screen and save them into a file called Report.txt
    //These results are the samples of the logical clocks collected
    //and the eventsJournal entries for each detection node
    private void displayResults() {
        printResultToScreen();
        saveResultsToFile();
    }

    private void printResultToScreen() {
        System.out.println("-------------------------------------------------------");
        System.out.println("-------------------------------------------------------");

        System.out.println("The samples for this simulation are:");
        for (String sample : dnSamplerList) {
            System.out.println(sample);
        }

        System.out.println("-------------------------------------------------------");
        System.out.println("-------------------------------------------------------");

        System.out.println("Nodes Journals");


        for(DetectionNodeQues dnq:detectionNodesQues) {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("Journal for DN "+dnq.getId());
            System.out.println(" ");

            for(String jn:dnq.getEventsJournal()) {
                System.out.println(jn);
            }
        }

        System.out.println("-------------------------------------------------------");
        System.out.println("-------------------------------------------------------");

    }

    private void saveResultsToFile() {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Report.txt"), "UTF-8"))) {
            out.write("-------------------------------------------------------\n");
            out.write("-------------------------------------------------------\n");

            out.write("The samples for this simulation are:\n");
            for (String sample : dnSamplerList) {
                out.write(sample);
                out.newLine();
            }

            out.write("-------------------------------------------------------\n");
            out.write("-------------------------------------------------------\n");

            out.write("Nodes Journals\n");


            for(DetectionNodeQues dnq:detectionNodesQues) {
                out.write("++++++++++++++++++++++++++++++++++++++++++++++\n");
                out.write("Journal for DN "+dnq.getId());
                out.newLine();

                for(String jn:dnq.getEventsJournal()) {
                    out.write(jn);
                    out.newLine();
                }
                out.write("++++++++++++++++++++++++++++++++++++++++++++++\n");
            }

            out.write("-------------------------------------------------------\n");
            out.write("-------------------------------------------------------\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This method saves the nodes logical clocks values at a certain moment in time
    private void takeASample() {
        String dnSample=" | ";
        for(DetectionNodeQues dnQues : detectionNodesQues) {
            dnSample =dnSample+"DN "+dnQues.getId()+" has LC = "+dnQues.getLogicalClock()+" | ";
        }
        dnSamplerList.add(dnSample);
    }

    //This method checks if there is at least one thread still working and returns false if there is
    private boolean areAllTheThreadsDoneWorking() {
        boolean allDone = true;
        for (Future<?> f:dnFutureList){
            if (f.isDone() == false) {
                allDone = false;
                break;
            }
        }

        return allDone;

    }

    //This methods calculates the cumulative value of the nodes logical clocks to be used in determine if its time
    //for a new sample to be taken
    private int getCumulativeLogicalClocksValue() {
        int sum = 0;
        for (DetectionNodeQues dnQues : detectionNodesQues) {
            sum = sum + dnQues.getLogicalClock();
        }
        return sum;
    }
}
