CLASSPATH := src/network/ear7h/jalc/
CLASSES := Main

.PHONY: all
all: $(CLASSES:%=src/network/ear7h/jalc/%.class)
	@echo build all $(CLASSES:=.class)

A=
run: all
	java -cp $(CLASSPATH) Main $(A)

%.class: $(@:.class=.java)
	@echo compiling $@
	javac $(@:.class=.java)

.PHONY: clean
clean:
	rm $(shell find . | grep .class)