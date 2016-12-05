package org.inspira.kevingutierrez.clientefilesharing.dialogos;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by jcapiz on 30/12/15.
 */
public class DialogoDeConsultaSimple extends DialogFragment {

    public interface AgenteDeInteraccionConResultado {
        void clickSobreAccionPositiva(DialogFragment dialogo);
        void clickSobreAccionNegativa(DialogFragment dialogo);
    }

    private AgenteDeInteraccionConResultado agenteDeInteraccion;

    public void setAgenteDeInteraccion(AgenteDeInteraccionConResultado agente){
        agenteDeInteraccion = agente;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle extras = getArguments();
        String mensaje;
        if( extras != null && (mensaje = extras.getString("mensaje")) != null)
            builder.setMessage(mensaje);
        else
            builder.setMessage("¿Podemos registrar su ubicación geográfica?");
        builder.setPositiveButton("Sí", new AccionPositiva());
        builder.setNegativeButton("No", new AccionNegativa());
        return builder.create();
    }

    private class AccionPositiva implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface di, int id){
            agenteDeInteraccion.clickSobreAccionPositiva(DialogoDeConsultaSimple.this);
        }
    }

    private class AccionNegativa implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface di, int id){
            agenteDeInteraccion.clickSobreAccionNegativa(DialogoDeConsultaSimple.this);
        }
    }
}
