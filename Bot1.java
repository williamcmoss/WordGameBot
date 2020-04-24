import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Bot1 implements BotAPI {

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the game objects
    // It may only inspect the state of the board and the player objects

    private PlayerAPI me;
    private OpponentAPI opponent;
    private BoardAPI board;
    private UserInterfaceAPI info;
    private DictionaryAPI dictionary;
    private int turnCount;
    private ArrayList<Coordinates> newLetterCoords;
    private ArrayList<Word> validWordList;

    private static final int BOARD_SIZE = 15;
    private static final int BONUS = 50; //Bonus points for scoring
    private static final int[] TILE_VALUE = {1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,1,1,1,1,4,4,8,4,10};
    //REVERSE_CHARACTER is added to frame to allow potential word to 'wrap' around anchor letter
    //used to know char anchoring it to the rest of the board, since it is allows first
    //Example: "A" is on board, "CB" in Frame, to spell CAB do, "AB/C"
    private static final char REVERSE_CHARACTER = '/';
    private static final char EMPTY_CHAR = ' ';
    private static final char INVALID_CHAR = '!';
    private static final char WILDCARD_CHAR = Tile.BLANK;

    private char[][] boardCopy;

    Bot1(PlayerAPI me, OpponentAPI opponent, BoardAPI board, UserInterfaceAPI ui, DictionaryAPI dictionary) {
        new DictionaryTrie(); //create Dictionary Trie

        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.info = ui;
        this.dictionary = dictionary;
        this.validWordList = new ArrayList<>();
        turnCount = 0;
        //this board copy exists bc it is quicker to copy once per turn rather than look at the API
        this.boardCopy = new char[][]{
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR},
                {EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR, EMPTY_CHAR}};    }

    public String getCommand() {
        // Add your code here to input your commands
        // Your code must give the command NAME <botname> at the start of the game
        String command = "";
        switch (turnCount) {
            case 0:
                command = "NAME Vanderbot2";
                break;
            default:
                findNewWordUpdateBoard();
                Word bestWord = findBestWord();
                if(bestWord != null)
                    command = playWordCommand(bestWord);
                else
                    command = "X " + currentFrameString(); //if we cant find new word, then swap out all pieces
                break;
        }
        turnCount++;
        return command;
    }
    public Word findBestWord(){
        ArrayList<Word> bestWords = new ArrayList<>();
        for(int row = 0; row < BOARD_SIZE; row++){
            for(int col = 0; col < BOARD_SIZE; col++){
                bestWords.addAll(getWordsAtCoordinates(new Coordinates(row, col)));
            }
        }
        return highestValidWordInArrayList(bestWords);
    }
    public String frameWithoutIndex(String Frame, int i){
        if(i>=Frame.length())
            return Frame;
        return Frame.substring(0, i) + Frame.substring(i + 1);
    }
    //public ArrayList<String> getWords(Tile t)
    //gets all new words
    public ArrayList<Word> getWordsAtCoordinates(Coordinates root){
        ArrayList<Word> wordArrayList = new ArrayList<>();
        if(isOccupied(root.getRow(), root.getCol())){
            wordArrayList.addAll(getWordsHelper("", currentFrameString()+REVERSE_CHARACTER, root, true));
            wordArrayList.addAll(getWordsHelper("", currentFrameString()+REVERSE_CHARACTER, root, false));

        }else if(board.isFirstPlay() && (root.col == 7) && (root.row == 7))
            wordArrayList.addAll(getWordsHelper("", currentFrameString()+REVERSE_CHARACTER, root, true));
        return wordArrayList;
    }

    public Word formattedStringToWord(Coordinates root, String formattedString, boolean isHorizontal){
        int i = formattedString.indexOf(REVERSE_CHARACTER);
        int newRow = root.getRow();
        int newCol = root.getCol();
        if( i > 0 ) {
            newRow = !isHorizontal ? root.row + i - formattedString.length() + 1 : root.row;
            newCol = isHorizontal ? root.col + i - formattedString.length() + 1 : root.col;
        }
        if(i == -1)
            return new Word(newRow, newCol, isHorizontal, formattedString);
        return new Word(newRow, newCol, isHorizontal, formattedString.substring(i+1)+formattedString.substring(0,i));
    }

    // A recursive function that searches for all the words that use root to anchor themselves to the board
    public ArrayList<Word> getWordsHelper(String accumulator, String remainingLetters, Coordinates root, boolean isHorizontal){
        //System.out.println("getWordsHelper");
        ArrayList<Word> words = new ArrayList<>();
        int currCol = root.col;
        int currRow = root.row;
        int indexReverseChar = accumulator.indexOf(REVERSE_CHARACTER);
        Word newWord = null;
        if(indexReverseChar == -1){
            if(isHorizontal)
                currCol = root.getCol() + accumulator.length();
            else
                currRow = root.getRow() + accumulator.length();
        }else{
            if(isHorizontal)
                currCol = root.getCol() + indexReverseChar - accumulator.length() + 1;
            else
                currRow = root.getRow() + indexReverseChar - accumulator.length() + 1;
        }
        if(currCol >= BOARD_SIZE || currRow >= BOARD_SIZE || currCol < 0 || currRow < 0)
            return words;
        if(DictionaryTrie.searchWord(accumulator) && (!remainingLetters.equals(currentFrameString()+REVERSE_CHARACTER))) {
            newWord = formattedStringToWord(root, accumulator, isHorizontal);
            if (isHorizontal
                    && (!isOccupied(newWord.getFirstRow(), newWord.getFirstColumn() - 1))
                    && (!isOccupied(newWord.getLastRow(), newWord.getLastColumn() + 1)))
                words.add(formattedStringToWord(root, accumulator, true));
            if (!isHorizontal
                    && (!isOccupied(newWord.getFirstRow() - 1, newWord.getFirstColumn()))
                    && (!isOccupied(newWord.getLastRow() + 1, newWord.getLastColumn())))
                words.add(formattedStringToWord(root, accumulator, false));
        }
        if(isOccupied(currRow, currCol)){
            if(DictionaryTrie.searchWordFragment(accumulator + getBoardTile(currRow, currCol))){
                words.addAll(getWordsHelper(accumulator + getBoardTile(currRow, currCol), remainingLetters, root, isHorizontal));
            }
        }else{
            for(int i = 0; i < remainingLetters.length(); i++){
                if((remainingLetters.charAt(i) != REVERSE_CHARACTER)&&
                        letterHasSideWord(currRow, currCol, isHorizontal)&&
                        (!checkSideWordValid(remainingLetters.charAt(i), currRow, currCol, !isHorizontal))){
                    //this eliminates chars that would create bad side perpendicular words
                }else if(DictionaryTrie.searchWordFragment(accumulator + remainingLetters.charAt(i))){
                    //This needs to show that a wildcard is being used within the accumulated when a new word is added
                    //we need to use the second Word constructor to differentiate between the letters and designated letters
                    if(remainingLetters.charAt(i) == WILDCARD_CHAR){
                        Set<Character> chars = DictionaryTrie.searchNextChar(accumulator);
                        for(char c : chars){
                            words.addAll(getWordsHelper(accumulator + c,
                                    frameWithoutIndex(remainingLetters , i), root, isHorizontal));                        }
                    }else{
                        words.addAll(getWordsHelper(accumulator + remainingLetters.charAt(i),
                                frameWithoutIndex(remainingLetters , i), root, isHorizontal));
                    }
                }
            }
        }
        return words;
    }
    //true if letter has a side word in the perpendicular direction
    private boolean letterHasSideWord(int row, int col, boolean isMainWordHorizontal) {
        boolean sideWord = false;
        if ((isMainWordHorizontal && (isOccupied(row - 1, col) || isOccupied(row + 1, col))) ||
                (!isMainWordHorizontal && (isOccupied(row, col - 1) || isOccupied(row, col + 1)))) {
            sideWord = true;
        }
        //System.out.println("isSideWord: " +sideWord);
        return sideWord;
    }
    //True if the newLetter can be placed without creating an invalid side word
    public boolean checkSideWordValid(char newLetter, int letterRow, int letterCol, boolean sideWordHorizontal){
        //System.out.println("checkSideWord");
        //GOTO BEGINNING
        String sideWord = "" + newLetter;
        boolean result = false;
        int offset = 0;
        if(sideWordHorizontal){
            while(isOccupied(letterRow, letterCol - offset - 1)){
                sideWord = getBoardTile(letterRow, letterCol - offset - 1) + sideWord;
                offset++;
            }
        }else{
            while (isOccupied(letterRow - offset - 1, letterCol)) {
                sideWord = getBoardTile(letterRow - offset - 1, letterCol) + sideWord;
                offset++;
            }
        }
        offset = 0;
        if(sideWordHorizontal){
            while(isOccupied(letterRow, letterCol + offset + 1)){
                sideWord = sideWord + getBoardTile(letterRow, letterCol + offset + 1);
                offset++;
            }
        }else {
            while (isOccupied(letterRow + offset + 1, letterCol)) {
                sideWord = sideWord + getBoardTile(letterRow + offset + 1, letterCol);
                offset++;
            }
        }
        result = DictionaryTrie.searchWord(sideWord);
        return result;
    }

    public boolean isOccupied(int row, int col){
        return (getBoardTile(row, col) != INVALID_CHAR) && (getBoardTile(row, col) != EMPTY_CHAR);
    }

    public char getBoardTile(int row, int col){
        if(row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
            return INVALID_CHAR;
        return boardCopy[row][col];
    }
    public String currentFrameString(){
        return me.getFrameAsString().replaceAll("[^A-Z_-]", "");
    }
    public Word highestValidWordInArrayList(ArrayList<Word> wordArrayList){
        ArrayList<Tile> tilesInFrame = new ArrayList<>();
        String stringFrame = currentFrameString();
        for(int i = 0; i < stringFrame.length(); i++){
            tilesInFrame.add(new Tile(stringFrame.charAt(i)));
        }
        Frame currentFrame = new Frame();
        currentFrame.addTiles(tilesInFrame);
        int highestScore = 0;
        Word highestScoreWord = null;
        //boolean hasWildcard = false;
        for(Word currentWord:wordArrayList){
            //currentWord = useWildcard(currentWord);
            //System.out.println("LETTERS: " +currentWord.getLetters());
            //System.out.println("DESIGNATED LETTERS: " +currentWord.getDesignatedLetters());
            //System.out.println("FRAME: " + me.getFrameAsString());

            int currentWordScore = getWordPoints(currentWord);
            if((currentWordScore > highestScore) && (board.isLegalPlay(currentFrame, currentWord))){
                highestScore = currentWordScore;
                highestScoreWord = currentWord;
            }
        }
        return highestScoreWord;
    }
    /*
    private Word useWildcard(Word word){
        StringBuilder wordLetters = new StringBuilder(word.getLetters());
        StringBuilder frameLetters = new StringBuilder(currentFrameString());
        int currRow = word.getFirstRow();
        int currCol = word.getFirstColumn();
            for(int i = 0; i < word.length(); i++){
                if(word.isHorizontal()){
                    currCol = word.getFirstColumn() + i;
                }else{
                    currRow = word.getFirstRow() + i;
                }
                if(!isOccupied(currRow, currCol)){

                    if(frameLetters.indexOf(""+wordLetters.charAt(i))!=-1){
                        frameLetters.deleteCharAt(frameLetters.indexOf(""+wordLetters.charAt(i)));
                    }else{
                        wordLetters.replace(i, i+1,""+WILDCARD_CHAR);
                    }
                }
            }
        return new Word(word.getRow(), word.getColumn(), word.isHorizontal(), wordLetters.toString(), word.getLetters());
    }
     */
    //needs to be programmed to take side words into affect when calculating score
    public int getWordPoints(Word word) {
        int wordValue = 0;
        int wordMultipler = 1;
        int r = word.getFirstRow();
        int c = word.getFirstColumn();
        for (int i = 0; i<word.length(); i++) {
            int letterValue = 0;
            if(word.toString().charAt(i)!='_')
                letterValue = TILE_VALUE[(int) word.toString().charAt(i) - (int) 'A'];
            wordValue = wordValue + letterValue * this.board.getSquareCopy(r, c).getLetterMuliplier();
            wordMultipler = wordMultipler * this.board.getSquareCopy(r, c).getWordMultiplier();
            if (word.isHorizontal()) {
                c++;
            } else {
                r++;
            }
        }
        return wordValue * wordMultipler;
    }
    public String playWordCommand(Word currentWord){
        for(int i = 0; i < currentWord.length(); i++){
            if(currentWord.isHorizontal())
                boardCopy[currentWord.getFirstRow()][currentWord.getFirstColumn() + i] = currentWord.getLetter(i);
            else
                boardCopy[currentWord.getFirstRow() + i][currentWord.getFirstColumn()] = currentWord.getLetter(i);
        }
        return coordinatesToCommand(currentWord.getRow(), currentWord.getColumn())
                + " " + (currentWord.isHorizontal() ? "A" : "D") + " " + currentWord.getDesignatedLetters();
    }
    //This method is run to update the local board
    //Ideally it would return the word the other player used to try to challange their new word if it is not in the
    //dictionary is not a valid word, basically check if new word is in our dictionary, and if the side words are in
    //our dictionary
    public Word findNewWordUpdateBoard(){
        ArrayList<Coordinates> newLetters = new ArrayList<>();
        for(int row = 0; row < BOARD_SIZE; row++){
            for(int col = 0; col < BOARD_SIZE; col++){
                if(this.board.getSquareCopy(row, col).isOccupied()){
                    char letter = this.board.getSquareCopy(row, col).getTile().getLetter();
                    if(boardCopy[row][col]==EMPTY_CHAR)
                        newLetters.add(new Coordinates(row, col));
                    boardCopy[row][col] = this.board.getSquareCopy(row, col).getTile().getLetter();
                }else{
                    boardCopy[row][col] = EMPTY_CHAR;
                }
            }
        }
        return null;
    }
    public String coordinatesToCommand(int row, int column){
        if(((row<0)||(row>14))||((column<0)||(column>14))){
            return "PASS";
        }else{
            return ""+(char)(column + ((int) 'A'))+(row+1);
        }
    }
}