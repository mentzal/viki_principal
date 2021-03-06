package com.example.pelu.viki;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.widget.MediaController.MediaPlayerControl;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.AsyncTask;
import java.util.ArrayList;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;


public class MainActivity extends AppCompatActivity implements edu.cmu.pocketsphinx.RecognitionListener, TextToSpeech.OnInitListener {


    private SpeechRecognizer recognizer;
    private static final String KEYPHRASE = "hola";
    private static final String MENU_SEARCH = "accede al menú";
    private static final String KWS_SEARCH = "hola";
    private static final String NOMBRE_VIKI = "viki";
    private static final String LISTASPOTY = "loquillo";
    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    ArrayList<String> listItems = new ArrayList<String>();

    //debemos almacenar para mostrar en la lista, el nombre de la play list que esté en la config de usuario
    //TODO: los datos mejor en base de datos ¿?? //
    String tituloPlayList[] = {"FIREHOUSE", "BBKING"};
    String Playlist[] = {"playlist:1X7lWdHqmPajeBECHtSVD3","playlist:0nSQ0Uaw66rJ3vZy9MUomB"};
    String Canciones[] = {};
    String Albumes[] = {"LOQUILLO","BBKING","LA LEYENDA DE LA MANCHA","GARY MOORE","STAYOZ","TOM JONES","WARCRY","DEBLER"};
    String Artistas[] = {};

    ArrayAdapter<String> adapter;
    ListView listaDispo;
    Button playlist, canciones,album, artista;
    TableLayout spotifyTabla;
    MenuItem MusicaPlay, MusicaNext, MusicaStop;



    private boolean dance = false;
    private boolean spoty_playLists = false;
    private boolean Spotypanel = false;
    private boolean listaRepord =  false;
    private boolean albumes =  false;
    private boolean artistas =  false;



    MailJob mail;

    /*variables dictado por voz */
    TextView grabar;
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;

    private int colorFondoLista = Color.parseColor("#973b89c7");


    //TODO: NOTA--> BUSCAR PALABRA CLAVE EN LA REPRODUCCION DE LA MUSICA // -->

    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);
        setContentView(R.layout.activity_main);


        MusicaNext = (MenuItem) findViewById(R.id.action_shuffle);
        MusicaStop = (MenuItem) findViewById(R.id.action_end);
        listaDispo = (ListView) findViewById(R.id.lista);
        playlist = (Button) findViewById(R.id.button8);
        canciones = (Button) findViewById(R.id.button7);
        album = (Button) findViewById(R.id.button10);
        artista = (Button) findViewById(R.id.button9);
        spotifyTabla = (TableLayout) findViewById(R.id.TablaSpoty);


        listaDispo.setBackgroundColor(255);
        spotifyTabla.setVisibility(View.INVISIBLE);

        textToSpeech = new TextToSpeech(this, this);
       // new MyAsynk().execute();
        runRecognizerSetup();





        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                System.out.println("boton presionado");
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            wakeup("85.56.134.181", "309C238975FF");

                        } catch (Exception e) {

                            showAlert();
                        }

                    }
                }).start();

                creaMusica();
                openWhatsApp(v);

            }
                   /*
            Abre whatsapp y envía mensaje al numero indicado
                                 */
            public void openWhatsApp(View view){
                try {
                    String text = "This is a test";// Replace with your message.

                    String toNumber = "34680701211"; // Replace with mobile phone number without +Sign or leading zeros.


                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
                    startActivity(intent);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


    }

                                        /*
Llamada al archivo xml que contien el menu... si no dará error
                                        */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

                            /*
    Abre spoty con la url de la lista que le pasemos y del tipo que sea
                            */

    public void abrespoty(String url){

                                /*
        TODO: La lista de reproduccion se obtiene, sacando el enlace de spotyfy web. Así podremos agregar las listas que queramos.
                                 */

        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);

        if(listaRepord = true){
            intent.setData(Uri.parse("spotify:user:mentzal:" + url));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try{

            MainActivity.this.startActivity(intent);
        }catch (Exception e){

            showAlert();
        }

    }

            /*
    Diálogo de alerta
            */

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ERROR")
                .setMessage("Error en la acción")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }



    public void creaMusica() {


        final String Cancion;
        listaDispo.setBackgroundColor(colorFondoLista);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems) {


            /*
    ESTILO TEXTVIEW DE LA LISTA
            */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                View view = super.getView(position, convertView, parent);

                TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);

                ListItemShow.setTextColor(Color.parseColor("#FFFFFF"));


                return view;
            }
        };


        if (dance == true && spoty_playLists == false) {

            try{
                File f = new File(Environment.getExternalStorageDirectory() + "/Music/dance");
                File[] files = f.listFiles();
                adapter.clear();

                for (int i = 0; i < files.length; i++)

                {
                    //Sacamos del array files un fichero
                    File file = files[i];

                    //Si es directorio...
                    if (file.isDirectory())

                        adapter.add(file.getName() + "/");

                        //Si es fichero...
                    else

                        adapter.add(file.getName());
                }
            }catch (Exception e){

                showAlert();
            }

        } else if (dance == false && spoty_playLists == false) {

            try{
                File f = new File(Environment.getExternalStorageDirectory() + "/Music/");
                File[] files = f.listFiles();
                adapter.clear();

                for (int i = 0; i < files.length; i++)

                {
                    //Sacamos del array files un fichero
                    File file = files[i];

                    //Si es directorio...
                    if (file.isDirectory()){

                    }

                    // adapter.add(file.getName() + "/"); //todo: añade al array los directorios (carpetas).--

                    //Si es fichero...
                    else

                        adapter.add(file.getName());
                }
            }catch (Exception e ){

                showAlert();
            }

        }

        //todo: preparando para mostrar la lista de playlist de spotify

        else if(spoty_playLists == true){

            try{


                adapter.clear();
                for(int i = 0; i<Playlist.length; i++){

                    adapter.add(tituloPlayList[i].toString());
                    adapter.add(Playlist[i].toString());


                }
                spotifyTabla.setVisibility(View.INVISIBLE);
            }catch (Exception e){

                showAlert();
            }

        }


        listaDispo.setAdapter(adapter);
        listaDispo.setVisibility(View.VISIBLE); //hace invisible o visible la lista //
        final String PATH_TO_FILE = "/sdcard/Music/";
        int tamanioAdapter =  adapter.getCount(); // tamaño del array de canciones //

        //todo: código duplicado-- REVISAR -- PARA DEPURACIÓN --
        if(dance == true && spoty_playLists == false){

                try{

                    Cancion =PATH_TO_FILE +"dance/" + adapter.getItem(0);
                    System.out.println(Cancion);

                    mediaPlayer = new MediaPlayer();


                    try {
                        mediaPlayer.setDataSource(Cancion);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();




                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                   @Override
                   public void onCompletion(MediaPlayer mp) {



                   }
                   });

                    recognizer.stop();
                    recognizer.startListening(MENU_SEARCH);
                }catch (Exception e){

                    showAlert();
                }


        }
        else if(dance == false && spoty_playLists == false){

            try{
                Cancion =PATH_TO_FILE + adapter.getItem(0);
                System.out.println(Cancion);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(Cancion);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();

                                            /*
                TODO: REVISAR METODO PARA SEGUIR LA SIGUIENTE CANCION AUTOMATICAMENTE

                                             */

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {




                    }
                });

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);

            }catch (Exception e){

                showAlert();
            }

        }

        // Accion para realizar al pulsar sobre un item de la lista
        listaDispo.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                System.out.println(adapter.getItem(i));

                if(spoty_playLists == true){

                    abrespoty(adapter.getItem(i));
                }


          else if (mediaPlayer != null && mediaPlayer.isPlaying()) {

               mediaPlayer.stop();
               mediaPlayer.release();
               mediaPlayer = null;
           }

           //Devolvemos el resultado de la selección
           Intent data = new Intent();
           data.putExtra("filename", adapter.getItemId(i));
           setResult(RESULT_OK, data);
           System.out.println(adapter.getItem(i));

           mediaPlayer = new MediaPlayer();

           //todo: la ruta depende de la carpeta donde nos encontremos ---REVISAR---



           try {
               if(dance == true && Spotypanel == false){
                   String PATH_TO_FILE = "/sdcard/Music/dance/" + adapter.getItem(i);
                   mediaPlayer.setDataSource(PATH_TO_FILE);
               }

               else if(dance == false && Spotypanel == false){

               }String PATH_TO_FILE = "/sdcard/Music/" + adapter.getItem(i);
               mediaPlayer.setDataSource(PATH_TO_FILE);


           } catch (IOException e) {
               e.printStackTrace();
           }
           try {
               if(Spotypanel == false){

                   //TODO: DA ERROR PORQUE USAMOS LA MISMA LISTA PARA TODA LA MUSICA // REVISAR  //
                   mediaPlayer.prepare();

               }

           } catch (IOException e) {
               e.printStackTrace();
           }

           mediaPlayer.start();
           recognizer.stop();
           recognizer.startListening(MENU_SEARCH);

       }
   });

}

@Override
protected void onPause() {
   super.onPause();

}

public void apagarViki() {
   textToSpeech.speak("apagando", TextToSpeech.QUEUE_FLUSH, null, null);
   recognizer.stop();

}

//todo: abre el nabegador chrome //
void open(Activity activity, String url) {
   Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
   Intent i = new Intent(Intent.ACTION_VIEW, uri);
   if (i.resolveActivity(activity.getPackageManager()) == null) {
       i.setData(Uri.parse(url));
   }
   activity.startActivity(i);
}


private void runRecognizerSetup() {
   // Recognizer initialization is a time-consuming and it involves IO,
   // so we execute it in async task
   new AsyncTask<Void, Void, Exception>() {
       @Override
       protected Exception doInBackground(Void... params) {
           try {
               Assets assets = new Assets(MainActivity.this);
               File assetDir = assets.syncAssets();
               setupRecognizer(assetDir);
           } catch (IOException e) {
               return e;
           }
           return null;
       }

       @Override
       protected void onPostExecute(Exception result) {
           if (result != null) {
               System.out.println(result.getMessage() + "asincrono");
           } else {
               switchSearch(KWS_SEARCH);
           }
       }
   }.execute();
}


private void setupRecognizer(File assetsDir) throws IOException {

   recognizer = SpeechRecognizerSetup.defaultSetup()
           .setAcousticModel(new File(assetsDir, "es-ptm"))
           .setDictionary(new File(assetsDir, "es.dict"))
           //.setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
           .setKeywordThreshold(Float.valueOf("1.8"))
           // Disable this line if you don't want recognizer to save raw
           // audio files to app's storage
           // .setRawLogDir(assetsDir)
           .getRecognizer();
   recognizer.addListener(this);
   // Create keyword-activation search.
   recognizer.addKeyphraseSearch(KWS_SEARCH, NOMBRE_VIKI);

   //todo: utilizamos el archivo de gramática para nvegar por él si queremos //
   // Create your custom grammar-based search

   File menuGrammar = new File(assetsDir, "menu.gram");
   recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
   recognizer.addGrammarSearch(LISTASPOTY, menuGrammar);


}


@Override
public void onBeginningOfSpeech() {

}


@Override
protected void onDestroy() {
   super.onDestroy();

   if (recognizer != null) {
       recognizer.cancel();
       recognizer.shutdown();
   }
}

@Override
public void onEndOfSpeech() {
   if (!recognizer.getSearchName().equals(KWS_SEARCH))
       switchSearch(KWS_SEARCH);

}


@Override
public void onPartialResult(Hypothesis hypothesis) {

   textToSpeech.setLanguage(Locale.UK);
   Locale locSpanish = new Locale("spa", "ES");
   textToSpeech.setLanguage(locSpanish);


   if (hypothesis == null)
       return;

   String text = hypothesis.getHypstr();


   if (text.equals(KEYPHRASE)) {
       recognizer.stop();
       switchSearch(MENU_SEARCH);
   } else

   {
       recognizer.stop();
       recognizer.startListening(MENU_SEARCH);
       System.out.println(hypothesis.getHypstr() + " Hypotesis Onpartial");

   }

}

/*
ENCENDIDO REMOTO
*/
    private static byte[] getMacBytes(String mac) throws IllegalArgumentException {
        Log.d("GetMacBytes", "method started");
// TODO Auto-generated method stub
        byte[] bytes = new byte[6];
        try {
            String hex;
            for (int i = 0; i < 6; i++) {
                hex = mac.substring(i * 2, i * 2 + 2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);
                Log.d("GetMacbytes", "calculated");
                Log.d("GetMacBytes (bytes)", new String(bytes));
            }
        } catch (NumberFormatException e) {
            Log.e("GetMacBytes", "error");
        }
        return bytes;
    }

    public static void wakeup(String broadcastIP, String mac) {
        Log.d("wakeup", "method started");
        if (mac == null) {
            Log.d("Mac error at wakeup", "mac = null");
            return;
        }

        try {
            byte[] macBytes = getMacBytes(mac);
            Log.d("wakeup (bytes)", new String(macBytes));
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            Log.d("wakeup", "calculating completed, sending...");
            InetAddress address = InetAddress.getByName(broadcastIP);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            Log.d("wakeup", "Magic Packet sent");


        } catch (Exception e) {
            Log.e("wakeup", "error");
        }

    }
             /*
    FIN ENCENDIDO REMOTO
             */


    @Override
    public void onResult(Hypothesis hypothesis) {

         boolean listalokillo = false;

        if (hypothesis != null) {


            System.out.println(hypothesis.getHypstr() + "hipotesis");



            if (hypothesis.getHypstr().equals("encender ordenador")) {
                textToSpeech.speak("encendiendo", TextToSpeech.QUEUE_FLUSH, null, null);

                new Thread(new Runnable() {
                    public void run() {
                        wakeup("192.168.1.112", "309C238975FF");
                    }
                }).start();

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            } else if (hypothesis.getHypstr().equals("abre navegador")) {
                // abre el navegador chorme //
                open(MainActivity.this, "http://www.google.com");
                textToSpeech.speak("abriendo navegador", TextToSpeech.QUEUE_FLUSH, null, null);

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            } else if (hypothesis.getHypstr().equals("abre whatsapp")) {


                textToSpeech.speak("abriendo whatsapp", TextToSpeech.QUEUE_FLUSH, null, null);
                Context ctx = MainActivity.this; // or you can replace **'this'** with your **ActivityName.this**
                Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                ctx.startActivity(i);


                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);

            } else if (hypothesis.getHypstr().equals("abre spotify")) {

                textToSpeech.speak("abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                spotifyTabla.setVisibility(View.VISIBLE);
                Spotypanel = true;

            }

            else if(hypothesis.getHypstr().equals("abre playlist") && Spotypanel == true){

                listaRepord = true;  // para saber si la url pertenece a playlist..> para el método "abrespoty" //
                spoty_playLists = true;
                dance = false;
                creaMusica();

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);


                spoty_playLists = true;
                creaMusica();

            }

             else if (hypothesis.getHypstr().equals("abre música")) {

                                /*
                  reproduce una cancion selecionada
                                 */
                dance = false;
                creaMusica();

                // musica = true;
                textToSpeech.speak("reproduciendo", TextToSpeech.QUEUE_FLUSH, null, null);
                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);


            }
                    /*
              Detiene la reproduccion
                    */

            else if (hypothesis.getHypstr().equals("para música")) {

                mediaPlayer.stop();
                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
                //musica=false;

            } else if (hypothesis.getHypstr().equals("cierra música")) {
                dance = false;
                mediaPlayer.stop();
                recognizer.stop();
                listaDispo.setVisibility(View.INVISIBLE);
                recognizer.startListening(MENU_SEARCH);
            }

            //todo: hacer lista dinámica...par amostrar unas canciones u otras ... sacar del oncreate //
            else if (hypothesis.getHypstr().equals("música dance")) {
                dance = true;
                // mediaPlayer.stop();
                creaMusica();
                recognizer.stop();

                //  lista.setVisibility(View.INVISIBLE);
                recognizer.startListening(MENU_SEARCH);


            }
            /*
            else if (hypothesis.getHypstr().equals("a dormir")) {

                apagarViki();
            }
            */

            else if(hypothesis.getHypstr().equals("hola viki")){
                textToSpeech.speak("hola", TextToSpeech.QUEUE_FLUSH, null, null);
                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            }

            else if(hypothesis.getHypstr().equals("manda correo")){
                textToSpeech.speak("Enviando correo", TextToSpeech.QUEUE_FLUSH, null, null);

                 /*
        ENVIO DE CORREO DE PRUEBA
        En la cuenta de correo de gmail, permitir acceso a aplicaciones no seguras.
                */
                  new MailJob("belaklord@gmail.com", "A968908054").execute(
                  new MailJob.Mail("belaklord@gmail.com", "belaklord@gmail.com", "subjeto", "contenido") );

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            }

            else if(hypothesis.getHypstr().equals("enviar correo")){

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Dictado.class);
                startActivity(intent);
                recognizer.stop();
                finish();

            }

        }

    }

    @Override
    public void onError(Exception e) {

        System.out.println(e.getMessage());

    }

    @Override
    public void onTimeout() {


        switchSearch(KWS_SEARCH);
    }


    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH)) {
            recognizer.startListening(searchName);
        } else {
            // textToSpeech.speak("no se ha accedido al menú", TextToSpeech.QUEUE_FLUSH, null,null);

            recognizer.startListening(searchName, 10000);

        }

    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public void onStop() {
        super.onStop();

        /*
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        */
    }

}
