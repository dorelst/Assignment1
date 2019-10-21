JFLAGS = -g
JC = javac
JVM= java 
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Assignment1.java \
        DetectionNodeProcessor.java \
        DetectionNodeQues.java \
        Sampler.java

MAIN = Assignment1

default: classes

classes: $(CLASSES:.java=.class)

run: classes
	$(JVM) $(MAIN)

clean:
	$(RM) *.class
