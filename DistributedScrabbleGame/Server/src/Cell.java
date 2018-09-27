public class Cell {

    private Character letter;

    /**
     * Constructor
     */
    public Cell() {

    }


    /**
     * Mutator
     */
    public void setLetter(Character letter) {
        this.letter = letter;
    }

    /**
     * Accessor
     * @return current letter in the cell
     */
    public Character getLetter() { return letter; }
}
