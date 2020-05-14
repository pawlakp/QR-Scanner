package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.QRGeoModel;
import com.example.myapplication.Model.QRUrlModel;
import com.example.myapplication.Model.QRVCardModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private TextView txtResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicjlaizacja
        scannerView=(ZXingScannerView)findViewById(R.id.zxscan);
        txtResult=(TextView)findViewById(R.id.txt_result);

        //Zezwolenie na dostęp do kamery
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Zezwolono na dostęp do kamery", Toast.LENGTH_SHORT).show();
                            scannerView.setResultHandler(MainActivity.this);
                            scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Musisz zezwolić na dostęp do kamery", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();


    }

    //wznowienie kamery
    @Override
    protected void onResume() {
        super.onResume();
        scannerView.startCamera();
    }
    //wyłączenie kamery
    @Override
    protected void onStop() {
        super.onStop();
        scannerView.stopCamera();

    }


    @Override
    public void handleResult(Result rawResult) {
        //rezultat skanowania
        processRawResult(rawResult.getText());


    }

    private void processRawResult(String text) {
        if(text.startsWith("BEGIN:")) {
            String[] tokens = text.split("\n");
            QRVCardModel qrvCardModel = new QRVCardModel();
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].startsWith("BEGIN:")) {
                    qrvCardModel.setType(tokens[i].substring("BEGIN:".length())); //Remove BEGIN: to get Type
                } else if (tokens[i].startsWith("N:")) {
                    qrvCardModel.setName(tokens[i].substring("N:".length()));
                } else if (tokens[i].startsWith("ORG:")) {
                    qrvCardModel.setOrg(tokens[i].substring("ORG:".length()));
                } else if (tokens[i].startsWith("TEL:")) {
                    qrvCardModel.setTel(tokens[i].substring("TEL:".length()));
                } else if (tokens[i].startsWith("URL:")) {
                    qrvCardModel.setUrl(tokens[i].substring("URL:".length()));
                } else if (tokens[i].startsWith("EMAIL:")) {
                    qrvCardModel.setEmail(tokens[i].substring("EMAIL:".length()));
                } else if (tokens[i].startsWith("ADR:")) {
                    qrvCardModel.setAddress(tokens[i].substring("ADR:".length()));
                } else if (tokens[i].startsWith("NOTE:")) {
                    qrvCardModel.setNote(tokens[i].substring("NOTE:".length()));
                } else if (tokens[i].startsWith("SUMMARY:")) {
                    qrvCardModel.setSummary(tokens[i].substring("SUMMARY:".length()));
                } else if (tokens[i].startsWith("DTSTART:")) {
                    qrvCardModel.setDtstart(tokens[i].substring("DTSTART:".length()));
                } else if (tokens[i].startsWith("DTEND:")) {
                    qrvCardModel.setDtend(tokens[i].substring("DTEND:".length()));
                }

                //pokaz rezultat
                txtResult.setText(qrvCardModel.getType());
            }
        }
            else if(text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www.")) {

            QRUrlModel qrUrlModel = new QRUrlModel(text);
            txtResult.setText(qrUrlModel.getUrl());
            //metoda pozwalajaca na otworzenie linku
            ResultWeb(qrUrlModel.getUrl());
        }
            else if(text.startsWith("geo:")){
            QRGeoModel qrGeoModel=new QRGeoModel();
            String delims="[   ?q= ]+";
            String tokens[]=text.split(delims);

            for(int i=0;i<tokens.length;i++)
            {
                if(tokens[i].startsWith(" geo:")){
                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
                }
        }
            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLng(tokens[1]);
            qrGeoModel.setGeo_place(tokens[2]);

            txtResult.setText(qrGeoModel.getLat()+"/"+qrGeoModel.getLng());
        }
        else{
            //pokaz tekst
            txtResult.setText(text);
        }
        scannerView.resumeCameraPreview(MainActivity.this);
    }


    //metoda pozwalajaca na otworzenie linku
    public void ResultWeb(String text){
        final String myResult = text;
         onStop();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rezultat");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  onResume();
                }
            });
            builder.setNeutralButton("GO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myResult));
                    startActivity(intent);
                }
            });
            builder.setMessage(text);
            AlertDialog alert1 = builder.create();
            alert1.show();

    }


}
