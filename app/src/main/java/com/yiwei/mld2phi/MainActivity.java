package com.yiwei.mld2phi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;


public  class MainActivity extends AppCompatActivity{
    Button mld, music, picture;
    final int MLD_REQUEST_MODE = 11451419;
    final int MUSIC_REQUEST_MODE = 99900011;
    final int PIC_REQUEST_MODE = 123123434;
    final int SAVE_TO_LOCAL = 6523458;

    String template;

    JSONObject mcjson;

    private int gen_count = 0;

    public static Config config = new Config();

    private void openfile(int code){
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

        if (data != null && resultCode == RESULT_OK){
            try {
                Uri uri = data.getData();
                String path = uri.getPath();
                String [] tmp = path.split(File.separator);

                if (requestCode == MLD_REQUEST_MODE) {

                    config.setMly_chart_path(path);
                    mld.setText(tmp[tmp.length - 1]);

                    InputStream is = get_stream(uri);
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    String content = new String(buffer, StandardCharsets.UTF_8);
                    mcjson = new JSONObject(content);
                    is.close();
                    Parse.ParseMC(config, mcjson);
                } else if (requestCode == PIC_REQUEST_MODE) {

                    if (config.bkg_stream != null) {
                        config.bkg_stream.close();
                    }

                    config.setBackground(path);
                    picture.setText(tmp[tmp.length - 1]);
                    config.bkg_stream = get_stream(uri);
                } else if (requestCode == MUSIC_REQUEST_MODE) {

                    if (config.song_stream != null) {
                        config.song_stream.close();
                    }

                    config.setSong(path);
                    music.setText(tmp[tmp.length - 1]);
                    config.song_stream = get_stream(uri);
                } else if (requestCode == SAVE_TO_LOCAL) {

                    JSONObject phi = Core.generate(template, config, mcjson);
                    byte[] zip = Core.createZipBytes(phi, config);

                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(zip);
                        outputStream.close();
                    }

                    toast("生成成功" + (++ gen_count == 1 ? "" : "x" + gen_count) );
                }

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
    protected void onDestroy() {
        super.onDestroy();

        if (config.song_stream != null) {
            try {
                config.song_stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (config.bkg_stream != null) {
            try {
                config.bkg_stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        mld.setOnClickListener(v -> openfile(MLD_REQUEST_MODE));
        picture.setOnClickListener(v -> openfile(PIC_REQUEST_MODE));
        music.setOnClickListener(v -> openfile(MUSIC_REQUEST_MODE));

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

    public String getTemplate() throws IOException {
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

    private void update_config(){
        TextView tv = findViewById(R.id.information);
        tv.setText("信息: \n" + config.toString());
    }
}