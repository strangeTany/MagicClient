package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARAlphaVideoNode;
import eu.kudan.kudan.ARImageTrackable;
import eu.kudan.kudan.ARImageTrackableListener;
import eu.kudan.kudan.ARImageTracker;
import eu.kudan.kudan.ARModelNode;
import eu.kudan.kudan.ARVideoTexture;
import io.colyseus.Client;
import io.colyseus.Room;

public class GameActivity extends ARActivity implements ARImageTrackableListener {

    private List<String> currentCombo = new ArrayList<>();
    private List<String> usedCards = new ArrayList<>();
    private String[] colors;
    private List<Integer> userColors = new ArrayList<>();
    private JSONObject restriction;
    private boolean isGameStarted = false;
    private int userRounds = 0;
    private int opponentRounds = 0;
    private String UserId;
    private Room room;
    private LinearLayout restrictLayout;
    private ConstraintLayout gameLayout;
    private LinearLayout endLayout;
    private LinearLayout loading;
    private Button castSpell;
    private Client client;
    private Handler handler;
    private CountDownTimer timer;
    private boolean doneRestriction = false;
    private int rightCards = 0;

    public static int convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        colors = new String[]{getString(R.string.red), getString(R.string.yellow),
                getString(R.string.green), getString(R.string.blue), getString(R.string.violet)};

        if (!checkPermissions()) {
            backToMainActivity(getString(R.string.permission_error));
        }
        setContentView(R.layout.activity_game);
        TextView message = findViewById(R.id.message);
        gameLayout = findViewById(R.id.gameLayout);
        loading = findViewById(R.id.loading);
        restrictLayout = findViewById(R.id.announcementLayout);
        endLayout = findViewById(R.id.endLayout);
        //сообщения при загрузке
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
                TextView message = findViewById(R.id.message);
                message.setText(getString(msg.arg1));
            }
        };

        castSpell = findViewById(R.id.castSpell);
        castSpell.setOnClickListener(v -> {
            sendComboToServer();
            timer.cancel();
        });
        //подключение юзера
        String url = "http://192.168.43.63:3000";
        client = new Client(url, new Client.Listener() {
            @Override
            public void onOpen(String id) {
                UserId = id;
            }

            @Override
            public void onMessage(Object message) {
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("cli error", e.getMessage());
                e.printStackTrace();
            }
        });


    }

    private void startGame() {
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("username", MainActivity.user.getUserName());
        Log.e("room", "room");
        endLayout.setVisibility(View.GONE);
        room = client.join("battle", options);
        //подключение к комнате
        room.addListener(new Room.Listener() {
            @Override
            protected void onLeave() {
            }

            @Override
            protected void onError(Exception e) {
                Log.e("room err", e.getMessage());
//                backToMainActivity(getString(R.string.room_error));

                e.printStackTrace();
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onMessage(Object message) {
                runOnUiThread(() -> {
                    //установка ограничений
                    TextView announcement = findViewById(R.id.announcement);

                    try {
                        Log.e("mes", String.valueOf(message));

                        JSONObject obj = new JSONObject(message.toString());

                        switch (obj.getString("message")) {
                            case ("start"):
                                setPlayers(obj);
                                loading.setVisibility(View.GONE);
                                String restrObj = obj.getString("restriction");
                                restriction = new JSONObject(restrObj);
                                gameLayout.setVisibility(View.GONE);
                                switch (restriction.getInt("index")) {
                                    case 0:
                                        announcement.setText(getString(R.string.must_use) + restriction.getInt("count") + getString(R.string.protecting_cards));
                                        break;
                                    case 1:
                                        announcement.setText(getString(R.string.must_use) + restriction.getInt("count") + " " + colors[restriction.getInt("color")] + getString(R.string.cards));
                                        break;
                                    case 2:
                                        announcement.setText(getString(R.string.must_use_1) + colors[restriction.getInt("secondColor")] + getString(R.string.card_after) + colors[restriction.getInt(("firstColor"))] + getString(R.string.cards));
                                        break;
                                }
                                //это все эти слои с обЪявлениями приходят и уходят
                                restrictLayout.setVisibility(View.VISIBLE);
                                Handler handlerRestriction = new Handler();
                                handlerRestriction.postDelayed(() -> {
                                    restrictLayout.setVisibility(View.GONE);
                                    gameLayout.setVisibility(View.VISIBLE);
                                    isGameStarted = true;
                                    castSpell.setEnabled(true);
                                    startTimer();
                                }, 5000);


                                break;

                            case ("end"):
                                //конец игры приходит id победителя
                                Log.e("end", obj.toString());
                                loading.setVisibility(View.GONE);
                                String winnerId = obj.getString("winner");
                                TextView result = findViewById(R.id.result);
                                if (winnerId.equals("none") || !winnerId.equals(UserId)) {
                                    result.setText(R.string.you_lost);
                                } else {
                                    result.setText(R.string.you_won);
                                }
                                TextView won = findViewById(R.id.won);
                                TextView lost = findViewById(R.id.lost);
                                MainActivity.user.save();
                                endLayout.setVisibility(View.VISIBLE);
                                Button replay = findViewById(R.id.replay);
                                Button back = findViewById(R.id.back);
                                replay.setOnClickListener(v -> {
                                    startGame();
                                    Message message1 = new Message();
                                    message1.what = 1;
                                    message1.arg1 = R.string.finding_opponent;
                                    handler.sendMessage(message1);
                                });
                                back.setOnClickListener(v -> backToMainActivity(""));


                                break;
                        }
                    } catch (JSONException e) {
                        Log.e("ggg", "");
                        e.printStackTrace();
                    }
                });

            }

            @Override
            protected void onJoin() {
                //готовность к игре
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                data.put("message", "ready");
                room.send(data);
            }
        });

    }

    private void setPlayers(JSONObject obj) throws JSONException {
        //красиво делаем id игроков
        String playersString = obj.getString("players");
        JSONArray players = new JSONArray(playersString);
        JSONObject first = (JSONObject) players.get(0);
        JSONObject second = (JSONObject) players.get(1);
        String firstId = first.getString("id");
        String secondId = second.getString("id");
        TextView opponentName = findViewById(R.id.opponentName);
        TextView userName = findViewById(R.id.userName);
        if (!firstId.equals(UserId)) {
            opponentName.setText(first.getString("username"));
            userName.setText(second.getString("username"));
        } else {
            opponentName.setText(second.getString("username"));
            userName.setText(first.getString("username"));
        }

    }
    //что это было я беспонятия

//    private void changeState(int to, int fromHp, int fromProtect, int toHp, int toProtect, JSONObject spell) throws JSONException {
//        TextView fromHpView;
//        TextView fromProtectView;
//        TextView toHpView;
//        TextView toProtectView;
//        ImageView spellView;
//        TextView textSpell;
//
//        if (to == 1) {
//            toHpView = findViewById(R.id.textOpponentHP);
//            toProtectView = findViewById(R.id.textOpponentProtect);
//            fromHpView = findViewById(R.id.textUserHP);
//            fromProtectView = findViewById(R.id.textUserProtect);
//            spellView = findViewById(R.id.spellOnOpponent);
//            textSpell = findViewById(R.id.textSpellOnOp);
//        } else {
//            toHpView = findViewById(R.id.textUserHP);
//            toProtectView = findViewById(R.id.textUserProtect);
//            fromHpView = findViewById(R.id.textOpponentHP);
//            fromProtectView = findViewById(R.id.textOpponentProtect);
//            spellView = findViewById(R.id.spellOnUser);
//            textSpell = findViewById(R.id.textSpellOnUser);
//        }
//
//        int action = spell.getInt("action");
//        int affect = spell.getInt("affect");
//        Handler handler = new Handler();
//        switch (action) {
//            case 0:
//                textSpell.setText(String.valueOf(affect));
//                spellView.setVisibility(View.VISIBLE);
//                textSpell.setVisibility(View.VISIBLE);
//
//                handler.postDelayed(() -> {
//                    spellView.setVisibility(View.GONE);
//                    textSpell.setVisibility(View.GONE);
//                }, 500);
//                toProtectView.setText(String.valueOf(toProtect));
//                toHpView.setText(String.valueOf(toHp));
//                break;
//            case 1:
//                fromProtectView.setHeight(convertDpToPixel(65));
//                fromProtectView.setWidth(convertDpToPixel(65));
//                fromProtectView.setText(String.valueOf(fromProtect));
//                handler.postDelayed(() -> {
//                    fromProtectView.setHeight(convertDpToPixel(50));
//                    fromProtectView.setWidth(convertDpToPixel(50));
//                }, 500);
//                break;
//            case 2:
//                fromHpView.setHeight(convertDpToPixel(65));
//                fromHpView.setWidth(convertDpToPixel(65));
//                fromHpView.setText(String.valueOf(fromHp));
//                handler.postDelayed(() -> {
//                    fromHpView.setHeight(convertDpToPixel(50));
//                    fromHpView.setWidth(convertDpToPixel(50));
//                }, 500);
//                break;
//        }
//
//    }

    @Override
    public void setup() {
        super.setup();

        Thread thread = new Thread(() -> {
            Message message = new Message();
            message.what = 1;
            message.arg1 = R.string.generating_cards;
            handler.sendMessage(message);

            //получаем карты юзера
            List<UserCard> cards = UserCard.listAll(UserCard.class);

            List<String> targetImages = new ArrayList<>();
//        List<ARModelNode> models = new ArrayList<>();
            List<String> targetNames = new ArrayList<>();

            for (UserCard card : cards) {
                //делаем из карт маркеры
                Boolean cardStatus = card.getStatus();
                String cardName = card.getName();
                String targetFile = cardName + ".jpg";
//            String modelFile = cardName + ".armodel";
                Integer color = card.getColor();

                targetImages.add(targetFile);
                targetNames.add(cardStatus + "_" + cardName + "_" + color);
                userColors.add(color);
//            new Thread(() -> models.add(modelRendering(cardName)));
            }
//        while (models.size() != targetNames.size()) {
//
//        }
            List<ARImageTrackable> targets = createTargets(targetImages, targetNames);

//        addModelsToTrackables(targets, models);
            addImageToTrackables(targets);

            addTrackablesToManager(targets);
            startGame();
            Message message1 = new Message();
            message1.what = 1;
            message1.arg1 = R.string.finding_opponent;
            handler.sendMessage(message1);
        });
        thread.start();
//        isGameStarted = true;
    }

    private void backToMainActivity(String error) {
        if (!error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startTimer() {
        ProgressBar countdown = findViewById(R.id.timer);

        timer = new CountDownTimer(30000, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setProgress((int) (millisUntilFinished));
            }

            @Override
            public void onFinish() {
                if (isGameStarted) sendComboToServer();
                else Log.e("gg", "ne tut");
            }

        }.start();
    }

    private void sendComboToServer() {
        Log.e("gg", "tut");
        //правила рестрикшенов не помню но именно они тут проверяются
        try {
            if (restriction.getInt("index") == 2 && currentCombo.size() != 0) {
                UserCard lastCard = UserCard.find(UserCard.class, "name=?", currentCombo.get(currentCombo.size() - 1)).get(0);
                if (lastCard.getColor() != restriction.getInt("firstColor")) {
                    doneRestriction = true;
                }
            }
            if (!doneRestriction) {
                currentCombo = new ArrayList<>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        rightCards = 0;
        isGameStarted = false;
        doneRestriction = false;
        gameLayout.setVisibility(View.GONE);
        Message message = new Message();
        message.arg1 = R.string.fighting;
        message.what = 1;
        handler.sendMessage(message);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "spell");
        data.put("spell", currentCombo);
        room.send(data);
        currentCombo = new ArrayList<>();
        //это кажется для отображения выбранных карт
        ImageView[] cards = new ImageView[]{findViewById(R.id.first), findViewById(R.id.second), findViewById(R.id.third), findViewById(R.id.forth), findViewById(R.id.fith)};
        for (ImageView card : cards) {
            card.setImageResource(android.R.color.transparent);
        }
    }


//    private ARModelNode modelRendering(String name) {
//        ARModelImporter modelImporter = new ARModelImporter();
//        modelImporter.loadFromAsset("models/" + name + ".armodel");
//        ARModelNode modelNode = modelImporter.getNode();
//
//        List<ARMeshNode> meshNodes = modelImporter.getMeshNodes();
//
//        for (int i = 0; i < meshNodes.size(); i++) {
//
//            ARTexture2D texture = new ARTexture2D();
//            texture.loadFromAsset("textures/" + name + "_" + i + ".png");
//            ARLightMaterial lightMaterial = new ARLightMaterial();
//            lightMaterial.setTexture(texture);
//            meshNodes.get(i).setMaterial(lightMaterial);
//
//        }
//
//        return modelNode;
//    }

    //создание маркеров
    private List<ARImageTrackable> createTargets(List<String> targetFiles, List<String> targetNames) {
        List<ARImageTrackable> trackables = new ArrayList<>();

        for (int i = 0; i < targetFiles.size(); i++) {
            ARImageTrackable trackable = new ARImageTrackable(targetNames.get(i));
            trackable.loadFromAsset(targetFiles.get(i));
            trackable.addListener(this);
            trackables.add(trackable);
        }
        return trackables;
    }
    
    //а это кажется 3Д модельки котрых нет
    private void addModelsToTrackables(List<ARImageTrackable> trackables, List<ARModelNode> models) {

        for (int i = 0; i < trackables.size(); i++) {
            ARImageTrackable trackable = trackables.get(i);
            ARModelNode model = models.get(i);
            trackable.getWorld().addChild(model);
        }

    }

    //прикрепление картинок к маркерам
    private void addImageToTrackables(List<ARImageTrackable> trackables) throws IllegalStateException {
        String[] effects = new String[]{"fire", "air", "flower", "water"};
        int count = 0;
        for (int i = 0; i < trackables.size(); i++) {

            ARImageTrackable trackable = trackables.get(i);
            int color = Integer.parseInt(trackable.getName().split("_")[2]);
            runOnUiThread(() -> {
                ARVideoTexture texture = new ARVideoTexture();
                texture.loadFromAsset(effects[color] + ".mp4");
                Log.e("color", String.valueOf(color));
                ARAlphaVideoNode effect = new ARAlphaVideoNode(texture);
                trackable.getWorld().addChild(effect);

            });


        }

    }
    
    //это кудановское чтобы оно работало в принципе
    private void addTrackablesToManager(List<ARImageTrackable> trackables) {
        ARImageTracker tracker = ARImageTracker.getInstance();
        tracker.initialise();
        for (ARImageTrackable trackable : trackables) {
            tracker.addTrackable(trackable);
        }
    }
 
    //тут адовая дичь с тем, что когда карта детектится она проверяется на все ограничения и добавляется в комбо
    @Override
    public void didDetect(ARImageTrackable arImageTrackable) {
        String cardStatus = arImageTrackable.getName().split("_")[0];
        String cardName = arImageTrackable.getName().split("_")[1];
        int cardColor = Integer.parseInt(arImageTrackable.getName().split("_")[2]);
        arImageTrackable.getWorld().getChildren().get(0).setVisible(false);
        
        if (isGameStarted) {
            try {
                if (!doneRestriction) {

                    boolean condition;
                    switch (restriction.getInt("index")) {
                        case 0:
                            condition = cardStatus.equals("false");
                            checkRestriction(arImageTrackable, cardName, condition);
                            Log.e("add1", cardName);

                            break;
                        case 1:
                            condition = cardColor == restriction.getInt("color");
                            checkRestriction(arImageTrackable, cardName, condition);
                            Log.e("add2", cardName);

                            break;
                        case 2:
                            if (currentCombo.size() != 0) {
                                UserCard lastCard = UserCard.find(UserCard.class, "name=?", currentCombo.get(currentCombo.size() - 1)).get(0);
                                if (lastCard.getColor() == restriction.getInt("firstColor")) {
                                    if (cardColor == restriction.getInt("secondColor") && currentCombo.indexOf(cardName) == -1) {
                                        arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                                        currentCombo.add(cardName);
                                        addImage(cardName);
                                        Log.e("add3", cardName);


                                    }
                                } else if(currentCombo.indexOf(cardName) == -1){
                                    arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                                    currentCombo.add(cardName);
                                    addImage(cardName);
                                    Log.e("add4", cardName);

                                }
                            } else {
                                arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                                currentCombo.add(cardName);
                                addImage(cardName);
                                Log.e("add5", cardName);

                            }

                    }

                } else if (currentCombo.indexOf(cardName) == -1 && restriction.getInt("index") == 0 && !cardStatus.equals("false") ){
                    arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                    Log.e("add6", cardName);

                    currentCombo.add(cardName);
                    addImage(cardName);
                } else if(currentCombo.indexOf(cardName) == -1 && restriction.getInt("index") == 1 && cardColor != restriction.getInt("color")){
                    Log.e("add7", cardName);
                    arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                    currentCombo.add(cardName);
                    addImage(cardName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (currentCombo.size() == 5) {
                sendComboToServer();
                timer.cancel();
            }
        }
    }
    //подгрузка изображений из ассетов
    private void addImage(String name) {
        ImageView[] cards = new ImageView[]{findViewById(R.id.first), findViewById(R.id.second), findViewById(R.id.third), findViewById(R.id.forth), findViewById(R.id.fith)};
        try {
            // get input stream
            InputStream ims = getAssets().open(name + ".jpg");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            ImageView image = cards[currentCombo.size() - 1];
            image.setImageDrawable(d);
            ims.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //очередная проверка на рестрикшены(точнее функция которая юзается при проверке)
    private void checkRestriction(ARImageTrackable arImageTrackable, String cardName, boolean condition) throws JSONException {
        if (restriction.getInt("count") - rightCards == 5 - currentCombo.size()) {
            if (condition &&
                    currentCombo.indexOf(cardName) == -1) {
                arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
                currentCombo.add(cardName);
                addImage(cardName);

                rightCards++;
            }
        } else if (condition && currentCombo.indexOf(cardName) == -1) {
            arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
            currentCombo.add(cardName);
            addImage(cardName);

            rightCards++;
        } else if (currentCombo.indexOf(cardName) == -1) {
            arImageTrackable.getWorld().getChildren().get(0).setVisible(true);
            currentCombo.add(cardName);
            addImage(cardName);

        }
        if (rightCards == restriction.getInt("count")) {
            doneRestriction = true;
        }
    }

    @Override
    public void didTrack(ARImageTrackable arImageTrackable) {


    }

    @Override
    public void didLose(ARImageTrackable arImageTrackable) {
        Log.e("loose", arImageTrackable.getName());
        arImageTrackable.getWorld().getChildren().get(0).setVisible(false);

    }

    private boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

//    private List<Integer> arrayToList(JSONArray array) {
//        List<Integer> list = new ArrayList<>();
//        for (int i = 0; i < array.length(); i++) {
//            try {
//                list.add((Integer) array.get(i));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return list;
//    }
}
