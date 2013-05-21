package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CSContext extends Context {
    private int position;

    public CSContext(int position) {
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public int hashCode() {
        return this.position;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CSContext)) {
            return false;
        }
        CSContext comp = (CSContext) obj;
        return this.position == comp.getPosition();
    }

    public String toString() {
        return String.valueOf(this.position);
    }
}