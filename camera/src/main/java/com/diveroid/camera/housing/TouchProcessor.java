package com.diveroid.camera.housing;

/**
 * 김윤태님과 동시 작업을 하기 위해서 만들었던 임시 터치 인터페이스
 * @deprecated
 */
public interface TouchProcessor {
    void button1Down(float x, float y);

    void button2Down(float x, float y);

    void button3Down(float x, float y);

    void button1Up(float x, float y);

    void button2Up(float x, float y);

    void button3Up(float x, float y);

    void button1Click(float x, float y);

    void button2Click(float x, float y);

    void button3Click(float x, float y);

    void button1LongClick(float x, float y);

    void button2LongClick(float x, float y);

    void button3LongClick(float x, float y);

    void button12LongClick(float x, float y);
    void button23LongClick(float x, float y);
    void button13LongClick(float x, float y);
    void button123LongClick(float x, float y);
    void button12Down(float x, float y);

    void button23Down(float x, float y);

    void button13Down(float x, float y);

    void button123Down(float x, float y);
}
