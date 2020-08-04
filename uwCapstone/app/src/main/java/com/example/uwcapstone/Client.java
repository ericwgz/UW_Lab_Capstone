package com.example.uwcapstone;

import android.util.Log;

import com.sonicmeter.android.multisonicmeter.Params;
import com.sonicmeter.android.multisonicmeter.Utils;

import java.util.HashMap;
import java.util.Map;

class Client extends Thread{
    String msg;
    Map<String, Integer> map;
    //private short[] signalSequence;
    private byte[] playSequence;
    private volatile boolean exit;
    int number;

    private static Params params = new Params();

    public Client(String message) {
        this.msg = message;
        map = new HashMap<>();
        map.put("SOS", 1500)   ;
        map.put("UnderGround", 200);
        map.put("Fire", 800);
        number = 0;
        exit = false;
    }

    @Override
    public void run() {

        try {
            Log.d("client receive message", msg);
            MainActivity.log("client receive message: " + msg);
            MainActivity.setStartSendBtnState(false);
            MainActivity.setStopSendBtnState(true);

            //signalSequence = Utils.generateSignalSequence_63(map.get(msg));//Utils.generateChirpSequence_seed(params.signalSequenceLength, params.sampleRate, randomSeed, 1);
            playSequence = Utils.convertShortsToBytes( Utils.generateActuateSequence_seed(params.warmSequenceLength, params.signalSequenceLength, params.sampleRate, map.get(msg), params.noneSignalLength));

            while(!exit) {
                Log.d("start", "play sequence");
                MainActivity.log("start to play sequence " + String.valueOf(number++));
                Utils.play(playSequence);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        exit = true;
    }

}
