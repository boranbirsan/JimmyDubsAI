import enumerate.Action;

public class ActionObj {

    private float value;
    private int index;
    private Action action;

    public ActionObj(float value, int index){
        this.value = value;
        this.index = index;
    }

    public float getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
