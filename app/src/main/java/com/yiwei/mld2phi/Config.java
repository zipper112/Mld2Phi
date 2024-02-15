package com.yiwei.mld2phi;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class ClassBPM{
    float data = 120f;
    int [] time = new int[] {0, 0, 1};

    @Override
    public String toString() {
        return "ClassBPM{" +
                "data=" + data +
                ", time=" + Arrays.toString(time) +
                '}';
    }
}

class ColumnPOS{
    public ArrayList<ArrayList<Float>> column;
    public ColumnPOS(){
        column = new ArrayList<>();
    }
    public void column_builder(int clm, int div){
        ArrayList<Float> ret = new ArrayList<>();
        for(int i = 0; i < clm; i++){
            ret.add((float)(1 - clm) * div / 2 + i * div);
        }
        column.add(ret);
    }
}

public class Config {


    private ColumnPOS colpos = new ColumnPOS();
    private Map<String, Object> mld_j;
    private String name = "NameNotFound";
    private List<ClassBPM> bpm = new ArrayList<>();
    private float [] mainbpm = new float[]{0f, 0f};
    private int offset = 0;
    private int note_number = 0;
    private int special_note = 0;
    private int malodyid = 0;
    private int column_number = 0;
    private String charter = "";
    private String composer = "";
    private String level = "";
    private String background = "";
    private String backgrounddir = "";
    private String song = "";
    private String songdir = "";
    private float line_y = -350.0f;
    private float line_speed = 13.0f;
    private boolean speed_change_with_bpm = true;
    private boolean speed_change_with_effect = true;
    private int line_alpha = 255;
    private float line_x = 0.0f;
    private float line_rotate = 0.0f;
    private String illustrator = "";
    private boolean note_flip = false;
    private boolean note_luck = false;
    private boolean note_cst = false;

    private String mly_chart_path = "";

    public InputStream mld_char_stream, bkg_stream, song_stream;


    @Override
    public String toString() {
        return
                "\n轨道个数: " + column_number +
                "\n音符总个数: " + note_number +
                "\n谱师: " + charter +
                "\n等级提示: " + level +
                "\n曲绘作者: " + illustrator +
                "\n谱面名称: " + name +
                "\n曲师" + composer +
                "\nBPM: " + mainbpm[0] +
                "\n偏移值: " + offset +
                "\n基础流速: " + line_speed +
                "\n线高度: " + line_y +
                "\nLUCK: " + (note_luck ? "ON": "OFF") +
                "\nFLIP: " + (note_flip ? "ON" : "OFF") +
                "\nCONST: " + (note_cst ? "ON" : "OFF") +
                "\n线透明度" + line_alpha +
                "\n横向偏移" + line_x +
                "\n旋转角度" + line_rotate;
    }

    public String getMly_chart_path() {
        return mly_chart_path;
    }

    public void setMly_chart_path(String mly_chart_path) {
        this.mly_chart_path = mly_chart_path;
    }
    public Map<String, Object> getMld_j() {
        return mld_j;
    }

    public void setMld_j(Map<String, Object> mld_j) {
        this.mld_j = mld_j;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNote_cst() {
        return note_cst;
    }

    public void setNote_cst(boolean note_cst) {
        this.note_cst = note_cst;
    }

    public List<ClassBPM> getBpm() {
        return bpm;
    }

    public ColumnPOS getColpos() {
        return colpos;
    }

    public float[] getMainbpm() {
        return mainbpm;
    }

    public void setMainbpm(float[] mainbpm) {
        this.mainbpm = mainbpm;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getNote_number() {
        return note_number;
    }

    public void setNote_number(int note_number) {
        this.note_number = note_number;
    }

    public int getSpecial_note() {
        return special_note;
    }

    public void setSpecial_note(int special_note) {
        this.special_note = special_note;
    }

    public int getMalodyid() {
        return malodyid;
    }

    public void setMalodyid(int malodyid) {
        this.malodyid = malodyid;
    }

    public int getColumn_number() {
        return column_number;
    }

    public void setColumn_number(int column_number) {
        this.column_number = column_number;
    }

    public String getCharter() {
        return charter;
    }

    public void setCharter(String charter) {
        this.charter = charter;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBackgrounddir() {
        return backgrounddir;
    }

    public void setBackgrounddir(String backgrounddir) {
        this.backgrounddir = backgrounddir;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSongdir() {
        return songdir;
    }

    public void setSongdir(String songdir) {
        this.songdir = songdir;
    }

    public float getLine_y() {
        return line_y;
    }

    public void setLine_y(float line_y) {
        this.line_y = line_y;
    }

    public float getLine_speed() {
        return line_speed;
    }

    public void setLine_speed(float line_speed) {
        this.line_speed = line_speed;
    }

    public boolean isSpeed_change_with_bpm() {
        return speed_change_with_bpm;
    }

    public void setSpeed_change_with_bpm(boolean speed_change_with_bpm) {
        this.speed_change_with_bpm = speed_change_with_bpm;
    }

    public boolean isSpeed_change_with_effect() {
        return speed_change_with_effect;
    }

    public void setSpeed_change_with_effect(boolean speed_change_with_effect) {
        this.speed_change_with_effect = speed_change_with_effect;
    }

    public int getLine_alpha() {
        return line_alpha;
    }

    public void setLine_alpha(int line_alpha) {
        this.line_alpha = line_alpha;
    }

    public float getLine_x() {
        return line_x;
    }

    public void setLine_x(float line_x) {
        this.line_x = line_x;
    }

    public float getLine_rotate() {
        return line_rotate;
    }

    public void setLine_rotate(float line_rotate) {
        this.line_rotate = line_rotate;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public boolean isNote_flip() {
        return note_flip;
    }

    public void setNote_flip(boolean note_flip) {
        this.note_flip = note_flip;
    }

    public boolean isNote_luck() {
        return note_luck;
    }

    public void setNote_luck(boolean note_luck) {
        this.note_luck = note_luck;
    }

    public boolean[] getLock() {
        return lock;
    }

    public void setLock(boolean[] lock) {
        this.lock = lock;
    }

    public String getTmp_folder() {
        return tmp_folder;
    }

    public void setTmp_folder(String tmp_folder) {
        this.tmp_folder = tmp_folder;
    }

    private boolean [] lock = new boolean[]{false, false, false, false, false, false, false, false, false, false};
    private String tmp_folder = "temp";


}
