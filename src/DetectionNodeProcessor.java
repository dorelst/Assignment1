import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DetectionNodeProcessor implements Runnable {
    private int nodeId;
    private DetectionNodeQues processorQues;
    private List<DetectionNodeQues> otherProcessorsQues;
    private int numberOfIterations;
    private String[] randomMessages;
    private CountDownLatch countDownLatch;

    public int getNodeId() {
        return nodeId;
    }

    public DetectionNodeQues getProcessorQues() {
        return processorQues;
    }

    public List<DetectionNodeQues> getOtherProcessorsQues() {
        return otherProcessorsQues;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public String[] getRandomMessages() {
        return randomMessages;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }


    public DetectionNodeProcessor(int nodeId, DetectionNodeQues processorQues, List<DetectionNodeQues> otherProcessorsQues, int numberOfIterations, CountDownLatch countDownLatch) {
        this.nodeId = nodeId;
        this.processorQues = processorQues;
        this.otherProcessorsQues = otherProcessorsQues;
        this.numberOfIterations = numberOfIterations;
        this.countDownLatch = countDownLatch;
        this.randomMessages = new String[]{"asfhas kfhlajsfl asfaoafda", "boiupoeiwr wwehwqe puupiu", "casdfk ahsfdklj uyotytyut", "duwewe mzvcxzc pipiet", "enzvzz zxczx opoetr"};
    }

    public void run() {
        System.out.println("Inside Node " + getNodeId());

        int senderNodeId;
        int typeOfEvent;

        for (int i = 0; i < getNumberOfIterations(); i++) {
            System.out.println("Inside node " + getNodeId() + " and i = " + i);
            typeOfEvent = generateTypeOfEvent();
            switch (typeOfEvent) {
                case 0:
                    internalEvent();
                    break;
                case 1:
                    senderNodeId = generateNodeId();
                    receivingEvent(senderNodeId);
                    break;
                case 2:
                    int destinationNodeId = generateNodeId();
                    sendingEvent(destinationNodeId);
                    break;
            }
        }
        countDownLatch.countDown();
    }

    private int generateNodeId() {
        int nodeId = (int)(Math.random()*3);
        return nodeId;
    }

    private int generateTypeOfEvent() {
        int typeOfEvent = (int)(Math.random()*100);
        if ( (typeOfEvent >= 0) && (typeOfEvent <= 33)) {
            return 0;
        }
        if ( (typeOfEvent >= 34) && (typeOfEvent <= 66)) {
            return 1;
        }
        return 2;
    }

    private void internalEvent() {
        int typeOfInternalEvent = (int)(Math.random()*2);
        if (typeOfInternalEvent == 0) {
            generateMessage();
        } else {
            anomalyDetection();
        }
    }

    private String createRandomMessage(String eventType) {
        String message;
        int chooseMessage = (int)(Math.random()*5);
        message = getNodeId()+" "+eventType+" "+getRandomMessages()[chooseMessage];
        return message;
    }

    private void generateMessage() {
        processorQues.setLogicalClock(processorQues.getLogicalClock()+1);
        String message = processorQues.getLogicalClock()+" "+createRandomMessage("message_generation");
        processorQues.addMessageToEventsJournal(message);
    }

    private void anomalyDetection() {
        processorQues.setLogicalClock(processorQues.getLogicalClock()+1);
        String message;
        if (processorQues.getReceivedMessages().isEmpty()) {
            message = processorQues.getLogicalClock()+" "+getNodeId()+" "+"detection_event"+" "+"No messages available to analyze";
            processorQues.addMessageToEventsJournal(message);
            return;
        }
        int abnormalMessage = (int) (Math.random() * 10);
        message = processorQues.getLogicalClock() + " " + getNodeId() + " " + "detection_event" + " " + processorQues.getReceivedMessages().poll();
        if (abnormalMessage == 6) {
            message = message + " " + "ABNORMAL_MESSAGE";
        } else {
            message = message + " " + "NORMAL_MESSAGE";
        }
        processorQues.addMessageToEventsJournal(message);
    }

    private void receivingEvent(int senderNodeId) {
        processorQues.setLogicalClock(processorQues.getLogicalClock()+1);
        String message;
        int senderLogicalClock;
        message = processorQues.getLogicalClock()+" "+getNodeId()+" "+"receiving_event";
        if (processorQues.getReceivingQue().isEmpty()) {
            message = message+" "+"Receiving que is empty";
            processorQues.addMessageToEventsJournal(message);
            return;
        }
        String incomingMessageFromSelectedNode = processorQues.getMessageFromReceivingQue(senderNodeId);
        if (incomingMessageFromSelectedNode.equals("")) {
            message = message + "No message received from Detection Node "+senderNodeId;
        } else {
            senderLogicalClock = processorQues.getNodeLogicalClockFromIncomingMessage(incomingMessageFromSelectedNode);
            if (senderLogicalClock >= processorQues.getLogicalClock()) {
                processorQues.setLogicalClock(senderLogicalClock+1);
                message = processorQues.getLogicalClock()+" "+getNodeId()+" "+"receiving_event";
            }
            message = message + " " + incomingMessageFromSelectedNode;
        }
        processorQues.addMessageToReceivedMessages(message);
        processorQues.addMessageToEventsJournal(message);
    }

    private void sendingEvent(int destinationNodeId) {
        processorQues.setLogicalClock(processorQues.getLogicalClock()+1);
        String message = processorQues.getLogicalClock()+" "+createRandomMessage("sending_event");

        synchronized (getOtherProcessorsQues().get(destinationNodeId).getReceivingQue()){
            getOtherProcessorsQues().get(destinationNodeId).addMessageToReceivingQue(message);
        }

        processorQues.addMessageToSendingQue(message);
        processorQues.addMessageToEventsJournal(message);
    }
}
