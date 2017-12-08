package de.hs_hannover.mfinimento.wakeapp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText et;
    Button btn;
    TextView tv;

    final String scripturlstring = "http://finimento.de";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.editText);
        tv = (TextView) findViewById(R.id.textView);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(internetAvailable()) {
                    sendToServer(et.getText().toString());
                }else{
                    Toast.makeText(getApplicationContext(), "Internet ist nicht verfügbar.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendToServer(final String text){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String textparam = "text1=" + URLEncoder.encode(text, "UTF-8");   //unser Text für den http-Request

                    URL scripturl = new URL(scripturlstring);   //neues URL Object aus der Web-Server-Domain erstellen
                    HttpURLConnection connection = (HttpURLConnection) scripturl.openConnection();   //ein HttpURLConnection Objekt zum Senden und Empfangen der Daten aus dem URL-Objekt erstellen
                    connection.setDoOutput(true);   //Das HttpURLConnection Objekt auf Post und Ausgabe auf True stellen
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");   //Die Funktion setRequestProperty() wird als Accept-Encoding Request Header verwendet, um eine automatische Dekomprimierung zu deaktivieren. Ganz nach dem Motto 'client.setRequestProperty(“Key”,”Value”)'   Versteh ich nicht wirklich
                    connection.setFixedLengthStreamingMode(textparam.getBytes().length);  // Informiert den Server darüber wie groß die gesendete Datei ist. (PerformanceGründe: ansonsten würde das HttpURLConnection Objekt den kompletten Body im Speicher puffert, bevor er übermittelt wird)

                    //sendenAnfang
                    OutputStreamWriter contentWriter = new OutputStreamWriter(connection.getOutputStream());  // mit QutputStreamWriter können wir den Inhalt an die URL-Connection(connection) senden
                    contentWriter.write(textparam);   //unseren zusammengebauten Text für die PHP (textparam) in den OutputStreamWriter schreiben
                    contentWriter.flush();   //Sicherstellen das der OutputStreamWriter nun leer (spülen) ist um Ihn daraufhin schließen zu können.
                    contentWriter.close();   //OutputStreamWriter schlie0en
                    //sendenEnde

                    InputStream answerInputStream = connection.getInputStream();
                    final String answer = getTextFromInputStream(answerInputStream);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(answer);
                        }
                    });
                    answerInputStream.close();
                    connection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //holenAnfang
    public String getTextFromInputStream(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();

        String aktuelleZeile;
        try {
            while ((aktuelleZeile = reader.readLine()) != null){
                stringBuilder.append(aktuelleZeile);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString().trim();
    }
    //holenEnde

    public boolean internetAvailable(){   //Diese Funktion fragt, ob wir aktuell Internet haben bzw. das Netz grad am aufbauen ist. Und gibt dementsprechen True oder False zurück
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
