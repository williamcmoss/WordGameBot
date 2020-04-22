
import java.util.ArrayList;
import java.util.Iterator;

public class Bot0 implements BotAPI {

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

    public static final int BOARD_SIZE = 15;
    public static final int BONUS = 50;

    Bot0(PlayerAPI me, OpponentAPI opponent, BoardAPI board, UserInterfaceAPI ui, DictionaryAPI dictionary) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.info = ui;
        this.dictionary = dictionary;
        turnCount = 0;
    }

    public String getCommand() {
        // Add your code here to input your commands
        // Your code must give the command NAME <botname> at the start of the game
        String command = "";
        switch (turnCount) {
            case 0:
                command = "NAME Vanderbot";
                break;
            case 1:
                command = "PASS";
                break;
            case 2:
                command = "HELP";
                break;
            case 3:
                command = "SCORE";
                break;
            case 4:
                command = "POOL";
                break;
            default:
                command = playWordCommand(getFirstWord());
                break;
        }
        turnCount++;
        return command;
    }
    //Finds highest scoring word for the first play
    public Word getFirstWord() {
        int row = 7;
        int col = 7;
        String Frame = me.getFrameAsString();
        ArrayList<Word> allWords = getWords(Frame, "", 0);
        System.out.println(allWords);
        Iterator itr = allWords.iterator();
        while (itr.hasNext())
        {
            ArrayList<Word> Temp = new ArrayList<Word>();
            Temp.add((Word) itr.next());
            if(!dictionary.areWords(Temp))
                itr.remove();
        }
        Word highestScoreWord = null;
        int highestScore = 0;
        for(Word currentWord: allWords){
            if(this.getWordPoints(currentWord)>highestScore){
                highestScore = this.getWordPoints(currentWord);
                highestScoreWord = currentWord;
            }
        }
        return highestScoreWord;
    }
    //letters are the letters left in the frame, this is recursive so anchor starts out with just one letter and
    // builds words around it. The idea is that since every new word must be connected to an old one, you can just
    // run this getWords method "anchored" to every tile with a letter. probably not the best way but oh well
    public static ArrayList<Word> getWords(String letters, String anchor, int index){
        ArrayList<Word> output = new ArrayList<Word>();
        output.add(new Word(7, 7, true, anchor));
        if(letters.length()!=0){
            for(int i = 0; i<letters.length(); i++){
                String newLetters = letters.substring(0, i) + letters.substring(i+1);
                output.addAll(getWords(newLetters, letters.charAt(i)+anchor,index-1));
                output.addAll(getWords(newLetters, anchor+letters.charAt(i),index));
            }
        }

        return output;
    }

    public int calculatePlayScore(Word currentWord){
        return getWordPoints(currentWord);
    }

    private int getWordPoints(Word word) {
        int wordValue = 0;
        int wordMultipler = 1;
        int r = word.getFirstRow();
        int c = word.getFirstColumn();
        for (int i = 0; i<word.length(); i++) {
            int letterValue = this.board.getSquareCopy(r, c).getTile().getValue();
            if (newLetterCoords.contains(new Coordinates(r,c))) {
                wordValue = wordValue + letterValue * this.board.getSquareCopy(r, c).getLetterMuliplier();
                wordMultipler = wordMultipler * this.board.getSquareCopy(r, c).getWordMultiplier();
            } else {
                wordValue = wordValue + letterValue;
            }
            if (word.isHorizontal()) {
                c++;
            } else {
                r++;
            }
        }
        return wordValue * wordMultipler;
    }

    public String playWordCommand(Word currentWord){
        return coordinatesToCommand(currentWord.getRow(), currentWord.getColumn())
                + " " + (currentWord.isHorizontal() ? "A" : "D") + " " + currentWord.getDesignatedLetters();
    }
    public String coordinatesToCommand(int row, int column){
        if(((row<0)||(row>14))||((column<0)||(column>14))){
            return "PASS";
        }else{
            return ""+(char)(column + ((int) 'A'))+(row+1);
        }
    }
}