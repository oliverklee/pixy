package at.ac.tuwien.infosys.www.pixy.conversion.includes;

import java.io.File;
import java.io.IOException;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
class IncludeNode {
    private File file;
    private String canonicalPath;

    IncludeNode(File file) {
        this.file = file;
        try {
            this.canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    File getFile() {
        return this.file;
    }

    String getCanonicalPath() {
        return this.canonicalPath;
    }

    public int hashCode() {
        return this.canonicalPath.hashCode();
    }

    public boolean equals(Object obj) {
        // the "equals" method of File is stupid:
        // only compares with File.getPath() (not canonical
        // and not even absolute), so we have to do this
        // ourselves
        if (!(obj instanceof IncludeNode)) {
            return false;
        }
        IncludeNode comp = (IncludeNode) obj;

        return this.canonicalPath.equals(comp.getCanonicalPath());
    }

    public String toString() {
        return this.canonicalPath;
    }
}