package org.o7planning.sqlietereal;

//Klasse für ein Produkt
public class ExampleItem {
    private int mImageResource;
    private String mText1;
    private String mText2;
    private int mColor;

    public ExampleItem(int imageResource, String text1, String text2, int color){
        mImageResource = imageResource;
        mText1 = text1;
        mText2 = text2;
        mColor = color;
    }

    public void changeText1(String text){
        mText1 = text;
    }

    public int getImageResource(){
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }

    public Integer getColor() { return mColor; }

}
