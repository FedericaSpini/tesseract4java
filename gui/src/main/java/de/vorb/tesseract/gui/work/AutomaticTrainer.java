package de.vorb.tesseract.gui.work;

import de.vorb.tesseract.gui.controller.TesseractTrainer;
import de.vorb.tesseract.gui.view.dialogs.Dialogs;

import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.*;

/**
 * Created by federica on 15/06/17.
 */
public class AutomaticTrainer {

    private Path execDir;
    private Path trainingDir;
    private Path langdataDir;
    private String cmdDir;

    public  AutomaticTrainer(Path execDir, Path trainingDir, Path langdataDir ) {
        //final Path execDir = Paths.get(tfExecutablesDir.getText());
        if (!Files.isDirectory(execDir)) {
            throw new RuntimeException("Error Invalid executables directory.");
        }

        this.cmdDir = execDir + File.separator;

        //final Path trainingDir = Paths.get(tfTrainingDir.getText());
        if (!Files.isDirectory(trainingDir)
                || !Files.isWritable(trainingDir)) {
            throw new RuntimeException("Error Invalid training directory.");
        }

        //final Path langdataDir = Paths.get(tfLangdataDir.getText());
        if (langdataDir != null
                && !Files.isDirectory(langdataDir)) {
            throw new RuntimeException("Error Invalid langdata directory.");
        }

        this.execDir = execDir;
        this.trainingDir = trainingDir;
        this.langdataDir=langdataDir;

    }
        public void doInBackGround(){
        // indeterminate
        //TesseractTrainer.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Files.deleteIfExists(trainingDir.resolve("training.log"));

            // create log stream

            try (final PrintStream log = new PrintStream(Files.newOutputStream(
                    trainingDir.resolve("training.log")), true, "UTF-8")) {

                final DirectoryStream<Path> ds = Files.newDirectoryStream(trainingDir, new TesseractTrainer.TrainingFileFilter());

                ProcessBuilder pb;
                InputStream err;
                int c;

                final LinkedList<String> boxFiles = new LinkedList<>();
                final LinkedList<String> trFiles = new LinkedList<>();

                String base = null;

                // train
                for (Path file : ds) {

                    final String sample = file.toString();
                    final String sampleBase = sample.replaceFirst("\\.[^.]+$", "");

                    if (base == null) {

                        final String fname = file.getFileName().toString();
                        base = file.getParent()
                                .resolve(fname.replaceFirst("\\..+", "") + ".")
                                .toString();
                    }
                    boxFiles.add(sampleBase + ".box");
                    trFiles.add(sampleBase + ".tr");

                    pb = new ProcessBuilder(cmdDir + "tesseract", sample, sampleBase, "box.train")
                            .directory(trainingDir.toFile());

                    log.println("tesseract " + sample + " box.train:\n");

                    final Process train = pb.start();

                    err = train.getErrorStream();

                    while ((c = err.read()) != -1) {
                        log.print((char) c);
                    }

                    log.println();

                    if (train.waitFor() != 0) {
                        throw new Exception("Unable to train '" + sample + "'.");
                    }
                }

                final String lang = Paths.get(base).getFileName().toString();


                // delete old unicharset
                Files.deleteIfExists(trainingDir.resolve("unicharset"));

                // extract unicharset
                final java.util.List<String> uniExtractor = new LinkedList<>();
                uniExtractor.add(cmdDir + "unicharset_extractor");
                uniExtractor.addAll(boxFiles);

                pb = new ProcessBuilder(uniExtractor).directory(trainingDir.toFile());

                log.println("\nunicharset_extractor:\n");

                final Process unicharset = pb.start();
                err = unicharset.getInputStream();

                while ((c = err.read()) != -1) {
                    log.print((char) c);
                }

                if (unicharset.waitFor() != 0) {
                    throw new Exception("Unable to extract unicharset.");
                }

                // set unicharset properties
                if (langdataDir != null) {
                    pb = new ProcessBuilder(cmdDir + "set_unicharset_properties",
                            "-U", "unicharset", "-O", "out.unicharset",
                            "--script_dir=" + langdataDir).directory(
                            trainingDir.toFile());

                    log.println("\nset_unicharset_properties:\n");
                    final Process uniProps = pb.start();
                    err = uniProps.getErrorStream();

                    while ((c = err.read()) != -1) {
                        log.print((char) c);
                    }

                    if (uniProps.waitFor() != 0) {
                        throw new Exception("Unable to set unicharset properties.");
                    }
                } else {
                    Files.copy(trainingDir.resolve("unicharset"),
                            trainingDir.resolve("out.unicharset"),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                // mftraining
                final java.util.List<String> mfTraining = new LinkedList<>();
                mfTraining.add(cmdDir + "mftraining");
                mfTraining.add("-F");
                mfTraining.add(lang + "font_properties");
                mfTraining.add("-U");
                mfTraining.add("out.unicharset");
                mfTraining.addAll(trFiles);



                pb = new ProcessBuilder(mfTraining).directory(trainingDir.toFile());



                log.println("\nmftraining:\n");


                final Process mfTrain = pb.start();


                err = mfTrain.getErrorStream();


                while ((c = err.read()) != -1) {
                    log.print((char) c);
                }


                if (mfTrain.waitFor() != 0) {
                    throw new Exception("Unable to do mftraining.");
                }


                // cntraining
                final java.util.List<String> cnTrainingParams = new LinkedList<>();
                cnTrainingParams.add(cmdDir + "cntraining");
                cnTrainingParams.addAll(trFiles);

                pb = new ProcessBuilder(cnTrainingParams).directory(trainingDir.toFile());

                log.println("\ncntraining:\n");
                final Process cnTraining = pb.start();
                err = cnTraining.getErrorStream();

                while ((c = err.read()) != -1) {
                    log.print((char) c);
                }

                if (cnTraining.waitFor() != 0) {
                    throw new Exception("Unable to do cntraining.");
                }

                // rename files
                Files.move(trainingDir.resolve("inttemp"),
                        trainingDir.resolve(lang + "inttemp"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(trainingDir.resolve("normproto"),
                        trainingDir.resolve(lang + "normproto"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(trainingDir.resolve("out.unicharset"),
                        trainingDir.resolve(lang + "unicharset"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(trainingDir.resolve("pffmtable"),
                        trainingDir.resolve(lang + "pffmtable"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(trainingDir.resolve("shapetable"),
                        trainingDir.resolve(lang + "shapetable"),
                        StandardCopyOption.REPLACE_EXISTING);

                // combine
                pb = new ProcessBuilder(cmdDir + "combine_tessdata", lang).directory(trainingDir.toFile());

                log.println("\ncombine_tessdata:\n");
                final Process combine = pb.start();
                err = combine.getErrorStream();

                while ((c = err.read()) != -1) {
                    log.print((char) c);
                }

                if (combine.waitFor() != 0) {
                    throw new Exception("Unable to combine the traineddata files.");
                }

                System.out.println("Training Complete Training completed successfully.");
            } catch (Exception e) {
                System.err.println("Error Training failed. " + e.getMessage());
            } finally {
                //TesseractTrainer.this.setCursor(Cursor.getDefaultCursor());

                try {
                    Desktop.getDesktop().open(
                            trainingDir.resolve("training.log").toFile());
                } catch (IOException e) {
                    System.out.println("Warning Could not open training log file.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error Training failed. " + e.getMessage());
        }
    }
}
