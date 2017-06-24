package de.vorb.tesseract.gui.model;

import de.vorb.tesseract.util.Symbol;
import de.vorb.tesseract.util.Box;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxFileModel {
    private final Path file;
    private final BufferedImage image;
    private final List<Symbol> boxes;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final List<Symbol> disabled;
    private final List<Symbol> killed;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public BoxFileModel(Path file,BufferedImage image, List<Symbol> boxes, List<Symbol> disabled) {
        this.file = file;
        this.image = image;
        this.boxes = boxes;
        this.disabled =disabled;
        this.killed = new ArrayList<Symbol>();
    }

    public Path getFile() {
        return file;
    }

    public BufferedImage getImage() {
        return image;
    }

    public List<Symbol> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

    public void insertBoxAt(int index, Symbol box) {
        boxes.add(index, box);
    }

    public void replaceBoxAt(int index, Symbol box) {
        boxes.set(index, box);
    }

    public void removeBoxAt(int index) {
        boxes.remove(index);
    }

    public void removeBox(Symbol box) {
        boxes.remove(box);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void filterByConfidence(float threshold)
    {
        List<Symbol> b= new ArrayList<Symbol>(this.boxes);
        List<Symbol> d= new ArrayList<Symbol>(this.disabled);

        for(Symbol s: b){
            if (s.getConfidence()<=threshold){
                boxes.remove(s);
                disabled.add(s);
            }}
        for(Symbol s: d){
            if (s.getConfidence()>=threshold) {
                disabled.remove(s);
                boxes.add(s);
            }
        }
    }

    public void deleteBox(Symbol s){

        if(boxes.contains(s))   {
            boxes.remove(s);
            killed.add(s);
        }
    }

    public void splitBox(Symbol s){
        if(boxes.contains(s)){
            Box b=s.getBoundingBox();
            Symbol s1= new Symbol("",new Box(b.getX(),b.getY(),b.getWidth()/2, b.getHeight()), s.getConfidence());
            Symbol s2= new Symbol("",new Box(b.getX()+ b.getWidth()/2,b.getY(),b.getWidth()/2, b.getHeight()), s.getConfidence());

            boxes.add(boxes.indexOf(s),s1);
            boxes.add(boxes.indexOf(s)+1,s2);
            boxes.remove(s);

            killed.add(s);


        }
    }

    public void mergeWithPrevious(Symbol s){
        if(boxes.contains(s)){
            if(boxes.indexOf(s)!=0){
                Symbol previous=boxes.get(boxes.indexOf(s)-1);

                killed.add(s);
                killed.add(previous);
                boxes.add(boxes.indexOf(s),new Symbol("", getMergedBox(s.getBoundingBox(),previous.getBoundingBox()),100));
                boxes.remove(s);
                boxes.remove(previous);
            }
        }
    }

    public void mergeWithNext(Symbol s){
        if(boxes.contains(s)){
            if(boxes.indexOf(s)!=(boxes.size()-1)){
                Symbol next=boxes.get(boxes.indexOf(s)+1);

                killed.add(s);
                killed.add(next);
                boxes.add(boxes.indexOf(s),new Symbol("", getMergedBox(s.getBoundingBox(),next.getBoundingBox()),100));
                boxes.remove(s);
                boxes.remove(next);
            }
        }
    }

    public void setBoxX(Symbol s, int i){
        if(boxes.contains(s)){
                Box b= s.getBoundingBox();
                Box b1= new Box(i, b.getY(), b.getWidth(), b.getHeight());
                Symbol s1 = new Symbol(s.getText(), b1, s.getConfidence());
                boxes.add(boxes.indexOf(s), s1);
                boxes.remove(s);
        }
    }

    public void setBoxY(Symbol s, int i){
        if(boxes.contains(s)){
            Box b= s.getBoundingBox();
            Box b1= new Box(b.getX(), i, b.getWidth(), b.getHeight());
            Symbol s1 = new Symbol(s.getText(), b1, s.getConfidence());
            boxes.add(boxes.indexOf(s),s1);
            boxes.remove(s);
        }
    }

    public void setBoxWidth(Symbol s, int i){
        if(boxes.contains(s)){
            Box b= s.getBoundingBox();
            Box b1= new Box(b.getX(), b.getY(), i, b.getHeight());
            Symbol s1 = new Symbol(s.getText(), b1, s.getConfidence());
            boxes.add(boxes.indexOf(s),s1);
            boxes.remove(s);
        }
    }
    public void setBoxHeight(Symbol s, int i){
        if(boxes.contains(s)){
            Box b= s.getBoundingBox();
            Box b1= new Box(b.getX(), b.getY(), b.getWidth(),i);
            Symbol s1 = new Symbol(s.getText(), b1, s.getConfidence());
            boxes.add(boxes.indexOf(s),s1);
            boxes.remove(s);
        }
    }

    public List<Symbol> getDisabled(){return this.disabled;}
    public List<Symbol> getKilled(){return this.killed;}

    public Box getMergedBox(Box b1, Box b2){
        int y = Math.min(b1.getY(), b2.getY());
        int x = Math.min(b1.getX(), b2.getX());

        return   new Box(x, y,
                Math.max(b1.getX()+b1.getWidth(),b2.getX()+b2.getWidth())-x,
                Math.max(b1.getY()+b1.getHeight(),b2.getY()+b2.getHeight())-y);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
