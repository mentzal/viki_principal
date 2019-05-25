package com.example.pelu.viki;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class GatosExcel extends AppCompatActivity {

    Button guardar,subir;
    EditText titulo, importe;

    //conexión ftp variable //

    private FTPconnect ftpconnect = null;
    FileInputStream fis = null;
    BufferedInputStream buffer;
    BufferedReader bufferRead;
    FileOutputStream stream = null;

    /*
  variable Objeto FTP
   */
    FTPClient ftpClient = new FTPClient();

    MenuItem  MusicaNext, MusicaStop; //menú superior



    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.gastos);

        subir = (Button) findViewById(R.id.subir);
        titulo = (EditText) findViewById(R.id.titulo);
        importe = (EditText) findViewById(R.id.importe);
        guardar = (Button) findViewById(R.id.guardar);


                        /*
        Modifica y guarda el excel en el terminal
                        */

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CreaExcel(titulo.getText().toString(),importe.getText().toString()); //añade la nueva línea
            }
        });



        subir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Guarda los datos en la excel y los sube al FTP //


                /*
                todo: falla al subir al ftp y corrompe el archivo


                tratarFichero();

                ftpconnect = new FTPconnect();

                new Thread(new Runnable() {


                    @Override
                    public void run() {

                        boolean status = false;
                        status = ftpconnect.ftpConnect("85.56.134.181", "belaklord", "a968908054", 21);

                        if(status == true){


                            try {
                                ftpClient.connect("85.56.134.181", 21);
                                ftpClient.login("belaklord", "a968908054");
                                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                ftpClient.enterLocalPassiveMode();

                                FTPFile[] files = ftpClient.listFiles();
                                printFileDetails(files);

                                //comparamos las fechas d emodificación de ambos archivos //

                                if(tratarFichero() < printFileDetails(files)){

                                    String archivo = "Gastos.xls";
                                    File sdCard = Environment.getExternalStorageDirectory();
                                    File directory = new File(sdCard.getAbsolutePath() + "/javatechig.todo");
                                    stream = new FileOutputStream(directory + "/Gastos.xls");

                                    ftpClient.retrieveFile(archivo, stream);//pone el archivo en tu stream
                                    stream.close();
                                    ftpClient.disconnect();

                                   // CreaExcel(titulo.getText().toString(),importe.getText().toString()); //añade la nueva línea
                                    subeftp(); //sube el arcivo al ftp

                                }

                                else{

                                   // CreaExcel(titulo.getText().toString(),importe.getText().toString());
                                    subeftp();

                                }


                            } catch (IOException e) {

                            }

                        }

                        else{
                            GatosExcel.this.runOnUiThread(new Runnable() {

                                public void run() {

                                    Toast.makeText(getApplicationContext(), "No se ha podido conectar con el servidor FTP", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                }).start();
                */
            }
        });

    }


    public void CreaExcel(String motivo, String cantidad) {



                        File sdCard = Environment.getExternalStorageDirectory();
                        File directory = new File(sdCard.getAbsolutePath() + "/javatechig.todo");
                        File inputWorkbook = new File(directory + "/Gastos.xls");

                        Workbook w;

                        try {
                            w = Workbook.getWorkbook(inputWorkbook);

                            // Get the first sheet

                            Sheet sheet = w.getSheet(0);
                            WritableWorkbook wkr = Workbook.createWorkbook(inputWorkbook, w);
                            WritableSheet getsht = wkr.getSheet(0);



                            //todo: Obtener el numeto total de columnas y filas para insertar el nuevo dato

                            int contador = 0;

                            for (int j = 0; j < sheet.getRows(); j++) {

                                Cell celda = sheet.getCell(0,j); //obtenemos las celdas de la primera columna ("0")
                                Cell celda_2 = sheet.getCell(2,j); // obtenemos las celdas de la tercera columna columna ("2")

                                System.out.println(celda.getContents());
                                System.out.println(celda_2.getContents());

                            /*
                Añade un nuevo gasto fijo
                            */
                                if(celda_2.getContents()== ""  && contador == 0){
                                    contador ++;
                                    Label label = new Label(2, j, motivo); // título del gasto
                                    getsht.addCell(label);


                                    NumberFormat decimalNo = new NumberFormat("#.0");
                                    WritableCellFormat numberFormat = new WritableCellFormat(decimalNo);
                                    //write to datasheet
                                    int cantidad_final =   Integer.parseInt(cantidad);
                                    Number numberCell = new Number(3, j, cantidad_final, numberFormat);  //Añade la celda del gasto fijo en modo número //
                                    getsht.addCell(numberCell);


                                    wkr.write();
                                    wkr.close();


                                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                    builder.setMessage("Se ha guardado").setPositiveButton("OK", dialogClickListener).show();



                                    //todo: falta agregar una celda de prueba //

                                }


                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (BiffException e) {
                            e.printStackTrace();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }

            }


    public void subeftp(){

        ftpconnect = new FTPconnect();

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {


                    ftpClient.connect("85.56.134.181", 21);
                    ftpClient.login("belaklord", "a968908054");
                    File files = new File("/sdcard/javatechig.todo/Gastos.xls");
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.enterLocalPassiveMode();
                    buffer = new BufferedInputStream(new FileInputStream(files));


                        if (ftpClient.storeFile("Gastos.xls", buffer)){

                            //Informa al usuario
                            GatosExcel.this.runOnUiThread(new Runnable() {

                                public void run() {

                                    Toast.makeText(getApplicationContext(), " Se ha Subido ", Toast.LENGTH_SHORT).show();
                                }
                            });

                            buffer.close();        //Cierra el bufer

                        }
                        else{

                            //Informa al usuario
                            System.out.println("!!!!!nO Se ha subid!!!!!o");

                            buffer.close();        //Cierra el bufer

                        }

                }catch(Exception e){

                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"No se ha podido conectar con el servidor FTP", Toast.LENGTH_LONG).show();
                }
            }
        }).start();

    }


                                    /*
   Obtiene el tamaño y la fecha de creacion del fichero
                                    */

    public static long  tratarFichero() {


        String path = "/sdcard/javatechig.todo";
        String nombreFichero = "Gastos.xls";
        long mt = 0;


        File f = new File(path);
        File file[] = f.listFiles();


        for (int i = 0; i < file.length; i++) {
            nombreFichero = file[i].getName();


            if (nombreFichero.equals("Gastos.xls")) {

                // System.out.println("El nombre del fichero es :"+file[i].getName());
                mt = file[i].lastModified(); // tiempo en milisegundos que se modifico el archivo.
                System.out.println("Fecha en milisegundos del archivo en terminal: "+mt);

            }

        }
        return mt;
    }

       /*
        devuelve el tiempo en milisegundos el archivo FTP
                                */

    private static long printFileDetails(FTPFile[] files) {

        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar mt;

        long mt2 = 0;

        for (FTPFile file : files) {

            if(file.getName().equals("Gastos.xls")){

                mt = file.getTimestamp();
                System.out.println("Fecha de modificacion del FTP milisegundos : "+mt.getTimeInMillis());
                mt2 = mt.getTimeInMillis();

            }
        }

        return mt2;
    }

                          //Cuadro de diálogo con confirmación //

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {


        @Override
        public void onClick(DialogInterface dialog, int which) {


            switch (which){

                case DialogInterface.BUTTON_POSITIVE:



                    break;


                case DialogInterface.BUTTON_NEGATIVE:



                    break;
            }

        }
    };


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem sufle = menu.findItem(R.id.action_shuffle);
        MenuItem salir = menu.findItem(R.id.action_end);


        sufle.setVisible(true);
        salir.setVisible(true);

        MenuItem off = menu.findItem(R.id.estadoVikiOff);
        MenuItem on = menu.findItem(R.id.estadoVikiOn);
        on.setVisible(false);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_shuffle:
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.pelu.viki");
                startActivity(launchIntent);

                super.finish();


                break;
            case R.id.action_end:
                System.exit(0);

                break;

        }
        return super.onOptionsItemSelected(item);
    }




}
