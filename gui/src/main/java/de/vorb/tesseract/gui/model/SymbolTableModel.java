package de.vorb.tesseract.gui.model;

import de.vorb.tesseract.util.Symbol;

import javax.swing.DefaultListModel;

public class SymbolTableModel extends FilteredTableModel<Symbol> {
    private static final long serialVersionUID = 1L;

    public SymbolTableModel() {
        super(new FilteredListModel<>(new DefaultListModel<>()));
    }

    //@Override
    //public int getColumnCount() {
    //    return 6;
    //}

    //federica/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int getColumnCount() {
        return 7;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        if (colIndex == 0) {
            return rowIndex + 1;
        }

        final Symbol symbol = getSource().getElementAt(rowIndex);

        switch (colIndex) {
            case 1:
                return symbol.getText();
            case 2:
                return symbol.getBoundingBox().getX();
            case 3:
                return symbol.getBoundingBox().getY();
            case 4:
                return symbol.getBoundingBox().getWidth();
            case 5:
                return symbol.getBoundingBox().getHeight();
            //federica/////////////////////////////////////////////////////////////////////////////////////////////////
            case 6:
                return symbol.getConfidence();
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            default:
                throw new IndexOutOfBoundsException("undefined row or column");
        }
    }

    @Override
    public String getColumnName(int colIndex) {
        switch (colIndex) {
            case 0:
                return "#";
            case 1:
                return "Symbol";
            case 2:
                return "X";
            case 3:
                return "Y";
            case 4:
                return "Width";
            case 5:
                return "Height";
            //federica/////////////////////////////////////////////////////////////////////////////////////////////////
            case 6:
                return "Confidence";
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            default:
                throw new IndexOutOfBoundsException("undefined column");
        }
    }

    @Override
    public Class<?> getColumnClass(int colIndex) {
        switch (colIndex) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            case 3:
                return Integer.class;
            case 4:
                return Integer.class;
            case 5:
                return Integer.class;
            //federica/////////////////////////////////////////////////////////////////////////////////////////////////
            case 6:
                return Float.class;
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            default:
                throw new IndexOutOfBoundsException("undefined column");
        }
    }

    public Symbol getSymbol(int index) {
        return getSource().getElementAt(index);
    }
}
