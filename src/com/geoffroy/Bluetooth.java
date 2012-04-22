package com.geoffroy;
/*
 * MOST OF THE CODE IN THIS FILE IS:
 * 
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class Bluetooth {
	
    // Name for the SDP record when creating server socket
    private static final String NAME = "DroidDTN";
    // Unique UUID for this application
    private static final UUID MY_UUID = 
    		UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private Handler appHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    /**
     * Constructor. Prepares a new Bluetooth session. mState is initialized to
     * STATE_NONE, and mHandler is passed from ConnectionService later.
     * @param context  The UI Activity Context
     */
    public Bluetooth(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Util.STATE_NONE;
    }
    
    public void setHandler(Handler h) {
    	this.appHandler = h;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
    	Util.log(Util.LOG_DEBUG, "State set from " + mState + " to " + state + 
    			".", null);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        appHandler.obtainMessage(Util.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
    	Util.log(Util.LOG_INFO, "Bluetooth helper class started; " +
    			"listening for incoming connections.", null);
        
        // Cancel currently running threads
        stop();

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(Util.STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
    	Util.log(Util.LOG_INFO, "Attempting to connect to " 
    			+ device.getName() + ".", null);


        // Cancel any thread attempting to make a connection
        if (mState == Util.STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        
        setState(Util.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
    	Util.log(Util.LOG_INFO, "Now connected to " + device.getName() + ".", null);
    	
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = appHandler.obtainMessage(Util.MESSAGE_CONNECTION_ESTABLISHED);
        Bundle bundle = new Bundle();
        bundle.putString(Util.DEVICE_ADDRESS, device.getAddress());
        bundle.putString(Util.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        appHandler.sendMessage(msg);

        setState(Util.STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
    	Util.log(Util.LOG_INFO, "Re-initializing Bluetooth helper threads.", null);

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(Util.STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Util.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mState != Util.STATE_CONNECTED) {
        	Util.log(Util.LOG_DEBUG, "We're attempting to send a message but " +
        			"are not connected.", null);
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
        	// Wrap the message
        	message = "<msg>" + message + "</msg>";
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            write(send);
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(Exception e, String device) {
        setState(Util.STATE_LISTEN);

        // Send a failure message back to the Activity        
        Message msg = appHandler.obtainMessage(Util.MESSAGE_FAILED_CONNECTION);
        Bundle bundle = new Bundle();
        bundle.putString(Util.DEVICE_ADDRESS, device);
        msg.setData(bundle);
        appHandler.sendMessage(msg);
        
        String s = "";
        StackTraceElement[] st = e.getStackTrace();
        for(int i = 0; i < st.length; i++) {
        	s += "\n" + st[i].toString();
        }
        
    	Util.log(Util.LOG_ERROR, "A connection attempt failed.", e);        
   }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(Exception e) {
        setState(Util.STATE_LISTEN);

        // Send a failure message back to the Activity
        appHandler.obtainMessage(Util.MESSAGE_LOST_CONNECTION, -1, -1).sendToTarget();
    	Util.log(Util.LOG_INFO, "A device connection was shut down.", e);        
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "Bluetooth AcceptThread listen() failed.", e);        
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != Util.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                	Util.log(Util.LOG_DEBUG, "Bluetooth AcceptThread accept() failed.", e);        
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (Bluetooth.this) {
                        switch (mState) {
                        case Util.STATE_LISTEN:
                        case Util.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                        	Util.log(Util.LOG_INFO, "Accepting a connection from device " 
                        			+ socket.getRemoteDevice().getName() + ".", null);        
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case Util.STATE_NONE:
                        case Util.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                            	Util.log(Util.LOG_DEBUG, "Could not close unwanted socket in AcceptThread.", e);        
                            }
                            break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "Bluetooth AcceptThread close() failed.", e);        
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
            	Util.log(Util.LOG_DEBUG, "Bluetooth ConnectThread create() failed.", e);        
			}
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed(e, mmDevice.getAddress());
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                	Util.log(Util.LOG_DEBUG, "Could not close socket during connection failure in ConnectThread", e2);        
                }
                // Start the service over to restart listening mode
                Bluetooth.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Bluetooth.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "Could not close socket in ConnectThread", e);        
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     * 
     * The problem occurs when the listening device is not actually listening. That is what "Service Discovery
     * Failed" means. We need to make sure that the listener is *always* running.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "Could not create temp sockets in ConnectedThread", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte ch;
        	String msgString = "";

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream                	
                	ch = (byte) mmInStream.read();
            		msgString += (char) ch;
            		
            		if(msgString.indexOf("</msg>") > -1) {
            			msgString = msgString.replaceFirst("<msg>", "");
            			msgString = msgString.replaceFirst("</msg>", "");
            			Message msg = appHandler.obtainMessage(Util.MESSAGE_READ, -1, -1, msgString);
                        Bundle bundle = new Bundle();
                        bundle.putString(Util.DEVICE_ADDRESS, 
                        		mmSocket.getRemoteDevice().getAddress());
                        msg.setData(bundle);
                        appHandler.sendMessage(msg);
                        
                        msgString = "";
            		}
                } catch (IOException e) {
                	Util.log(Util.LOG_DEBUG, "Bluetooth ConnectedThread run() disconnected.", e);        
                    connectionLost(e);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                appHandler.obtainMessage(Util.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "An exception occured during write() in ConnectedThread", e);        
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            	Util.log(Util.LOG_DEBUG, "Could not close socket in ConnectedThread", e);        
            }
        }
    }
}
