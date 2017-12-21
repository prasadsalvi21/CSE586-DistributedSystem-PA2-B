package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    static final List<String> remotePort = new ArrayList<String>(5);
    int seqNumber = 0;
    int counter = 0;
    Integer failed_pid;
    boolean flag = true;
    int n = 5;
    PriorityQueue<Message> pq = new PriorityQueue<Message>();
    PriorityQueue<Message> pq1 = new PriorityQueue<Message>();
    HashMap<String, List> listofList = new HashMap<String, List>();
    List<String> a=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        remotePort.add("11108");
        remotePort.add("11112");
        remotePort.add("11116");
        remotePort.add("11120");
        remotePort.add("11124");

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.i("myport", myPort);

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket", e);
            return;
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button b4 = (Button) findViewById(R.id.button4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                tv.append(msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        private Uri uriAddress = null;


        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            try {
                ServerSocket serverSocket = sockets[0];
                uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
                Integer count = -1;
                Socket socket = null;
                int value = 0;
                int pq_size = 0;
                int pq1_size = 0;
                int index_msg = 0;
                int index_mid = 0;
                int index_fpid = 0;
                int index_tpid = 0;
                int index_status = 0;
                String finalMessage = "";
                String messageId = "";
                String frompid = "";
                String topid = "";
                String sequence = "";
                String msg_received, ack;


                while (true) {
                    ack = null;

                    socket = serverSocket.accept();
                    //socket.setSoTimeout(5000);
                    DataInputStream is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    msg_received = is.readUTF();
                    Log.d(TAG, "Recieved message from client: " + msg_received);

                    index_status = msg_received.lastIndexOf("STATUS=");
                    index_msg = msg_received.lastIndexOf("MESSAGE=");
                    index_mid = msg_received.lastIndexOf("MID=");
                    index_fpid = msg_received.lastIndexOf("FROMPID=");
                    index_tpid = msg_received.lastIndexOf("TOPID=");
                    int index_seq = msg_received.lastIndexOf("SEQ=");


                    String status = msg_received.substring(7, index_msg);

                    Log.i("Status", status);

                    if (status.equals("MSG")) {
                        finalMessage = msg_received.substring(index_msg + 8, index_mid);
                        messageId = msg_received.substring(index_mid + 4, index_fpid);
                        frompid = msg_received.substring(index_fpid + 8, index_tpid);
                        topid = msg_received.substring(index_tpid + 6, msg_received.length());


                        Log.d(TAG, "After fetching msg from client:" + status + ":" + finalMessage + ":" + messageId + ":" + frompid + ":" + topid);
                        count++;
                        String seq = count.toString();
                        try {

                            String messageProposed = "STATUS=" + "PROPOSED" + "MESSAGE=" + finalMessage + "MID=" + messageId + "FROMPID=" + frompid + "TOPID=" + topid + "SEQ=" + count;

                            Message m = new Message();
                            m.setMessage(finalMessage);
                            m.setStatus("UNDELIVERED");
                            m.setFrompid(frompid);
                            m.setTopid(topid);
                            m.setSeq(seq);
                            m.setMessageId(messageId);
                            pq.add(m);
                            pq_size++;
                            Log.i("Initial PQ ", pq.toString());
                            Log.i("Intial PQ size", "" + pq_size);

                            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                            os.writeUTF(messageProposed);
                            Log.d(TAG, "Sending Proposed Message to Client : " + messageProposed);
                            os.flush();


                        } catch (UnknownHostException e) {
                            Log.e(TAG, "ClientTask2 UnknownHostException");
                        } catch (IOException e) {
                            Log.e(TAG, "ClientTask2 socket IOException");
                        }


                    } else if (status.equals("AGREED")) {
                        Log.i(TAG, "AGREED SEQUENCE");
                        finalMessage = msg_received.substring(index_msg + 8, index_mid);
                        messageId = msg_received.substring(index_mid + 4, index_fpid);
                        frompid = msg_received.substring(index_fpid + 8, index_tpid);
                        topid = msg_received.substring(index_tpid + 6, index_seq);
                        sequence = msg_received.substring(index_seq + 4, msg_received.length());

                        Log.d(TAG, "After fetching Proposed from client :" + status + ":" + finalMessage + ":" + messageId + ":" + frompid + ":" + topid + ":" + sequence);
                        Message m3 = new Message();
                        m3.setMessage(finalMessage);
                        m3.setStatus("PASS");
                        m3.setFrompid(frompid);
                        m3.setTopid(topid);
                        m3.setSeq(sequence);
                        m3.setMessageId(messageId);

                        Iterator<Message> iter = pq.iterator();
                        while (iter.hasNext()) {
                            Message m4 = iter.next();
                            if (m4.getMessageId().equals(messageId) && m4.getFrompid().equals(frompid)) {
                                pq.remove(m4);
                                pq.add(m3);
                                break;

                            }

                        }
                        Log.i("Left in PQ", pq.toString());
                        Iterator<Message> iter1 = pq.iterator();

                        while (iter1.hasNext()) {
                            Message m6 = iter1.next();

                            if (m6.getStatus().equals("PASS")) {
                                pq1.add(m6);
                                pq1_size++;
                                pq.remove(m6);

                            }
                        }
                        Log.i("Added in PQ1", pq1.toString());
                        //Thread.sleep(500);


                    }
                    Log.i("Server flag", "" + flag);
                    if (flag == false && pq_size > 19) {

                        Iterator<Message> iter3 = pq.iterator();

                        while (iter3.hasNext()) {
                            Message m8 = iter3.next();
                            Log.i("M8 out", m8.toString());

                            if (m8.getStatus().equals("UNDELIVERED") && listofList.containsKey(m8.getMessageId())) {

                                Log.i("M8 in", m8.toString());
                                n = 4;
                                calculatePropse(m8);

                            }
                        }
                    }
                    if ((flag == false && pq_size > 19 && pq1_size >= 20 && !pq.isEmpty())) {
                        Iterator<Message> iter3 = pq.iterator();

                        while (iter3.hasNext()) {
                            Message m9 = iter3.next();

                            String failId = failed_pid.toString();
                            Log.i("Message m9", m9.toString() + ":" + failId);
                            if (m9.getFrompid().equals(failId)) {
                                Log.i("Left pq failed before", pq.toString());
                                pq1.add(m9);
                                pq.remove(m9);
                                pq1_size++;
                                Log.i("Left pq failed after", pq.toString());

                            }
                        }

                    }


                    if (flag == true && pq1.size() == 25 && pq.isEmpty()) {

                        while (!pq1.isEmpty()) {
                            Message m5 = pq1.poll();
                            String test = m5.getStatus();
                            // Log.i("Final TEST",test);

                            pq1.remove(m5);
                            Log.i("FINAL MESSAGE 2", m5.toString());

                            finalMessage = m5.getMessage();
                            ContentValues keyValueToInsert = new ContentValues();

                            // inserting <”key-to-insert”, “value-to-insert”>

                            keyValueToInsert.put("key", Integer.toString(seqNumber));
                            keyValueToInsert.put("value", finalMessage);
                            seqNumber = seqNumber + 1;
                            Uri newUri = getContentResolver().insert(uriAddress, keyValueToInsert);
                        }


                    }

                    if (flag == false && pq.isEmpty() && pq1_size >= 20) {
                        while (!pq1.isEmpty()) {
                            Message m5 = pq1.poll();
                            String test = m5.getStatus();
                            // Log.i("Final TEST",test);

                            pq1.remove(m5);
                            Log.i("FINAL MESSAGE 3", m5.toString());

                            finalMessage = m5.getMessage();
                            ContentValues keyValueToInsert = new ContentValues();

                            // inserting <”key-to-insert”, “value-to-insert”>

                            keyValueToInsert.put("key", Integer.toString(seqNumber));
                            keyValueToInsert.put("value", finalMessage);
                            seqNumber = seqNumber + 1;
                            Uri newUri = getContentResolver().insert(uriAddress, keyValueToInsert);
                        }
                    }
                    Log.i("pq_size & pq1_size", "" + pq_size + ":" + pq1_size);
                    DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                    ack = "server ack";
                    os.writeUTF(ack);
                    os.flush();
                    Log.d(TAG, "Sending ack to Client : " + ack);

                    publishProgress(finalMessage);
                    socket.close();
                }

            }/* catch (InterruptedException e) {
                Log.e(TAG, "ServerTask INTERRUPTED");
            }*/ catch (SocketTimeoutException ste) {
                flag = false;
                Log.i("FALG SET server", "" + flag);
            } catch (UnknownHostException e) {
                Log.e(TAG, "ServerTask UnknownHostException" + e);
            } catch (IOException e) {
                Log.e(TAG, "ServerTask socket IOException" + e);
                flag = false;
                Log.i("FLAG SET server", "" + flag);
            }
            return null;
        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView rtv = (TextView) findViewById(R.id.textView1);
            rtv.append(strReceived + "\t\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            return;
        }
    }

    public void calculatePropse(Message m1) {
        String messageId2 = m1.getMessageId();
        if (listofList.get(messageId2).size() == n) {
            Log.i("Hashmap value", messageId2 + " : " + listofList.get(messageId2).toString());

            int max = -1;
            int tl = 10;
            for (int k = 0; k < n; k++) {


                String ml = listofList.get(messageId2).get(k).toString();
                ml = ml.replace("[", "").replace("]", "");

                int index_sq = ml.lastIndexOf("SEQ=");
                int index_tp = ml.lastIndexOf("TOPID=");
                int index_fp = ml.lastIndexOf("FROMPID=");

                //Log.i("Arraylist", ml);

                String tp3 = ml.substring(index_tp + 6, index_sq);
                String sq3 = ml.substring(index_sq + 4, ml.length());

                int temp = Integer.parseInt(sq3);
                int a = Integer.parseInt(tp3);
                if (temp > max) {
                    max = temp;
                    tl = a;
                } else if (temp == max) {
                    if (a < tl) {
                        max = temp;
                        tl = a;
                    }
                }

            }
            int finalseq = max;
            int finalPid = tl;
            Log.i("map Proposed Seq for", messageId2 + " Seq:" + finalseq + " PID:" + finalPid);

            for (int l = 0; l < remotePort.size(); l++) {

                try {
                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort.get(l)));

                    String proposed = "STATUS=" + "AGREED" + "MESSAGE=" + m1.getMessage() + "MID=" + messageId2 + "FROMPID=" + m1.getFrompid() + "TOPID=" + finalPid + "SEQ=" + finalseq;
                    Log.i(TAG, "Sending Proposed sequnece to server: " + proposed);
                    DataOutputStream os1 = new DataOutputStream(socket1.getOutputStream());
                    os1.writeUTF(proposed);
                    os1.flush();
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }
            }
            max = -1;
            tl = 10;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String myport = msgs[1];
            int fpid = 0;
            int tpid = 0;
            for (int i = 0; i < remotePort.size(); i++) {
                String auction = remotePort.get(i);
                if (myport.equals(auction)) {
                    fpid = i;
                }
            }
            Log.i("From Process Id", "P" + fpid);
            String messageId=RandomIdGenerator.randomNumber();
            Log.i("Random message Id",messageId);

            for(int x=0;x<a.size();x++) {
                if (a.get(x).equals(messageId)) {
                    messageId = RandomIdGenerator.randomNumber();
                    x=0;
                    Log.i("Updated Random msgId",messageId);
                }
            }
            a.add(messageId);

            for (int i = 0; i < remotePort.size(); i++) {

                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort.get(i)));

                    String msgToSend = msgs[0];
                    tpid = i;
                    Log.i("TO Process Id", "P" + tpid);
                    String msg = "STATUS=" + "MSG" + "MESSAGE=" + msgToSend + "MID=" + messageId + "FROMPID=" + fpid + "TOPID=" + tpid;
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                    String ack;
                    do {
                        ack = null;
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        os.writeUTF(msg);
                        Log.d(TAG, "Sending Message to Server : " + msg);
                        os.flush();

                        //socket.setSoTimeout(5000);


                        DataInputStream is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        String msg_received = is.readUTF();
                        if (msg_received.equals("Fail")) {
                            System.out.print("test");
                        } else {
                            int index_msg = 0;
                            int index_mid = 0;
                            int index_fpid = 0;
                            int index_tpid = 0;
                            int index_status = 0;
                            int index_seq = 0;
                            String finalMessage2 = "";
                            String messageId2 = "";
                            String frompid2 = "";
                            String topid2 = "";
                            String seq2 = "";
                            String status2 = "";

                            index_status = msg_received.lastIndexOf("STATUS=");
                            index_msg = msg_received.lastIndexOf("MESSAGE=");
                            index_mid = msg_received.lastIndexOf("MID=");
                            index_fpid = msg_received.lastIndexOf("FROMPID=");
                            index_tpid = msg_received.lastIndexOf("TOPID=");
                            index_seq = msg_received.lastIndexOf("SEQ=");

                            status2 = msg_received.substring(7, index_msg);
                            finalMessage2 = msg_received.substring(index_msg + 8, index_mid);
                            messageId2 = msg_received.substring(index_mid + 4, index_fpid);
                            frompid2 = msg_received.substring(index_fpid + 8, index_tpid);
                            topid2 = msg_received.substring(index_tpid + 6, index_seq);
                            seq2 = msg_received.substring(index_seq + 4, msg_received.length());

                            Log.d(TAG, "After fetching msg from server:" + status2 + ":" + finalMessage2 + ":" + messageId2 + ":" + frompid2 + ":" + topid2 + ":" + seq2);


                            if (status2.equals("PROPOSED")) {
                                Message m1 = new Message();
                                m1.setMessage(finalMessage2);
                                m1.setMessageId(messageId2);
                                m1.setFrompid(frompid2);
                                m1.setTopid(topid2);
                                m1.setSeq(seq2);
                                m1.setStatus(status2);

                                List<Message> li = new ArrayList<Message>();
                                li.add(m1);

                                if (flag == true || (flag == false && !m1.topid.equals(failed_pid))) {
                                    if (listofList.isEmpty() || !listofList.containsKey(messageId2)) {
                                        listofList.put(messageId2, li);
                                        Log.i("Created Hashmap value", messageId + " : " + listofList.get(messageId2).toString());
                                    } else {
                                        listofList.get(messageId2).add(li);
                                        Log.i("Inserted Hashmap value", messageId2 + " : " + listofList.get(messageId2).toString());
                                    }

                                }
                                Log.i("FLAG After Hash", "" + flag + "" + n);
                                calculatePropse(m1);

                            }
                        }
                        ack = is.readUTF();
                        Log.d(TAG, "Receiving ack from Server : " + ack);
                    }
                    while (!ack.equals("server ack"));
                    socket.close();
                } catch (InterruptedIOException ste) {
                    flag = false;
                    Log.i("FLAG SET to client Intr", "" + flag);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                    flag = false;
                    n = 4;
                    Log.e("FLAG SET to client", "" + flag);
                    failed_pid = tpid;
                    Log.e("Failed Process id", "" + failed_pid);

                }
            }
            return null;
        }
    }
}
