package org.inspira.kevingutierrez.clientefilesharing;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by jcapiz on 14/12/15.
 */
public class IOHandler {

    private DataInputStream entrada;
    private DataOutputStream salida;
    private String allocatedString;
    private byte[] chunk;
    private int rate;

    public IOHandler(DataInputStream entrada, DataOutputStream salida){
        this.entrada = entrada;
        this.salida = salida;
        rate = 64;
    }

    public byte[] handleIncommingMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int toRead;
        int length;
        byte[] block = new byte[rate];
        // Anticipa cuantos bytes mandará el emisor, para luego leer por bloques antes de
        // esperar a que la nueva trama llegue.
        while ((toRead = readInt()) > 0) {
//            Log.e("IOHandler", "Recibiremos: " + toRead + " bytes");
            int times = toRead/rate;
            for ( int i=0; i<times; i++ ) {
                length = entrada.read(block);
                baos.write(block, 0, length);
            }
            int remaining = toRead - times*block.length;
            if( remaining > 0 ){
                entrada.read(block);
                baos.write(block, 0, remaining);
            }
            writeInt(1);
        }
//        Log.e("IOHandler", "Terminamos de recibir.");
        //System.out.println("Done reading $$$$" + baos.toString("UTF-8") + "$$$$");
        allocatedString = baos.toString("UTF-8");
        chunk = baos.toByteArray();
        baos.close();
        return chunk;
    }

    public void sendMessage(byte[] message) throws IOException {
        int times = message.length/rate;
//        System.out.println("Writing " + times + " blocks from " + message.length + " bytes.");
        for( int i=0; i<times; i++ ){
//            Log.e("IOHandler", "Going to send " + rate + " bytes");
            writeInt(rate);
            salida.write(message, i * rate, rate);
            readInt();
//            Log.e("IOHandler", "Bloque confirmado");
        }
        int remaining = message.length - times*rate;
        if(remaining > 0){
            writeInt(remaining);
//            Log.e("IOHandler", "Enviando bytes restantes");
            salida.write(message,times*rate,remaining);
            readInt();
        }
        writeInt(0);
    }

    public int readInt() throws IOException {
        return entrada.readInt();
    }

    public void writeInt(int i) throws IOException {
        salida.writeInt(i);
        salida.flush();
    }

    public long readLong() throws IOException {
        return entrada.readLong();
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getAllocatedString(){
        return allocatedString;
    }

    public void close() throws IOException {
        entrada.close();
        salida.close();
    }
}
