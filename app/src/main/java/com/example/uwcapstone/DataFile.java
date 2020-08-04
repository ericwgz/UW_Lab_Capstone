package com.example.uwcapstone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sonicmeter.android.multisonicmeter.Params;
import com.sonicmeter.android.multisonicmeter.Utils;

public class DataFile {
    Map<String, Integer> clientMap;
    Map<short[], String> serverMap;
    List<String> msgList;

    private static Params params = new Params();

    public DataFile() {
        clientMap = new HashMap<>();
        serverMap = new HashMap<>();
        msgList = new ArrayList<>();
    }

    public Map<String, Integer> generateClientMap() {
        for (int i = 0; i < msgList.size(); i++) {
            clientMap.put(msgList.get(i), i);
        }

        return clientMap;
    }

    public Map<short[], String> generateServerMap() {
        for (String msg: msgList) {
            short[] seq = Utils.generateActuateSequence_seed(params.warmSequenceLength, params.signalSequenceLength, params.sampleRate, clientMap.get(msg), params.noneSignalLength);
            serverMap.put(seq, msg);
        }
        return serverMap;
    }
}
