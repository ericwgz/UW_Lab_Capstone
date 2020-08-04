package com.example.uwcapstone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sonicmeter.android.multisonicmeter.TrackRecord;
import com.sonicmeter.android.multisonicmeter.Utils;
import com.sonicmeter.android.multisonicmeter.Params;

class Server extends Thread{
    //String msg;
    boolean bContinue;
    private static Server instance;
    //Map<byte[], String> serverMap;
    //private short[] signalSequence;
    Map<String, Integer> tmp;
    //private short[] recordedSequence;
    int length;
    boolean exit;

    private static Params params = new Params();
    private byte[] playSequence;

    static TrackRecord audioTrack = new TrackRecord();
    private RecordThread recordThread;

    public Server() {
        //this.msg = message;
        bContinue = true;
        exit = false;
        length = Params.recordSampleLength * 6;
        tmp = new HashMap<>();
        /* 2020/07/28 */
        tmp.put("SOS", 1500);
        tmp.put("UnderGround", 200);
        tmp.put("Fire", 800);
        /* 2020/07/28 */
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        MainActivity.setStartReceiveBtnState(false);
        MainActivity.setStopReceiveBtnState(true);
        List<short[]> signalList = new ArrayList<>();
        List<String> msgList = new ArrayList<>();

        /* 2020/07/28 Adding all the sequence accordingly*/
        signalList.add(Utils.generateSignalSequence_63(tmp.get("SOS")));
        signalList.add(Utils.generateSignalSequence_63(tmp.get("UnderGround")));
        signalList.add(Utils.generateSignalSequence_63(tmp.get("Fire")));
        msgList.addAll(tmp.keySet());

//        Yixuan Wang
//        for (String key: tmp.keySet()) {
//            short[] signalSequence = Utils.generateSignalSequence_63(tmp.get(key));
//            signalList.add(signalSequence);
//            msgList.add(key);
//        }

        try {
            //Start recording Server
            MainActivity.log("start to record");
            recordThread = new RecordThread();
            recordThread.start();

            while (!exit) {
                Utils.sleep(5000);
                short[] recordedSequence = getRecordedSequence(params.recordSampleLength * 6);
                List<Double> similarityList = new ArrayList();

                for (int i = 0; i < signalList.size(); i++) {

                    Utils.setFilter_convolution(signalList.get(i));

                    /* 2020/07/28 similarity should get value from estimate_min_diff rather than a int value from estimate_convolution_index*/
                    int startIdx = Utils.estimate_convolution_index(recordedSequence, signalList.get(i), 0,recordedSequence.length);
                    double similarity = Utils.estimate_min_diff(recordedSequence, signalList.get(i), startIdx ,recordedSequence.length);
                    /* 2020/07/28 */
                    // double similarity = Utils.estimate_convolution_index(recordedSequence, signalList.get(i), 0,recordedSequence.length);
                    similarityList.add(similarity);
                }

                double min = Math.exp(100);
                double threshold = 0;
                int index = -1;
                //Log.d("similarityList",String.valueOf(similarityList.size()));
                for (int i = 0; i < similarityList.size(); i++) {
                    //Log.d("similarityList",similarityList.get(i).toString());
                    if (similarityList.get(i) < min && similarityList.get(i) > threshold) {
                        min = similarityList.get(i);
                        index = i;
                    }
                }

                if(index != -1) {
                    String msg = msgList.get(index);
                    MainActivity.decodedMsg(msg);
                    /* 2020/07/28 */
                    MainActivity.log("SOS deviation: "+ similarityList.get(0));
                    MainActivity.log("Underground deviation: "+ similarityList.get(1));
                    MainActivity.log("Fire deviation: "+ similarityList.get(2));
                    /* 2020/07/28 */
                    exit = true;
                    recordThread.stopRecord();
                }
            }

        } catch (Exception e) {
            MainActivity.log("Error!"+e.getMessage());
        }
    }

    private class RecordThread extends Thread {
        boolean bContinue = true;

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            if (Utils.getRecorderHandle() == null)
                Utils.initRecorder(Params.sampleRate);
            int minBufferSize = Utils.getMinBufferSize(Params.sampleRate);
            try {
                Utils.getRecorderHandle().startRecording();
            } catch (Throwable x) {
                MainActivity.log("Error recording: " + x.getMessage());
            }
            int i = 0;
            while (bContinue) {
                try {
                    short[] buffer = Utils.recordBuffer(minBufferSize);
                    audioTrack.addSamples(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.log("Server Recording Failed " + e.getMessage());
                    Utils.getRecorderHandle().stop();
                    bContinue = false;
                    break;
                } finally {
//                    MainActivity.log("Server Recording Ended");
                }
            }
            try {
                Utils.getRecorderHandle().stop();
            } catch (Throwable x) {
                MainActivity.log("Error recording: " + x.getMessage());
            }

        }

        public void stopRecord(){
            bContinue = false;
        }
    }

    synchronized static short[] getRecordedSequence(int length){
        return audioTrack.getSamples(length);
    }

    public void stopRecording(){
        instance.recordThread.stopRecord();
    }

    public void stopThread(){
        exit = true;
    }
}
