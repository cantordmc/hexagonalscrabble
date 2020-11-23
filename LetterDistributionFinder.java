import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;

public class LetterDistributionFinder {
    public ArrayList<String> words;

    // Constuctor adds words from list
    public LetterDistributionFinder(String filename) {
        Scanner sc;
        try{
            sc = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) {
            System.out.println("ERROR: "+filename+" doesn't exist.");
            return;
        }
        words = new ArrayList<String>();
        while (sc.hasNext()) {
            words.add(sc.next());
        }
    }

    public int[] findDistribution() {
        int[] distribution = new int[26];
        for (String word : words) {
            int count = 0;
            for (char letter : word.toCharArray()) {
                int index = letter - 'A';
                distribution[index]++;
            }
        }
        return distribution;
    }

    public static void main(String[] args) {
        LetterDistributionFinder ref;

        ref = new LetterDistributionFinder("Word Lists/Fours.txt");
        int[] fours = ref.findDistribution();
        System.out.println("Number of Fours: "+ref.words.size());

        ref = new LetterDistributionFinder("Word Lists/Fives.txt");
        int[] fives = ref.findDistribution();
        System.out.println("Number of Fives: "+ref.words.size());

        ref = new LetterDistributionFinder("Word Lists/Sixes.txt");
        int[] sixes = ref.findDistribution();
        System.out.println("Number of Sixes: "+ref.words.size());

        ref = new LetterDistributionFinder("Word Lists/Sevens.txt");
        int[] sevens = ref.findDistribution();
        System.out.println("Number of Sevens: "+ref.words.size());

        ref = new LetterDistributionFinder("Word Lists/Eights.txt");
        int[] eights = ref.findDistribution();
        System.out.println("Number of Eights: "+ref.words.size());

        for (int i = 0; i < 26; i++) {
            double sum = fours[i]/4125.0 + fives[i]/9426.0 + sixes[i]/16612.0 + sevens[i]/25318.0 + eights[i]/31557.0;
            double average = sum/30.0;
            char letter = (char)('A' + i);
            int tiles = 75;
            System.out.println(letter+": "+average+" x "+tiles+" = "+(average*tiles));
        }
    }
}
