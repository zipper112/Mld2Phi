package com.yiwei.mld2phi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Consumer;

import kotlin.Pair;


public  class MainActivity extends AppCompatActivity{
    private static final int START_GEN = 265841510;
    private final int MLD_REQUEST_MODE = 11451419;
    private final int MUSIC_REQUEST_MODE = 99900011;
    private final int PIC_REQUEST_MODE = 123123434;
    private final int SAVE_TO_LOCAL = 6523458;
    private final int SUCCESS_GEN = 2565123;
    private final int FAILURE_GEN = 2565124;
    private Button mld, music, picture;
    private String template;
    private JSONObject mcjson;
    private int gen_count = 0;
    public static Config config = new Config();
    private AlertDialog progressDialog;

    private final Handler genTask = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message message) {


            if (message.what == START_GEN) {

                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                    Pair<ProgressBar, TextView> pair = getViewFromDialog();
                    ProgressBar bar = pair.getFirst();
                    TextView content = pair.getSecond();
                    content.setVisibility(View.GONE);
                    bar.setVisibility(View.VISIBLE);
                }

            } else if (message.what == SUCCESS_GEN) {
                if (progressDialog.isShowing()) {
                    Pair<ProgressBar, TextView> pair = getViewFromDialog();
                    ProgressBar bar = pair.getFirst();
                    TextView content = pair.getSecond();

                    bar.setVisibility(View.GONE);
                    content.setText("转换成功");
                    content.setVisibility(View.VISIBLE);
                } else {
                    toast("转换成功" + (++ gen_count == 1 ? "" : "x" + gen_count) );
                }
            } else if (message.what == FAILURE_GEN) {
                if (progressDialog.isShowing()) {
                    Pair<ProgressBar, TextView> pair = getViewFromDialog();
                    ProgressBar bar = pair.getFirst();
                    TextView content = pair.getSecond();

                    bar.setVisibility(View.GONE);
                    content.setText("转换失败, 原因: " + message.obj);
                    content.setVisibility(View.VISIBLE);
                } else {
                    toast("转换失败, 原因: " + message.obj );
                }
            }
        }
    };

    private Pair<ProgressBar, TextView> getViewFromDialog() {
        ProgressBar bar = progressDialog.findViewById(R.id.prog_bar);
        TextView content = progressDialog.findViewById(R.id.prog_msg);
        assert bar != null;
        assert content != null;

        return new Pair<>(bar, content);
    }

    private void pickFile(int code){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, code);
    }

    private InputStream get_stream(Uri path) throws FileNotFoundException {
        return getContentResolver().openInputStream(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null && resultCode == RESULT_OK){
            try {
                Uri uri = data.getData();
                DocumentFile df = DocumentFile.fromSingleUri(this, uri);
                String filename = df.getName();

                if (filename == null || filename.equals("")) {
                    filename = Paths.get(uri.getPath()).getFileName().toString();
                }

                if (requestCode == MLD_REQUEST_MODE) {

                    config.setMly_chart_path(filename);
                    mld.setText(filename);

                    InputStream is = get_stream(uri);
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    String content = new String(buffer, StandardCharsets.UTF_8);
                    mcjson = new JSONObject(content);
                    is.close();
                    Parse.ParseMC(config, mcjson);
                } else if (requestCode == PIC_REQUEST_MODE) {

                    String lfn = filename.toLowerCase();
                    if (!lfn.endsWith(".jpg") && !lfn.endsWith(".png") && !lfn.endsWith(".jpeg") && !lfn.endsWith(".bmp"))
                        filename += ".jpg";

                    config.setBackground(filename);
                    picture.setText(filename);
                    config.setBkg_uri(uri);

                } else if (requestCode == MUSIC_REQUEST_MODE) {

                    String lfn = filename.toLowerCase();
                    if (!lfn.endsWith(".ogg") && !lfn.endsWith(".m4a") && !lfn.endsWith(".mp3") && !lfn.endsWith(".wav") && !lfn.endsWith(".flac") && !lfn.endsWith(".amr") && !lfn.endsWith(".acc"))
                        filename += ".ogg";

                    config.setSong(filename);
                    music.setText(filename);
                    config.setSong_uri(uri);

                } else if (requestCode == SAVE_TO_LOCAL) {
                    new Thread(() -> {
                        genTask.sendMessage(genTask.obtainMessage(START_GEN));
                        try {
                            JSONObject phi = Core.generate(template, config, mcjson);
                            byte[] zip = Core.createZipBytes(this, phi, config);

                            OutputStream outputStream = getContentResolver().openOutputStream(uri);
                            if (outputStream != null) {
                                outputStream.write(zip);
                                outputStream.close();
                            }

                            Message message = genTask.obtainMessage();
                            message.what = SUCCESS_GEN;
                            genTask.sendMessage(message);

                        } catch (Exception e) {
                            Message message = genTask.obtainMessage();
                            message.what = FAILURE_GEN;
                            message.obj = e;
                            genTask.sendMessage(message);
                        }
                    }).start();
                }
            } catch (JSONException e) {
                errAlert("选择的文件不是标准谱面, 或者解析出错, 详细信息: " + e);
            } catch (Exception e){
                errAlert(e.toString());
            }
            update_config();
        }
    }


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void errAlert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("错误提示")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .create().show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new AlertDialog.Builder(this)
                .setTitle("处理信息")
                .setPositiveButton("杂鱼", null)
                .setView(R.layout.progess_dialog_view)
                .create();

        try {
            template = getTemplate();
            bind();
        } catch (IOException e) {
            errAlert(e.toString());
        }
    }

    private void bind() throws IOException {
        addTextChangeListener(R.id.level_hint, s -> config.setLevel(s.toString()));
        addTextChangeListener(R.id.ill_author, s -> config.setIllustrator(s.toString()));
        addTextChangeListener(R.id.basic_speed, s -> config.setLine_speed(Float.parseFloat(s.toString())));
        addTextChangeListener(R.id.line_height, s -> config.setLine_y(Float.parseFloat(s.toString())));
        addTextChangeListener(R.id.line_alpha, s -> config.setLine_alpha(Integer.parseInt(s.toString())));
        addTextChangeListener(R.id.x_shift, s -> config.setOffset(Float.parseFloat(s.toString())));
        addTextChangeListener(R.id.rotation_angle, s -> config.setLine_rotate(Float.parseFloat(s.toString())));

        CheckBox cst = findViewById(R.id.cst);
        CheckBox flip = findViewById(R.id.flip);
        CheckBox luck = findViewById(R.id.luck);
        addCheckBoxListener(cst, ic -> {
            config.setSpeed_change_with_bpm(ic);
            config.setSpeed_change_with_effect(ic);
            config.setNote_cst(ic);
        });
        addCheckBoxListener(flip, ic -> {
            config.setNote_luck(false);
            luck.setChecked(false);
            config.setNote_flip(ic);
        });
        addCheckBoxListener(luck, ic -> {
            config.setNote_flip(false);
            flip.setChecked(false);
            config.setNote_luck(ic);
        });

        mld = findViewById(R.id.mly_bt);
        picture = findViewById(R.id.picture_bt);
        music = findViewById(R.id.music_bt);
        mld.setOnClickListener(v -> pickFile(MLD_REQUEST_MODE));
        picture.setOnClickListener(v -> pickFile(PIC_REQUEST_MODE));
        music.setOnClickListener(v -> pickFile(MUSIC_REQUEST_MODE));

        Button start_gen = findViewById(R.id.start_gen);
        start_gen.setOnClickListener(v -> {
            if (config.getMly_chart_path().equals("")) {
                errAlert("杂鱼! 没有选择谱面");
            } else if (config.getSong().equals("")) {
                errAlert("杂鱼! 没有选择音乐");
            } else if (config.getBackground().equals("")) {
                errAlert("杂鱼! 没有选择背景");
            } else {
                createFile(config.getName() + ".zip");
            }
        });
    }

    public void createFile(String filename) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        startActivityForResult(intent, SAVE_TO_LOCAL);
    }

    private String getTemplate() throws IOException {
        AssetManager am = this.getAssets();
        InputStream is = am.open("template.json");
        byte [] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private void addCheckBoxListener(CheckBox cb, Consumer<Boolean> c){
        cb.setOnCheckedChangeListener((bv, ic) -> {
            try {
                c.accept(ic);
            } catch (Exception e) {
                errAlert(e.toString());
            }
            update_config();
        });
    }

    private void addTextChangeListener(int id, Consumer<Editable> cons){
        ((EditText)findViewById(id)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    cons.accept(s);
                } catch (Exception e) {
                    errAlert(e.toString());
                }

                update_config();
            }
        });
    }

    private void update_config() {
        TextView tv = findViewById(R.id.information);
        tv.setText("Original: IambicCave, ported by Zipper112(⑨) & whiterasbk\n\n" + "信息: \n" + config.toString());
    }
}