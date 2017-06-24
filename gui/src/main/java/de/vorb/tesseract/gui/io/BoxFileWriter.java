package de.vorb.tesseract.gui.io;

import de.vorb.tesseract.gui.model.BoxFileModel;
import de.vorb.tesseract.util.Box;
import de.vorb.tesseract.util.Symbol;
import de.vorb.util.FileNames;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BoxFileWriter {

    private BoxFileWriter() {}

    public static void writeBoxFile(BoxFileModel model) throws IOException {
        final BufferedWriter boxFileWriter = Files.newBufferedWriter(
                model.getFile(), StandardCharsets.UTF_8);

        final int pageHeight = model.getImage().getHeight();

        BoxToText(model, boxFileWriter, pageHeight);

        boxFileWriter.close();
    }

    public static void writeBoxFileForTraining(BoxFileModel model, Path path, String lan) throws IOException {

        String name = String.format("%s.%s", lan, FileNames.replaceExtension(model.getFile().getFileName(), "box") );
        final BufferedWriter boxFileWriter = Files.newBufferedWriter(
                path.resolve(name), StandardCharsets.UTF_8);

        final int pageHeight = model.getImage().getHeight();

       BoxToText(model, boxFileWriter, pageHeight);
        String nome = String.format("%s.%s", lan, FileNames.replaceExtension(model.getFile().getFileName(), "png") );
        Path output = path.resolve(nome);
        //Path output= FileNames.replaceExtension(outputfile, "png");
        ImageIO.write(model.getImage(), "png", new File(output.toString()));
        boxFileWriter.close();
    }

    public static void BoxToText(BoxFileModel model, BufferedWriter boxFileWriter, int pageHeight) throws IOException {
        for (Symbol symbol : model.getBoxes()) {

            final Box boundingBox = symbol.getBoundingBox();
            final int x0 = boundingBox.getX();
            final int y0 = pageHeight - boundingBox.getY() - boundingBox.getHeight();
            final int x1 = x0 + boundingBox.getWidth();
            final int y1 = y0 + boundingBox.getHeight();

            boxFileWriter.write(String.format("%s %d %d %d %d 0\n",
                    symbol.getText(), x0, y0, x1, y1));
        }

    }
}
