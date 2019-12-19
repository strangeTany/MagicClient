package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.orm.SugarContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import eu.kudan.kudan.ARAPIKey;

public class MainActivity extends AppCompatActivity {

    static User user;
    LinkedHashMap<String, Object> options = new LinkedHashMap<>();
    String url = "http://192.168.43.63:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        SugarContext.init(this);
        if (!checkPermissions())
            requestPermissions();
        //TODO: Перенести в логин или создать одного пользователя в бд и снова закомментить
//
        try {
            user = User.listAll(User.class).get(0);
        } catch (NullPointerException e) {
            // TODO: Переход на логин
            User NewUser = new User("5c9e0d64e6794d2f883df2ca", "user1", "user@mail.ru", 0, 0);
            NewUser.save();
        } catch (IndexOutOfBoundsException e){
            User NewUser = new User("5c9e0d64e6794d2f883df2ca", "user1", "user@mail.ru", 0, 0);
            NewUser.save();
        }
        RequestQueue MyRequestQueue;
        MyRequestQueue = Volley.newRequestQueue(MainActivity.this);
         StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url + "/game/cards?token=" + user.getToken(), res -> {
            try {
                JSONObject obj = new JSONObject(res);
                JSONObject userObj = obj.getJSONObject("user");
                user.save();
                JSONArray array = obj.getJSONArray("cards");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject card = (JSONObject) array.get(i);
                    if (UserCard.find(UserCard.class, "card_id = ?", String.valueOf(card.getDouble("_id"))).size() == 0) {
                        UserCard newCard = new UserCard(String.valueOf(card.getDouble("_id")), card.getString("name"), card.getInt("color"), card.getBoolean("status"));
                        newCard.save();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("er", String.valueOf(error)));
        MyRequestQueue.add(MyStringRequest);

        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey(
                "orYTa/wJ2uf2beNnqnlp8ZKkLXVyeTeJB5HXdmrs/4wJezs96yn/VwSGw7/5Odre/MD4OFaiWn31oizD1xfg3K2+PO5zcsH/eF2qnqAts43BJZraRBt5RTOhX1E205T7T9uAbe8vAwpi1HqiaDYOdBAw" +
                        "PFREVEY3AibdNKvBCicli/sqIf4WjYI7X/PoPj6+eiOBh9b/ZUrrjQ4k+2pKptY0wP7IXEcOwNRzhRbhWJvprB42Ja0Zi2ytut5+ZIFwXWY1ClM8+WQgaJ/j9oAdEFlZwj0gKChXZIflAkTu" +
                        "XrqQxvxBJIvkCHyjC8L7kKFTra5njcO+1qzh1uDZnZHn1VK6Typ/zowC7rruXrrVR+8nskgfdQPQfp8htdCSfGODkuGyQNgLCpVVOPRt6yhamMT0RQrxOcB1ifbaMNbPHbHNU4NL9tFfAmB0" +
                        "oneo+3xdb5APtT3qUyS2hwbWFWlXaYUnHh9X69gpEVckKwf15zG8/ZtWozUzS8gozLyX6HbyfkHyj1jljOM8qb741xdPiI/OtwjxkYeczzqkpwcSc0CMV3GXu/KgyaVvr0JuWtSSIVMPl71W" +
                        "/Dz97bmsKPd+bp3pc+1+S7QQGAtPGRCjqg79lr5LlbJnSCMtT/f03FMG8URw6xGfVmBlylUdAtYJc7LlpRES5rEF+2/Z68rmdt8=");

        button.setOnClickListener((v) -> {

            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            this.startActivity(intent);
        });
    }

    private void requestPermissions() {
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

}
