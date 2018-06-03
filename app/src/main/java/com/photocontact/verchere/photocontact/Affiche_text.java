package com.photocontact.verchere.photocontact;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by VERCHERE on 24/11/2017.
 */

public class Affiche_text extends AppCompatActivity {

    Bitmap image;
    String datapath = "";
    ImageView imageView;
    Button contact;
    String numero;
    boolean isnum=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partie_ocr);

        //init image
        File root = Environment.getExternalStorageDirectory();
        //image=BitmapFactory.decodeResource(getResources(),R.drawable.test_image);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        String _path=root.getAbsolutePath()+"/pic.jpg";

        image=BitmapFactory.decodeFile(_path,options);
        imageView=(ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(image);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            if (rotate != 0) {
                int w = image.getWidth();
                int h = image.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap & convert to ARGB_8888, required by tess
                image = Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);
            }
            image = image.copy(Bitmap.Config.ARGB_8888, true);
        }catch (IOException e){
            Log.e("toto","ioexception");
        }


        //recherche du texte
        contact=(Button)findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
                OCRTextView.setText("Recherche de numéro");
                adPhone(OCRTextView);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.help:
                AlertDialog.Builder help = new AlertDialog.Builder(Affiche_text.this);
                help.setCancelable(false);
                help.setTitle("Aide");
                help.setMessage("1-Prendre en photo le numéro avec le bouton 'PRENDRE LA PHOTO'\n\n2-Si la photo est bonne, appuyer sur le bouton 'CHERCHER LE NUMERO'\n\n3-Si un numéro est trouvé, la page d'insertion de contact s'affiche avec le numéro déjà écrit.  " );
                help.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action for "OK"
                    }
                })
                ;
                final AlertDialog alertHelp = help.create();
                alertHelp.show();
                return true;
            case R.id.about:
                AlertDialog.Builder about = new AlertDialog.Builder(Affiche_text.this);
                about.setCancelable(false);
                about.setTitle("A propos");
                about.setMessage("Si vous rencontrez un problème \nou avez la moindre suggestion ou commentaire, \nn' hésitez pas à nous contacter par mail.\n \nEmail : remi.verchere2@gmail.com" );
                about.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action for "OK"
                    }
                })
                ;
                final AlertDialog alertAbout = about.create();
                alertAbout.show();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void adPhone(TextView OCRTextView){

        //partie ocr

        String language="fra";
        datapath = getFilesDir()+ "/tesseract/";
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(datapath, language);
        baseApi.setImage(image);

        String OCRresult = baseApi.getUTF8Text();
        baseApi.end();

        if(language.equalsIgnoreCase("fra")){
            OCRresult=OCRresult.replaceAll("[^a-zA-Z0-9]+"," ");
        }

        boolean tailleInitOk=true;
        Log.e("test", "av: "+OCRresult);
        while (!OCRresult.substring(0,1).equals("0")){

            if(OCRresult.length()>10) {
                OCRresult = OCRresult.substring(1);
                Log.e("test", OCRresult);
            }
            else {
                tailleInitOk=false;
                break;
            }
        }
        if (tailleInitOk) {
            OCRTextView.setText(OCRresult);

            Log.e("test", OCRresult);
            int debutNum = OCRresult.indexOf("0");
            numero = "0";
            Log.e("test", numero);
            debutNum = debutNum + 1;
            while (numero.length() < 10) {
                Log.e("test", numero);
                int isTailleNum = OCRresult.length() - debutNum;
                if (isTailleNum >0) {
                    boolean result = recupNum(debutNum, OCRresult);
                    if (!result) {
                        Log.e("test", "char incorrect");
                        OCRresult=OCRresult.substring(debutNum);
                        numero="";
                        debutNum=OCRresult.indexOf("0");
                        if (debutNum==-1){
                            isnum = false;
                            break;
                        }
                        else{
                            numero="0";
                        }
                    }
                    debutNum = debutNum + 1;
                }else {
                    Log.e("test", String.valueOf(isTailleNum));
                    isnum=false;
                    break;
                }

            }
            if (isnum) {
                Log.e("test", numero);
                OCRTextView.setText(String.valueOf(numero));
                ajoutContact();
            } else {
                Log.e("test", numero);
                OCRTextView.setText("Pas de numéro trouvé");
            }

        }
        else{
            OCRTextView.setText("Pas de numéro francais trouvé");
        }

    }

    public boolean recupNum(int index,String txt){
        String element=txt.substring(index,index+1);
        Log.e("test","element "+element);
        switch (element){
            case " ":
                return true;
            case "0":
                numero=numero+"0";
                return true;
            case "1":
                numero=numero+"1";
                return true;
            case "2":
                numero=numero+"2";
                return true;
            case "3":
                numero=numero+"3";
                return true;
            case "4":
                numero=numero+"4";
                return true;
            case "5":
                numero=numero+"5";
                return true;
            case "6":
                numero=numero+"6";
                return true;
            case "7":
                numero=numero+"7";
                return true;
            case "8":
                numero=numero+"8";
                return true;
            case "9":
                numero=numero+"9";
                return true;
            default:
                return false;
        }
    }

    public void ajoutContact(){
        // Creates a new intent for sending to the device's contacts application
        Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);

        // Sets the MIME type to the one expected by the insertion activity
        insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        // Sets the new contact name
        //insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, "NOM");
        insertIntent.putExtra(ContactsContract.Intents.Insert.PHONE, numero);



        // Defines an array list to contain the ContentValues objects for each row
        ArrayList<ContentValues> contactData = new ArrayList<ContentValues>();


        // Sets up the row as a ContentValues object
        ContentValues phoneRow = new ContentValues();

        // Specifies the MIME type for this data row (all data rows must be marked by their type)
        phoneRow.put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        );

        // Adds the phone number and its type to the row
        phoneRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numero);

        // Adds the row to the array
        contactData.add(phoneRow);

        //Adds the array to the intent's extras. It must be a parcelable object in order to
        //travel between processes. The device's contacts app expects its key to be
        //Intents.Insert.DATA

        insertIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

        // Send out the intent to start the device's contacts app in its add contact activity.
        startActivity(insertIntent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent returnhome=new Intent(Affiche_text.this,MainActivity.class);
            startActivity(returnhome);
            finish();
            return true;
        }
        return false;

    }
}
