package learn.develop.ru.translater;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    Spinner lang_in, lang_out;
    EditText editText1, editText2;
    Button translate, clear, change;
    MyTask mt;
    String getLanguages = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=trnsl.1.1.20181024T031703Z.e633972b92e54262.1cca899a31214d245469e4b0cd4d2c29f6d6bde3";
    String getTranslate = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20181024T031703Z.e633972b92e54262.1cca899a31214d245469e4b0cd4d2c29f6d6bde3";
    String[] keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        translate = findViewById(R.id.button);
        clear = findViewById(R.id.button2);
        change = findViewById(R.id.button3);
        lang_in = findViewById(R.id.spinner1);
        lang_out = findViewById(R.id.spinner2);
        //определяю все языки в keys
        mt = new MyTask();
        mt.execute(getLanguages + "&ui=ru");
        try {
            keys = getListLanguages(mt.get());
            loadArrayToSpinner();
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
                translateText();
                break;
            case R.id.button2:
                editText1.setText("");
                editText2.setText("");
                break;
            case R.id.button3:
                int lang_in_pos = lang_in.getSelectedItemPosition();
                int lang_out_pos = lang_out.getSelectedItemPosition();
                lang_in.setSelection(lang_out_pos);
                lang_out.setSelection(lang_in_pos);
                break;
        }
    }

    public void translateText(){
        MyTask request = new MyTask();
        String lang1 = lang_in.getSelectedItem().toString().toLowerCase();
        String lang2 = lang_out.getSelectedItem().toString().toLowerCase();
        request.execute(getTranslate + "&text=" + editText1.getText() + "&lang=" + lang1 + "-" + lang2);
        String translation = "";
        try {
            translation = request.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        try {
            editText2.setText(parsingAnswerTranslation(translation));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String parsingAnswerTranslation(String translation) throws JSONException {
        JSONObject object = new JSONObject(translation);
        String s = object.get("text").toString();
        return s.substring(2,s.length()-2);
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<String, Void, String> {
        String result = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                result = getData(params[0]);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(String.valueOf(result));
            //editText1.setText(Arrays.toString(keys));

        }
    }

    public String getData(String urlStr){
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line=reader.readLine()) != null) {
                buf.append(line);
            }
            return buf.toString();
        } catch (Exception e){
            Log.d("error", e.toString());
        }
        return null;
    }

    public String[] getListLanguages(String s) throws JSONException {
        JSONObject object = new JSONObject(s);
        JSONObject langs = object.getJSONObject("langs");
        JSONArray arrayKeys = langs.names();
        String[] keys = new String[arrayKeys.length()];
        for(int i = 0; i < arrayKeys.length();i ++){
            keys[i] = arrayKeys.getString(i).toUpperCase();
        }
        return keys;
        //
    }

    public void loadArrayToSpinner(){
        //String[] str = {"EN", "RU", keys[0], keys[1], keys[2]};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keys);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        lang_in.setAdapter(adapter);
        lang_out.setAdapter(adapter);
    }

    //получение номера из контактов и отправка номера на звон
    /*
    public void mapClick(View v){
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri contactUri = data.getData();
        Cursor cursor =  getContentResolver().query(contactUri, null,
                null, null, null);
        cursor.moveToFirst();
        String columnName = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String number = cursor.getString(cursor.getColumnIndex(columnName));
        Intent calling = new Intent(Intent.ACTION_VIEW, numberUri);
        Uri numberUri = Uri.parse("tel: " + number);
        startActivity(calling);

    }
    */
}
