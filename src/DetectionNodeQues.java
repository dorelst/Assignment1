import java.util.*;

//This is the class that acts as the "storage" recipient for the detection nodes work
public class DetectionNodeQues {
    private int id;
    private volatile int logicalClock;

    //The messages sent to other nodes are saved in this list
    private List<String> sendingQue;

    //The messages sent by another detection nodes arrives first in receivingQue and during a "receiving" event
    //they are "received" by the node and moved to receivedMessages. This last que then is the place from where
    //the detection node looks for messages to classify them during an anomaly detection internal event
    private List<String> receivingQue;
    private Queue<String> receivedMessages;

    //This list is the journal of the detection node that stores all the events that happened during the node life cycle
    private List<String> eventsJournal;


    public DetectionNodeQues (int id) {
        this.id = id;
        this.logicalClock = 0;
        this.sendingQue = new ArrayList<>();
        String initialMessage = "Sent Messages Que for Detection Node "+id;
        this.sendingQue.add(initialMessage);
        this.receivingQue = new ArrayList<>();
        this.eventsJournal = new ArrayList<>();
        this.receivedMessages = new ArrayDeque<>();
    }

    int getId() {
        return id;
    }

    int getLogicalClock() {
        return logicalClock;
    }

    void setLogicalClock(int logicalClock) {
        this.logicalClock = logicalClock;
    }

    private List<String> getSendingQue() {
        return sendingQue;
    }

    List<String> getReceivingQue() {
        return receivingQue;
    }

    List<String> getEventsJournal() {
        return eventsJournal;
    }

    Queue<String> getReceivedMessages() {
        return receivedMessages;
    }

    void addMessageToSendingQue(String message) {
        getSendingQue().add(message);
    }

    //This method is used by other detection nodes to place their messages in the destination node receiving que
    //I synchronized it, in order to prevent a race condition between a node that wants to write a message to it,
    //during its "sending" event and the owner of the que that might want to read from it during a "receiving" event
    //the synchronization lock is the detection node receivingQue list/object
    void addMessageToReceivingQue(String message) {
        synchronized (getReceivingQue()){
            getReceivingQue().add(message);
        }
    }

    //Once a message is "received" it's moved to the receivedMessages que
    void addMessageToReceivedMessages(String message) {
        getReceivedMessages().add(message);
    }

    //This method is used by the detection node to get a message that another detection node have sent to it
    //I synchronized it, in order to prevent a race condition between a node that wants to write a message to it,
    //during its "sending" event and the owner of the que that might want to read from it during a "receiving" event
    //the synchronization lock is the detection node receivingQue list/object
    String getMessageFromReceivingQue(int senderNodeId) {
        String message = "";
        synchronized (getReceivingQue()){
            if (!getReceivingQue().isEmpty()) {

                //If the receiving que is not empty it iterates through its entries to search for a message sent
                //by the node specified by the senderNodeId parameter
                Iterator<String> iterator = getReceivingQue().iterator();
                int nodeId;
                while (iterator.hasNext()) {
                    message = iterator.next();
                    nodeId = getNodeIdFromIncomingMessage(message);

                    //If a message from the detection node specified by the senderNodeId parameter is found
                    //then it remove it from the receiving que, gets out from while loop and returns the message it founded
                    if (nodeId == senderNodeId) {
                        iterator.remove();
                        break;
                    }
                    message = "";
                }
            }
        }
        return message;
    }

    //Extract the sender node id from an incoming message. All the messages created follow the pattern:
    //logical_clock_value+" "+node_Id+" "+type_of_Event+" "+message
    private int getNodeIdFromIncomingMessage(String message) {
        String[] splitMessage = message.split(" ", 3);
        return (Integer.parseInt(splitMessage[1]));
    }

    //Extract the sender node logical clock from an incoming message. All the messages created follow the pattern:
    //logical_clock_value+" "+node_Id+" "+type_of_Event+" "+message
    int getNodeLogicalClockFromIncomingMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        return (Integer.parseInt(splitMessage[0]));
    }

    //All the events are logged in the eventsJournal list
    void addMessageToEventsJournal(String message) {
        getEventsJournal().add(message);
    }
}
