import java.util.List;
import java.util.concurrent.CountDownLatch;

//This is the class that is doing all the work: generates the events and processed them
public class DetectionNodeProcessor implements Runnable {
    private int nodeId;

    //The processorQues variable stores the node own ques
    private DetectionNodeQues processorQues;

    //The otherProcessorsQues is a list with three DetectionNodeQues objects containing the other nodes ques
    private List<DetectionNodeQues> otherProcessorsQues;
    private int numberOfIterations;

    //randomMessages variable stores 5 different strings/words used to create random 2 words messages
    private String[] randomMessages;
    private CountDownLatch countDownLatch;

    private int getNodeId() {
        return nodeId;
    }

    private List<DetectionNodeQues> getOtherProcessorsQues() {
        return otherProcessorsQues;
    }

    private int getNumberOfIterations() {
        return numberOfIterations;
    }

    private String[] getRandomMessages() {
        return randomMessages;
    }


    public DetectionNodeProcessor(int nodeId, DetectionNodeQues processorQues, List<DetectionNodeQues> otherProcessorsQues, int numberOfIterations, CountDownLatch countDownLatch) {
        this.nodeId = nodeId;
        this.processorQues = processorQues;
        this.otherProcessorsQues = otherProcessorsQues;
        this.numberOfIterations = numberOfIterations;
        this.countDownLatch = countDownLatch;
        this.randomMessages = new String[]{"aaaaaaa bbbbbbb", "ccccccc ddddddd", "eeeeeee fffffff", "ggggggg hhhhhhh", "iiiiiii jjjjjjj"};
    }

    //This run method runs the activities of the detection node, for the number of iterations that was set when the
    //detection node (detectionNodeProcessor) object was created in the Assignment1 class
    public void run() {

        int senderNodeId;
        int typeOfEvent;

        for (int i = 0; i < getNumberOfIterations(); i++) {

            //There are three type of events that generateTypeOfEvent might randomly return
            typeOfEvent = generateTypeOfEvent();
            processorQues.setLogicalClock(processorQues.getLogicalClock()+1);

            //The arbitraryFailure method will change the logical clock, if one of the arbitrary failures implemented happens
            arbitraryFailure();

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

        //When all the number of iterations are done, the thread finish its work and signal its termination to main thread
        countDownLatch.countDown();
    }

    //This method simulates different random types of arbitrary failures
    private int arbitraryFailure() {

        //af variable controls the chances that an arbitrary failure to happen
        int af = (int)(Math.random()*500);

        //This simulates a failure where the node doesn't advance its internal logical clock when an event happens.
        //Since the LC was incremented before the arbitraryFailure was called, the value of the LC is reduced by 1 unit
        // to simulate this
        if (af == 222) {
            processorQues.setLogicalClock(processorQues.getLogicalClock()-1);
        }

        //This simulates a failure where the node advances its LC two units when an event happens
        if (af == 333) {
            processorQues.setLogicalClock(processorQues.getLogicalClock()+1);
        }

        //This simulates a failure where the node doesn't advance its LC to have a greater value than the LC associated
        //with a message received from another detection node
        //it returns -1 to signal the receivingEvent method to skip the logical clock synchronization
        if (af == 444) {
            return -1;
        }
        return 1;
    }

    //This method generates a random node id that will be used either as the destination node id of a sending event
    //or in case of a receiving event the sender node id for which a receiving event will look to see if a message
    //was received in its receiving que
    private int generateNodeId() {
        return ((int)(Math.random()*3));
    }

    //This method generates a random type of event for the run method to process
    private int generateTypeOfEvent() {
        int typeOfEvent = (int)(Math.random()*100);
        if ( (typeOfEvent >= 0) && (typeOfEvent <= 32)) {
            return 0;
        }
        if ( (typeOfEvent >= 33) && (typeOfEvent <= 66)) {
            return 1;
        }
        return 2;
    }

    //This method generates a random internal event (create message or anomaly detection) for the internalEvent method
    //to process
    private void internalEvent() {
        int typeOfInternalEvent = (int)(Math.random()*2);
        if (typeOfInternalEvent == 0) {
            generateMessage();
        } else {
            anomalyDetection();
        }
    }

    //This method generates a random message following the pattern:
    //node_Id+" "+type_of_Event+" "+message
    private String createRandomMessage(String eventType) {
        String message;
        int chooseMessage = (int)(Math.random()*5);
        message = getNodeId()+" "+eventType+" "+getRandomMessages()[chooseMessage];
        return message;
    }

    //This method handle the internal event of creating a message
    private void generateMessage() {
        String message = processorQues.getLogicalClock()+" "+createRandomMessage("message_generation");
        processorQues.addMessageToEventsJournal(message);
    }

    //This method handle the internal event of detecting an anomaly in a received message
    private void anomalyDetection() {
        String message;

        //The method analysis the messages that were received (from the receivedMessages ques)
        //If the que is empty it logs this is the eventsJournal list
        if (processorQues.getReceivedMessages().isEmpty()) {
            message = processorQues.getLogicalClock()+" "+getNodeId()+" "+"detection_event"+" "+"No messages available to analyze";
            processorQues.addMessageToEventsJournal(message);
            return;
        }

        //abnormalMessage controls the chances that a message is considered normal or abnormal
        //since the assignment requirements said this is not relevant for the assignment I implemented this as a
        //random decision
        int abnormalMessage = (int) (Math.random() * 10);

        //If the receivedMessages que is not empty it reads the first element from the que and it removes it also
        message = processorQues.getLogicalClock() + " " + getNodeId() + " " + "detection_event" + " " + processorQues.getReceivedMessages().poll();

        //The message analysis was implemented here as random decision to classify a received message as normal or abnormal
        if (abnormalMessage == 6) {
            message = message + " " + "ABNORMAL_MESSAGE";
        } else {
            message = message + " " + "NORMAL_MESSAGE";
        }
        processorQues.addMessageToEventsJournal(message);
    }

    //This method implements a receiving event
    private void receivingEvent(int senderNodeId) {
        String message;
        int senderLogicalClock;
        message = processorQues.getLogicalClock()+" "+getNodeId()+" "+"receiving_event";

        //The method looks in the receiving que to process a message from specific detection node
        //If the receiving que is empty it logs this in the eventsJournal and exits
        if (processorQues.getReceivingQue().isEmpty()) {
            message = message+" "+"Receiving que is empty";
            processorQues.addMessageToEventsJournal(message);
            return;
        }

        //Method getMessageFromReceivingQue search into receiving que to see if a message from the specified node
        //was received and returns the message or an empty string if not
        String incomingMessageFromSelectedNode = processorQues.getMessageFromReceivingQue(getOtherProcessorsQues().get(senderNodeId).getId());
        if (incomingMessageFromSelectedNode.equals("")) {
            message = message + " No message received from Detection Node "+getOtherProcessorsQues().get(senderNodeId).getId();
            processorQues.addMessageToEventsJournal(message);
            return;
        } else {

            //Method getNodeLogicalClockFromIncomingMessage extracts the logical clock of the sender node from
            // the received message
            senderLogicalClock = processorQues.getNodeLogicalClockFromIncomingMessage(incomingMessageFromSelectedNode);

            // the sender logical clock is compared to the internal logical clock of the working node and if senders
            //value is greater or equal to the receiver one, the receiver LC is updated
            if ((senderLogicalClock >= processorQues.getLogicalClock()) && (arbitraryFailure() != -1)) {

                //System.out.println("DN "+getNodeId()+" LC was "+processorQues.getLogicalClock()+" and it advanced to "+senderLogicalClock+"+1 because senderNode "+senderNodeId+" LC was "+senderLogicalClock);
                processorQues.setLogicalClock(senderLogicalClock + 1);
                message = processorQues.getLogicalClock() + " " + getNodeId() + " " + "receiving_event";
            }
            message = message + " " + incomingMessageFromSelectedNode;
        }

        //Adds the message to receivedMessages que and also to the eventsJournal list
        processorQues.addMessageToReceivedMessages(message);
        processorQues.addMessageToEventsJournal(message);
    }

    //This method implements a sending event
    private void sendingEvent(int destinationNodeId) {

        //A random message is created to be sent to a detection node.
        //From the discussions in the class with Dr Raje and from the assigment directions the message sent does't need
        //to be in response to a received message for this assignment
        String message = processorQues.getLogicalClock()+" "+createRandomMessage("sending_event to DN "+getOtherProcessorsQues().get(destinationNodeId).getId());

        //The access to the destination receiving que is synchronized to avoid a race condition between this write operation
        //and an internal read operation from the destination node.
        synchronized (getOtherProcessorsQues().get(destinationNodeId).getReceivingQue()){
            getOtherProcessorsQues().get(destinationNodeId).addMessageToReceivingQue(message);
        }

        //Adds the received message to sending que and also to the eventsJournal list
        processorQues.addMessageToSendingQue(message);
        processorQues.addMessageToEventsJournal(message);
    }
}
