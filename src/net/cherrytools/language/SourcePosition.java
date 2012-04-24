package net.cherrytools.language;

public class SourcePosition {
    private int lineNo;
    private int columnNo;

    public SourcePosition(int lineNumber, int columnNumber) {
        this.lineNo = lineNumber;
        this.columnNo = columnNumber;
    }

    public int getLineNumber() {
        return lineNo;
    }

    public int getColumnNumber() {
        return columnNo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof SourcePosition))
            return false;
        SourcePosition pos = (SourcePosition) object;
        return this.lineNo == pos.lineNo && this.columnNo == pos.columnNo;
    }

    @Override
    public String toString() {
        return "line " + lineNo + " at column " + columnNo;
    }
}
