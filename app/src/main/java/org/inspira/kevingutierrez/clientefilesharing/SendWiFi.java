package org.inspira.kevingutierrez.clientefilesharing;

import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Hector Torres on 30/11/2016.
 */
public class SendWiFi extends Thread {

    private Activity activity;
    private JSONObject json;

    public SendWiFi(Activity activity, JSONObject json){
        this.activity = activity;
        this.json = json;
    }

    @Override
    public void run(){
        try{
            String host;
            Socket socket = new Socket((host = activity.getSharedPreferences("Host", Context.MODE_PRIVATE).getString("host", "NaN")).substring(0, host.length()), 23545);
            IOHandler ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.setRate(1000);
            ioHandler.sendMessage(json.toString().getBytes());
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
