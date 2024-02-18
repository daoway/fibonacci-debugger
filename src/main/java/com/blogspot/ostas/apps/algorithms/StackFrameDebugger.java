package com.blogspot.ostas.apps.algorithms;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;
import java.util.Map;

public class StackFrameDebugger {

    public static void main(String[] args) throws Exception {
        // Connect to the target VM (debuggee)
        VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
        AttachingConnector connector = findAttachingConnector(vmManager);
        VirtualMachine vm = attachToVM(connector,"localhost",8000);

        // Get the main thread
        ThreadReference mainThread = getMainThread(vm);

        // Set a breakpoint at a specific line of code
        Location breakpointLocation = findLocation(mainThread, FibonacciApp.class.getName(), 23);
        BreakpointRequest breakpointRequest = mainThread.virtualMachine().eventRequestManager().createBreakpointRequest(breakpointLocation);
        breakpointRequest.enable();

        // Resume the main thread to start debugging
        mainThread.resume();

        // Wait for breakpoint hit event
        EventSet eventSet = vm.eventQueue().remove();
        EventIterator eventIterator = eventSet.eventIterator();
        while (eventIterator.hasNext()) {
            Event event = eventIterator.next();
            System.out.println(event);
            if (event instanceof BreakpointEvent) {
                System.out.println("Breakpoint hit at line: " + ((BreakpointEvent) event).location().lineNumber());
                // Additional debugging tasks can be performed here

                // Get the current call stack
                List<StackFrame> stackFrames = mainThread.frames();
                for (StackFrame frame : stackFrames) {
                    System.out.println("---------------------------------------------------------");
                    System.out.println("Method: " + frame.location().method().name());
                    //System.out.println("File: " + frame.location().sourcePath());
                    //System.out.println("Line: " + frame.location().lineNumber());

                    // Print local variables
                    for (LocalVariable localVariable : frame.visibleVariables()) {
                        System.out.println("Variable: " + localVariable.name() + "=" + frame.getValue(localVariable));
                    }
                    System.out.println();
                }

            }
        }

        // Suspend the main thread
        mainThread.suspend();

        // Resume the main thread
        mainThread.resume();

        // Disconnect from the target VM
        vm.dispose();
    }

    private static AttachingConnector findAttachingConnector(VirtualMachineManager vmManager) {
        List<AttachingConnector> attachingConnectors = vmManager.attachingConnectors();
        for (AttachingConnector connector : attachingConnectors) {
            System.out.println("Attaching Connector: " + connector.name());
            if ("com.sun.jdi.SocketAttach".equals(connector.name())) {
                return connector;
            }
        }
        throw new IllegalStateException("No attaching connector found");
    }


    private static VirtualMachine attachToVM(AttachingConnector connector, String host, int port){
        try {
            // Create arguments for attaching to the VM
            Map<String, Connector.Argument> arguments = connector.defaultArguments();
            arguments.get("hostname").setValue(host);
            arguments.get("port").setValue(String.valueOf(port));

            return connector.attach(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Failed to attach to the target VM", e);
        }
    }


    private static ThreadReference getMainThread(VirtualMachine vm) {
        for (ThreadReference thread : vm.allThreads()) {
            System.out.println(thread.name());
            if ("main".equals(thread.name())) {
                return thread;
            }
        }
        throw new IllegalStateException("Main thread not found");
    }
    private static Location findLocation(ThreadReference thread, String className, int lineNumber) {
        List<Location> locations;
        try {
            locations = thread.virtualMachine().classesByName(className).get(0).locationsOfLine(lineNumber);
        } catch (AbsentInformationException e) {
            throw new IllegalStateException("No line number information found for class: " + className);
        }
        if (locations.isEmpty()) {
            throw new IllegalStateException("No locations found for class: " + className + ", line number: " + lineNumber);
        }
        return locations.get(0);
    }
}
