import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

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
        int step = 500;
        boolean allDone = false;
        int i=0;
        int logicalClocksProgress = 0;
        while (!allDone) {
            allDone = isAThreadStillWorking();
            if(!allDone) {
                if (logicalClocksProgress < (cumulativeLogicalClocksValue()/step)) {
                    logicalClocksProgress = cumulativeLogicalClocksValue()/step;
                    takeASample();
                }
            }
        }
        System.out.println("The samples for this simulation are:");
        for (String sample : dnSamplerList) {
            System.out.println(sample);
        }
        countDownLatch.countDown();
    }

    private void takeASample() {
        String dnSample="";
        for(DetectionNodeQues dnQues : detectionNodesQues) {
            dnSample =dnSample+"DN "+dnQues.getId()+" has LC = "+dnQues.getLogicalClock()+" | ";
        }
        dnSamplerList.add(dnSample);
    }

    private boolean isAThreadStillWorking() {
        boolean allDone = true;
        for (Future<?> f:dnFutureList){
            if (f.isDone() == false) {
                allDone = false;
                break;
            }
        }

        return allDone;

    }

    private int cumulativeLogicalClocksValue() {
        int sum = 0;
        for (DetectionNodeQues dnQues : detectionNodesQues) {
            sum = sum + dnQues.getLogicalClock();
        }
        return sum;
    }
}
