public class Cell {

    private char letter;

    /**
     * Constructor
     */
    public Cell() {

    }


    /**
     * Mutator
     */
    private void setLetter(char letter) {
        this.letter = letter;
    }

    /**
     * Accessor
     * @return current letter in the cell
     */
    private char getLetter() {
        return letter;
    }
}
