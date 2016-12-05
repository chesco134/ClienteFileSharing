package org.inspira.kevingutierrez.clientefilesharing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.inspira.kevingutierrez.clientefilesharing.dialogos.DialogoDeConsultaSimple;
import org.inspira.kevingutierrez.clientefilesharing.dialogos.ObtenerTexto;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> al;
    private List<String> content;
    private DataOutputStream salida;
    private File outFile;
    private ServerManager serverManager;
    private static final String SOURCE = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = new ArrayList<>();
        al = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, content);
        ((ListView)findViewById(R.id.lista)).setAdapter(al);
        if(savedInstanceState == null) {
            try {
                File f = new File(SOURCE, "TerminalSupport");
                if(!f.exists()){
                    f.mkdirs();
                }
                outFile = new File(SOURCE.concat("/TerminalSupport/Calaca.txt"));
                salida = new DataOutputStream(new FileOutputStream(outFile));
            } catch (IOException ignore) {}
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if("NaN".equals(getSharedPreferences("Host", Context.MODE_PRIVATE).getString("host", "NaN"))){
            showerBby();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        boolean consumed = false;
        if(item.getItemId() == R.id.main_menu_start_server){
            serverManager = new ServerManager();
            serverManager.start();
            consumed = true;
        }else if(item.getItemId() == R.id.main_menu_set_server){
            showerBby();
        }
        return consumed;
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(serverManager != null)
        serverManager.interruptActions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putSerializable("out_file", outFile);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        outFile = (File) savedInstanceState.getSerializable("out_file");
        assert outFile != null;
        try{ salida = new DataOutputStream(new FileOutputStream(outFile, true)); }catch(IOException ignore){}
    }

    private void showerBby(){
        ObtenerTexto ot = new ObtenerTexto();
        Bundle args = new Bundle();
        args.putString("mensaje", "Escriba la dirección del servidor");
        String tmp;
        args.putString("texto_anterior", (tmp = getSharedPreferences("Host", Context.MODE_PRIVATE).getString("host", "NaN")).equals("NaN") ? "" : tmp);
        ot.setArguments(args);
        ot.setAgenteDeInteraccion(new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
            @Override
            public void clickSobreAccionPositiva(DialogFragment dialogo) {
                String result = ((ObtenerTexto)dialogo).obtenerTexto();
                SharedPreferences.Editor editor = getSharedPreferences("Host", Context.MODE_PRIVATE).edit();
                editor.putString("host", result);
                editor.apply();
                serverManager = new ServerManager();
                serverManager.start();
            }

            @Override
            public void clickSobreAccionNegativa(DialogFragment dialogo) {

            }
        });
        ot.show(getSupportFragmentManager(), "Tul");
    }

    private void appendMessage(final String message){
        runOnUiThread(new Runnable(){ @Override public void run(){ al.add(message); } });
    }

    private class ServerManager extends Thread{

        private static final String MY_NAME = "TerminalSupport";
        private final BluetoothServerSocket serverSocket;
        private final BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket socket;
        private boolean success = false;

        public ServerManager(){
            BluetoothServerSocket zukam = null;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            try{
                zukam = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, UUID.fromString("035db532-1f9e-425a-ace8-8b271d33d3d9"));
            } catch (IOException e){
                e.printStackTrace();
            }
            serverSocket = zukam;
        }

        @Override
        public void run(){
            socket = null;
            while(true)
            try{
//                runOnUiThread(new Runnable(){ @Override public void run(){ Toast.makeText(MainActivity.this, "Preparados", Toast.LENGTH_SHORT).show(); }});
                socket = serverSocket.accept();
//                runOnUiThread(new Runnable(){ @Override public void run(){ Toast.makeText(MainActivity.this, "Aceptado", Toast.LENGTH_SHORT).show(); }});
                new Thread(){
                    @Override
                    public void run(){
                        try{//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            IOHandler ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
                            ioHandler.setRate(600);
                            String line = new String(ioHandler.handleIncommingMessage());
                            //pw.println("Ok");
                            if (!"".equals(line)) {
                                JSONObject json = new JSONObject(line);
                                JSONObject jsonObject = new JSONObject();
                                switch(json.getInt("action")){
                                    case 1:
                                        new SendWiFi(MainActivity.this, json).start();
                                        DataOutputStream salidaArchivo = new DataOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TerminalSupport/" + json.getString("nombre"))));
                                        byte[] bytesArchivo = hexStringToByteArray(json.getString("payload"));
                                        salidaArchivo.write(bytesArchivo);
                                        appendMessage(json.getString("nombre"));
                                        jsonObject.put("status", true);
                                        break;
                                    case 2:
                                        SharedPreferences sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor
                                                = sp.edit();
                                        int currUserId = sp.getInt("curr_user", 100);
                                        jsonObject.put("id", currUserId);
                                        jsonObject.put("status", true);
                                        editor.putInt("curr_user", currUserId+1);
                                        editor.commit();
                                        break;
                                }
                                ioHandler.sendMessage(jsonObject.toString().getBytes());
                            }
                        }catch(JSONException | IOException e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }catch( IOException e){
                e.printStackTrace();
                if(e.getMessage().contains("connection abort"))
                    success = true;
            }finally{
                try{
                    salida.close();
                }catch(NullPointerException | IOException ignore){ ignore.printStackTrace(); }
                runOnUiThread(new Runnable(){ @Override public void run(){ Toast.makeText(MainActivity.this, "Finaliza hilo de escritura", Toast.LENGTH_SHORT).show(); }});
//                finish();
            }
        }

        public void interruptActions(){
            try{
                salida.close();
                if(!success) outFile.delete();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
