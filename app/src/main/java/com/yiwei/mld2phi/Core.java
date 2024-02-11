package com.yiwei.mld2phi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Core {

    private static float time_to_float(JSONArray ja) throws JSONException {
        return  Parse.time_to_float(Parse.json2float_arrray(ja));
    }

    private static int[] mine (int a, int x, int y){
        if (x == 0){
            return  new int[] {a - 1, 767, 768};
        }
        return new int [] {a, (int)Math.floor(x * 768.0 / y) - 1, 768};
    }

    private static JSONArray cvt2jsarr(int [] x){
        JSONArray ja = new JSONArray();
        for (int a : x){
            ja.put(a);
        }
        return ja;
    }
    private static JSONObject format_speed(float speed, JSONArray time) throws JSONException {
        time.put(0, time.getInt(0) + time.getInt(1) /  time.getInt(2));
        time.put(1, time.getInt(1) % time.getInt(2));
        int [] endtime = new int [] {0, 1, 768}, start_time = new int [] {0, 0, 1};
        if (time.getInt(0) != 0 || time.getInt(1) != 0){
            endtime = new int[] {time.getInt(0), time.getInt(1), time.getInt(2)};
            start_time =  mine(time.getInt(0), time.getInt(1), time.getInt(2));
        }
        JSONObject res = new JSONObject();
        res.put("end", speed);
        res.put("endTime", cvt2jsarr(endtime));
        res.put("linkgroup", 0);
        res.put("start", speed);
        res.put("startTime", cvt2jsarr(start_time));
        return res;
    }

    public static JSONObject generate(String template, Config config, JSONObject mcsjson) throws JSONException {
        JSONObject phijson = new JSONObject(template);
        if (config.getMly_chart_path().equals(""))
            throw new IllegalArgumentException("错误：没有选择谱面");
        if (config.getSong().equals("")){
            throw new IllegalArgumentException("错误：没有音乐");
        }
        if (config.getBackground().equals("")){
            throw new IllegalArgumentException("错误：没有背景图片");
        }
        JSONObject meta = phijson.getJSONObject("META");
        meta.put("background", Parse.get_file_name(config.getBackground()));
        meta.put("charter", config.getCharter());
        meta.put("composer", config.getComposer());
        meta.put("id", config.getMalodyid());
        meta.put("level", config.getLevel());
        meta.put("name", config.getName());
        meta.put("offset", config.getOffset());
        meta.put("song", Parse.get_file_name(config.getSong())); // TODO: 获取文件名

        JSONObject bpmlist = phijson.getJSONArray("BPMList").getJSONObject(0);
        bpmlist.put("bpm", config.getBpm().get(0).data);
        JSONArray starttime = new JSONArray();
        starttime.put(config.getBpm().get(0).time[0]);
        starttime.put(config.getBpm().get(0).time[1]);
        starttime.put(config.getBpm().get(0).time[2]);
        bpmlist.put("startTime", starttime);

        JSONObject line_event = phijson.getJSONArray("judgeLineList").getJSONObject(0).getJSONArray("eventLayers").getJSONObject(0);
        line_event.getJSONArray("alphaEvents").getJSONObject(0).put("end", config.getLine_alpha());
        line_event.getJSONArray("alphaEvents").getJSONObject(0).put("start", config.getLine_alpha());

        line_event.getJSONArray("moveXEvents").getJSONObject(0).put("end", config.getLine_x());
        line_event.getJSONArray("moveXEvents").getJSONObject(0).put("start", config.getLine_x());

        line_event.getJSONArray("moveYEvents").getJSONObject(0).put("end", config.getLine_y());
        line_event.getJSONArray("moveYEvents").getJSONObject(0).put("start", config.getLine_y());

        line_event.getJSONArray("rotateEvents").getJSONObject(0).put("end", config.getLine_rotate());
        line_event.getJSONArray("rotateEvents").getJSONObject(0).put("start", config.getLine_rotate());

        String speed_event_template = "{\"end\": " + config.getLine_speed() +  ",\n" +
                "\"endTime\": [ 0, 1, 1],\n" +
                "\"linkgroup\": 0,\n" +
                "\"start\": " + config.getLine_speed() + ",\n" +
                "\"startTime\": [0, 0, 1]}";
        line_event.getJSONArray("speedEvents").put(new JSONObject(speed_event_template));


        for(int i = 1; i < mcsjson.getJSONArray("time").length(); i++){
            String this_bpm = "{\"bpm\": " + config.getBpm().get(i).data + ", \"startTime\": [" + config.getBpm().get(i).time[0] + ", " + config.getBpm().get(i).time[1] + ", " + config.getBpm().get(i).time[2] + "}";
            phijson.getJSONArray("BPMList").put(new JSONObject(this_bpm));
        }

        JSONArray effect = mcsjson.getJSONArray("effect"), time = mcsjson.getJSONArray("time");
        if (effect.length() == 0 || Parse.time_to_float(Parse.json2float_arrray(time.getJSONObject(0).getJSONArray("beat"))) != 0.0){
            effect.put(0, new JSONObject("{\"beat\": [0, 0, 1], \"scroll\": 1.0}"));
        }
        List<JSONArray> t_bpm = new ArrayList<>(), t_scroll = new ArrayList<>();
        List<Float> v_bpm = new ArrayList<>(), v_scroll = new ArrayList<>();

        for(int i = 0; i < time.length(); i++){
            t_bpm.add(time.getJSONObject(i).getJSONArray("beat"));
            v_bpm.add((float)time.getJSONObject(i).getDouble("bpm"));
        }
        for(int i = 0; i < effect.length(); i++){
            if (effect.getJSONObject(i).has("scroll")){
                t_scroll.add(effect.getJSONObject(i).getJSONArray("beat"));
                v_scroll.add((float)effect.getJSONObject(i).getDouble("scroll"));
            }
        }
        int lim1 = 1, lim2 = 0;
        if (config.isSpeed_change_with_bpm()){
            lim1 = t_bpm.size();
        }
        if (config.isSpeed_change_with_effect()){
            lim2 = t_scroll.size();
        }
        int cur2 = 0;

        JSONArray speed_list = phijson.getJSONArray("judgeLineList")
                .getJSONObject(0)
                .getJSONArray("eventLayers")
                .getJSONObject(0)
                .getJSONArray("speedEvents");

        for(int cur1 = 1; cur1 < lim1; cur1++){
            while(cur2 < lim2 && time_to_float(t_bpm.get(cur1)) > time_to_float(t_scroll.get(cur2))){
                if (speed_list.length() != 0 && time_to_float(t_bpm.get(cur1 - 1)) == time_to_float(t_scroll.get(cur2))){
                    speed_list.remove(speed_list.length() - 1);
                }
                float new_speed = (float) config.getLine_speed() / config.getMainbpm()[0] * v_bpm.get(cur1 - 1) * v_scroll.get(cur2);
                JSONArray new_time = t_scroll.get(cur2);
                speed_list.put(format_speed(new_speed, new_time));
                cur2 += 1;
            }
            float new_speed = (float) config.getLine_speed() / config.getMainbpm()[0] * v_bpm.get(cur1) * v_scroll.get(cur2 - 1);
            JSONArray new_time = t_bpm.get(cur1);
            speed_list.put(format_speed(new_speed, new_time));
        }

        while (cur2 < lim2){
            if(speed_list.length() != 0 && time_to_float(t_bpm.get(lim1 - 1)) == time_to_float(t_scroll.get(cur2))){
                speed_list.remove(speed_list.length() - 1);
            }
            float new_speed = (float) config.getLine_speed() / config.getMainbpm()[0] * v_bpm.get(lim1 - 1) * v_scroll.get(cur2);
            JSONArray new_time = t_scroll.get(cur2);
            speed_list.put(format_speed(new_speed, new_time));
            cur2 += 1;
        }

        speed_list.getJSONObject(0)
                .getJSONArray("endTime")
                .put(2, 1);

        List<Float> mapping = new ArrayList<>();
        for (int i = 0; i < config.getColumn_number(); i++){
            mapping.add(
                    config.getColpos().column.get(config.getColumn_number() - 4).get(i)
            );
        }
        if (config.isNote_luck()){
            for(int i = 0; i < 100; i++){
                int col1 = (int) Math.floor(Math.random() * config.getColumn_number());
                int col2 = (int) Math.floor(Math.random() * config.getColumn_number());
                float tmp = mapping.get(col1);
                mapping.add(col1, mapping.get(col2));
                mapping.add(col2, tmp);
            }
        }
        if (config.isNote_flip()){
            for(int col1 = 0; col1 < config.getColumn_number() / 2; col1++){
                int col2 = config.getColumn_number() - col1 - 1;
                float tmp = mapping.get(col1);
                mapping.add(col1, mapping.get(col2));
                mapping.add(col2, tmp);
            }
        }

        for(int i =  0; i < config.getNote_number() + 1; i++){
            if(i == config.getSpecial_note())
                continue;
            JSONObject note = mcsjson.getJSONArray("note")
                    .getJSONObject(i);

            JSONArray ftime = note.getJSONArray("beat");
            JSONArray ftime_;
            int note_type = 2;
            if(note.has("endbeat")){
                ftime_ = note.getJSONArray("endbeat");
            }else {
                ftime_ = note.getJSONArray("beat");
                note_type = 1;
            }
            float position = mapping.get(note.getInt("column"));
            JSONObject this_note = new JSONObject();
            this_note.put("above", 1);
            this_note.put("alpha", 255);
            this_note.put("endTime", cvt2jsarr(new int[] {ftime_.getInt(0), ftime_.getInt(1), ftime_.getInt(2)}));
            this_note.put("isFake", 0);
            this_note.put("positionX", position);
            this_note.put("size", 1.0);
            this_note.put("speed", 1.0);
            this_note.put("startTime", cvt2jsarr(new int[] {ftime.getInt(0), ftime.getInt(1), ftime.getInt(2)}));
            this_note.put("type", note_type);
            this_note.put("visibleTime", 999999.0);
            this_note.put("yOffset", 0.0);
            phijson.getJSONArray("judgeLineList")
                    .getJSONObject(0)
                    .getJSONArray("notes")
                    .put(this_note);
        }
        phijson.getJSONArray("judgeLineList")
                .getJSONObject(0)
                .put("numOfNotes", config.getNote_number());
        return phijson;
    }

    /*
    json # 配置谱面
    csv: # 基本信息
    ogg: # 音乐
    jpg: # 背景
     */
    public static void save(String path, JSONObject phijson, Config config) throws IOException {

        String info = "Chart,Music,Image,Name,Artist,Level,Illustrator,Charter\n";
        StringJoiner params = new StringJoiner(",");
        params.add(Parse.get_file_name(config.getName() + ".json"));
        params.add(Parse.get_file_name(config.getSong()));
        params.add(Parse.get_file_name(config.getBackground()));
        params.add(config.getName());
        params.add(config.getComposer());
        params.add(config.getLevel());
        params.add(config.getIllustrator());
        params.add(config.getCharter());
        info += params.toString();

        ZipOutputStream phistream = new ZipOutputStream(Files.newOutputStream(Paths.get(path)));
        phistream.putNextEntry(new ZipEntry("info.csv"));
        phistream.write(info.getBytes(StandardCharsets.UTF_8));
        phistream.closeEntry();

        phistream.putNextEntry(new ZipEntry(config.getName() + ".json"));
        phistream.write(phijson.toString().getBytes(StandardCharsets.UTF_8));
        phistream.closeEntry();

        byte[] buf1 = new byte[config.bkg_stream.available()];
        config.bkg_stream.read(buf1);
        phistream.putNextEntry(new ZipEntry(Parse.get_file_name(config.getBackground())));
        phistream.write(buf1);
        phistream.closeEntry();
        config.bkg_stream.close();

        byte[] buf2 = new byte[config.song_stream.available()];
        config.song_stream.read(buf2);
        phistream.putNextEntry(new ZipEntry(Parse.get_file_name(config.getSong())));
        phistream.write(buf2);
        phistream.closeEntry();
        phistream.close();
        config.song_stream.close();
    }
}
