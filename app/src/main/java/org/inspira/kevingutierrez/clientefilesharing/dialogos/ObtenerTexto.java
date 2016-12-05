package org.inspira.kevingutierrez.clientefilesharing.dialogos;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.inspira.kevingutierrez.clientefilesharing.R;

/**
 * Created by jcapiz on 30/12/15.
 */
public class ObtenerTexto extends DialogFragment {

    private EditText inputText;
    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion;

    public void setAgenteDeInteraccion(DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agente){
        agenteDeInteraccion = agente;
    }

    public String obtenerTexto(){
        return inputText.getText().toString();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("mensaje"));
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inputTextLayout = inflater.inflate(R.layout.input_text, null, false);
        inputText = (EditText) inputTextLayout.findViewById(R.id.input_text);
        String textoAnterior = getArguments().getString("texto_anterior");
        if(textoAnterior != null)
            inputText.setText(textoAnterior);
        builder.setView(inputTextLayout);
        builder.setPositiveButton("Confirmar", new ClickSobreAccionPositiva());
        return builder.create();
    }

    private class ClickSobreAccionPositiva implements Dialog.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int id){
            if(agenteDeInteraccion != null ) agenteDeInteraccion.clickSobreAccionPositiva(ObtenerTexto.this); else Log.e("Shu", "No pudimos ejecutar la acción");
        }
    }
}
