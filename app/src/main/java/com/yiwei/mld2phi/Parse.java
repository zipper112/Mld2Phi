package com.yiwei.mld2phi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Parse {


    public static float time_to_float(float[] x) {
        return x[0] + x[1] / x[2];
    }

    public static float[] json2float_arrray(JSONArray ja) throws JSONException {
        float[] res = new float[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            res[i] = (float) ja.getDouble(i);
        }
        return res;
    }

    public static void ParseMC(Config config, JSONObject mcjson) throws Exception {
        config.setColumn_number(mcjson.getJSONObject("meta")
                .getJSONObject("mode_ext")
                .getInt("column"));

        JSONArray ja = mcjson.getJSONArray("note");
        config.setNote_number(
                ja.length() - 1
        );
        for (int i = 0; i < ja.length(); i++) {
            if (ja.getJSONObject(i).has("sound")) {
                config.setSpecial_note(i);
            }
        }


        JSONObject meta = mcjson.getJSONObject("meta");
        config.setCharter(
                meta.getString("creator")
        );
        config.setLevel(
                meta.getString("version")
        );
        config.setIllustrator("");
        config.setName(
                meta.getJSONObject("song").getString("title")
        );
        config.setComposer(
                meta.getJSONObject("song").getString("artist")
        );


        JSONArray note = mcjson.getJSONArray("note");
        float[] endtime;
        if (note.getJSONObject(config.getNote_number() - 1).has("endbeat")) {
            endtime = json2float_arrray(
                    note.getJSONObject(config.getNote_number() - 1).getJSONArray("endbeat")
            );
        } else {
            endtime = json2float_arrray(note.getJSONObject(config.getNote_number() - 1).getJSONArray("beat")
            );
        }


        JSONArray time = mcjson.getJSONArray("time");
        int bpm_num = time.length();
        List<ClassBPM> cb = config.getBpm();
        for (int i = 0; i < bpm_num; i++) {
            ClassBPM tmp = new ClassBPM();
            tmp.data = (float) time.getJSONObject(i).getDouble("bpm");
            for (int j = 0; j < 3; j++) {
                tmp.time[j] = time.getJSONObject(i).getJSONArray("beat").getInt(j);
            }

            float during = 0f;
            if (i == bpm_num - 1)
                during = time_to_float(endtime) - time_to_float(json2float_arrray(time.getJSONObject(i).getJSONArray("beat")));
            else {
                during = time_to_float(
                        json2float_arrray(
                                time.getJSONObject(i + 1).getJSONArray("beat")
                        )
                ) - time_to_float(
                        json2float_arrray(
                                time.getJSONObject(i).getJSONArray("beat")
                        )
                );
            }

            if (during > config.getMainbpm()[1]) {
                config.setMainbpm(new float[]{tmp.data, during});
            }
            cb.add(tmp);
        }

        JSONObject spnote = note.getJSONObject(config.getSpecial_note());
        if (spnote.has("offset")) {
            config.setOffset((float) spnote.getDouble("offset"));
        } else {
            config.setOffset(0f);
        }
        config.setOffset(-config.getOffset());

        config.getColpos().column_builder(4, 260);
        config.getColpos().column_builder(5, 250);
        config.getColpos().column_builder(6, 220);
        config.getColpos().column_builder(7, 180);
        config.getColpos().column_builder(8, 160);
        config.getColpos().column_builder(9, 140);

    }
}
