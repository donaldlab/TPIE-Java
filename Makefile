
CPP_SRC_DIR = src/cpp
JAVA_CLASS_DIR = bin
JAVA_PACKAGE = edu.duke.cs.tpie
JAVA_INCLUDE_DIR = /usr/lib/jvm/java-8-openjdk-amd64/include
JAVA_INCLUDE_PLATFORM_DIR = /usr/lib/jvm/java-8-openjdk-amd64/include/linux
TPIE_DIR = ../tpie
BUILD_DIR = build/native
OUT_NAME = tpie-java
RESOURCES_DIR = resources/edu/duke/cs/tpie

CPP = g++
CPPFLAGS_DEBUG = -g
CPPFLAGS_RELEASE = -O3
CPPFLAGS = -std=c++11 -Wall -Wextra -fPIC -I$(CPP_SRC_DIR) -I$(TPIE_DIR) -I$(TPIE_DIR)/build -I$(JAVA_INCLUDE_DIR) -I$(JAVA_INCLUDE_PLATFORM_DIR) $(CPPFLAGS_DEBUG)
LDFLAGS = -pthread $(TPIE_DIR)/build/tpie/libtpie.a -lboost_filesystem -lboost_thread -lboost_system


all: lib

lib: jni TPIE DoublePriorityQueue FIFOQueue
	$(CPP) -o $(BUILD_DIR)/lib$(OUT_NAME).so -shared \
		$(BUILD_DIR)/jni.a \
		$(BUILD_DIR)/TPIE.a \
		$(BUILD_DIR)/DoublePriorityQueue.a \
		$(BUILD_DIR)/FIFOQueue.a \
		$(LDFLAGS)
	mv $(BUILD_DIR)/lib$(OUT_NAME).so $(RESOURCES_DIR)/

jni:
	$(CPP) $(CPPFLAGS) -o $(BUILD_DIR)/jni.a -c $(CPP_SRC_DIR)/jni.cpp
	
TPIE:
	$(CPP) $(CPPFLAGS) -o $(BUILD_DIR)/TPIE.a -c $(CPP_SRC_DIR)/TPIE.cpp

DoublePriorityQueue:
	$(CPP) $(CPPFLAGS) -o $(BUILD_DIR)/DoublePriorityQueue.a -c $(CPP_SRC_DIR)/DoublePriorityQueue.cpp

FIFOQueue:
	$(CPP) $(CPPFLAGS) -o $(BUILD_DIR)/FIFOQueue.a -c $(CPP_SRC_DIR)/FIFOQueue.cpp
	
javah:
	javah -cp $(JAVA_CLASS_DIR) -o $(CPP_SRC_DIR)/TPIE.hpp $(JAVA_PACKAGE).TPIE
	javah -cp $(JAVA_CLASS_DIR) -o $(CPP_SRC_DIR)/DoublePriorityQueue.hpp $(JAVA_PACKAGE).DoublePriorityQueue
	javah -cp $(JAVA_CLASS_DIR) -o $(CPP_SRC_DIR)/FIFOQueue.hpp $(JAVA_PACKAGE).FIFOQueue

clean:
	$(RM) $(BUILD_DIR)/*

