package de.vorb.tesseract.gui.work;

import de.vorb.tesseract.gui.view.dialogs.Dialogs;
import de.vorb.tesseract.util.Symbol;
import de.vorb.tesseract.util.xml.BoxAdapter;
import de.vorb.tesseract.util.Box;

import de.vorb.tesseract.gui.model.BoxFileModel;
import de.vorb.tesseract.gui.model.PageModel;
import de.vorb.tesseract.gui.view.BoxEditor;
import de.vorb.tesseract.gui.controller.TesseractController;
import de.vorb.tesseract.util.Block;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import java.nio.file.Path;
/**
 * Created by federica on 31/05/17.
 */
public class BoxFilterWorker {

    private BoxEditor boxEditor;
    private BoxFileModel boxFile;
    private TesseractController controller;

    public BoxFilterWorker(TesseractController controller, BoxFileModel boxFile, BoxEditor boxEditor) {
        this.controller = controller;
        this.boxFile = boxFile;
        this.boxEditor = boxEditor;
    }

    //metodo che permette di filtrare i box per confidence minima
    public void doInBackgroundMinCon() {
        Integer threshold = (Integer) boxEditor.getFilterSpinner().getValue();

        boxFile.filterByConfidence(threshold);
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);

        Optional<PageModel> pm = boxEditor.getPageModel();
        PageModel pageModel = pm.get();
        List<Block> blockList = pageModel.getPage().getBlocks();
        List<Rectangle> rectPage = new ArrayList<Rectangle>();
        List<Rectangle> rectBoxFile = new ArrayList<Rectangle>();
        for (Block b : blockList) {
            rectPage.add(b.getBoundingBox().toRectangle());
        }
        for (Symbol s : boxFile.getBoxes()) {
            Box b = s.getBoundingBox();
            rectBoxFile.add(new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight()));
        }
        for (Rectangle r : rectPage) {
            if (!rectBoxFile.contains(r)) {
                Box toRemove = new Box((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
                List<Block> lb = pageModel.getPage().getBlocks();

                for (Block block : lb) {
                    if (block.equals(toRemove)) {
                        lb.remove(block);
                    }
                }
            }
        }
    }

    //metodo che permette di eliminare il box selezionato dal boxeditor
    public void doInBackgroundDelSelectedBox(){
        Optional<Symbol> b= boxEditor.getSelectedSymbol();
        if(!b.isPresent()){
            Dialogs.showWarning(controller.getView(), "No box selection",
                    "No box has been selected. You need to select a box first.");
            return;
        }
        boxFile.deleteBox(b.get());
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }
    public void doInBackgroundSetX(){
        boxFile.setBoxX(boxEditor.getSelectedSymbol().get(), (Integer)(boxEditor.getXSpinner().getValue()));
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundSetY(){
        boxFile.setBoxY(boxEditor.getSelectedSymbol().get(), (Integer)(boxEditor.getYSpinner().getValue()));
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundSetWidth(){
        boxFile.setBoxWidth(boxEditor.getSelectedSymbol().get(), (Integer)(boxEditor.getWidthSpinner().getValue()));
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundSetHeight(){
        boxFile.setBoxHeight(boxEditor.getSelectedSymbol().get(), (Integer)(boxEditor.getHeightSpinner().getValue()));
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundSplit(){
        boxFile.splitBox(boxEditor.getSelectedSymbol().get());
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundMergeWithPrevious(){
        boxFile.mergeWithPrevious(boxEditor.getSelectedSymbol().get());
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }

    public void doInBackgroundMergeWithNext(){
        boxFile.mergeWithNext(boxEditor.getSelectedSymbol().get());
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }
    public void doInBackgroundDelSingleSymbol(Symbol symbol){
        boxFile.deleteBox(symbol);
        Optional<BoxFileModel> obf = Optional.of(boxFile);
        controller.setBoxFileModel(obf);
    }
}
