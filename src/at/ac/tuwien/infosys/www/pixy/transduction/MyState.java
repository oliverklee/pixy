package at.ac.tuwien.infosys.www.pixy.transduction;

public class MyState {

    public final int i;

    boolean initial;

    boolean terminal;

    MyState(int i, boolean initial, boolean terminal) {
        this.i = i;
        this.initial = initial;
        this.terminal = terminal;
    }

    MyState(MyState orig) {
        this.i = orig.i;
        this.initial = orig.initial;
        this.terminal = orig.terminal;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isInitial() {
        return this.initial;
    }

    public boolean isTerminal() {
        return this.terminal;
    }

    public String toString() {
        return Integer.toString(i);
    }

    public boolean equals(Object o) {
        try {
            MyState ds = (MyState) o;
            return (ds.i == i);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return i;
    }
}