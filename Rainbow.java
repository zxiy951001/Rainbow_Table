import java.io.*;
import java.util.*;
import java.security.*;
import java.math.BigInteger;

class Rainbow {
    
    // List of words
    private static List<String> words = new ArrayList<String>();

    // Hash function H (effectively MD5)
    private static String hash(String word) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Compute hash on a byte level
            byte[] msgDigest = md.digest(word.getBytes());
            // Conver to integer
            BigInteger num = new BigInteger(1, msgDigest);
            // Convert to hexadecimal string
            String hexString = num.toString(16);
            // Prepend with zeroes as required
            while (hexString.length() < 32) {
                hexString = "0" + hexString;
            }
            return hexString;
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("MD5 is not a valid algorithm to be used for hashing.");
        }
        return "";
    }

    // Reduction function R
    private static String reduce(String hashHex) {
        // Find the big integer representing the hex first
        BigInteger hashNum = new BigInteger("0");
        // Remove padding of 0s at the start
        while (hashHex.charAt(0) == '0') hashHex = hashHex.substring(1);
        // Process the hex string
        String hexadecimal = "0123456789abcdef";
        for (int i = 0; i < hashHex.length(); i++) {
            hashNum = hashNum.multiply(new BigInteger("16")).add(
                            BigInteger.valueOf(hexadecimal.indexOf(hashHex.charAt(i))));
        }
        // Modulo the dictionary size to get the next password
        return words.get(hashNum.mod(new BigInteger("1000")).intValue());
    }

    // Generating rainbow table
    private static void generateRainbowTable() {
        // Storage of final hash values H and initial words W
        SortedMap<String, String> rainbowTable = new TreeMap<String, String>(new Comparator<String>() {
            // Sort in ascending order of hash (key)
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });
        
        // Initialize boolean array to mark whether a word is used or not
        boolean[] used = new boolean[words.size()];
        // For every word
        for (int i = 0; i < words.size(); i++) {
            // If it is used, skip
            if (used[i]) continue;
            // Use it
            used[i] = true;
            
            String currString = words.get(i);
            currString = hash(currString);
            
            for (int j = 0; j < 4; j++) { 
                currString = reduce(currString);
                used[words.indexOf(currString)] = true;
                currString = hash(currString);
            }
            // Put the hash followed by initial word
            rainbowTable.put(words.get(i), currString);
        }
        // Output to a file Rainbow.txt our rainbow table
        try {
            PrintWriter pw = new PrintWriter(new File("Rainbow.txt"));
            // Loop through the chains generated
            for (String wordValue : rainbowTable.keySet()) {
                String hashValue = rainbowTable.get(wordValue);
                pw.write(wordValue + " " + hashValue + "\n");
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File Rainbow.txt could not be created.");
        }
        // Indicate done
        System.out.println("Rainbow table successfully generated.");
    }

    // Read all words from the file
    private static void readPasswords(String filename) throws IOException {
        // Scan line by line for password
        Scanner sc = new Scanner(new File(filename));
        while(sc.hasNextLine()) {
            words.add(sc.nextLine());
        }
        sc.close();
        // Report number of words
        System.out.println("Number of words read: " + words.size());
    }

    private static void findWordForHash(String userInput) throws IOException {
        // If user input is not length 32, stop (non-MD5)
        if (userInput.length() != 32) {
            System.out.println("Failed: hash is not MD5 hash (length is not 32)");
            return;
        }
        // Read the rainbow table from Rainbow.txt first (hash first, then word)
        Map<String, String> rainbowTable = new HashMap<String, String>();
        // Scan file and add to rainbow table
        Scanner sc = new Scanner(new File("Rainbow.txt"));
        while (sc.hasNextLine()) {
            String[] input = sc.nextLine().split(" ");
            rainbowTable.put(input[0], input[1]);
        }
        sc.close();
	
	String pImage = "";
	String prevInput = userInput;
	for (String wordValue : rainbowTable.keySet())
	{
		String hashValue = rainbowTable.get(wordValue);
		userInput = prevInput;
        // Check current hash + reduce and hash 5 more times
        for (int i = 0; i < 5; i++) {
            // If the hash exists in rainbow table, use it to our advantage
            if (userInput.equals(hashValue))  {
                // Start from the start of the chain (i.e. start word)
                String currWord = wordValue;
                for (int j = 0; j < 5; j++) {
					String hashWord = hash(currWord);
                    // If this word hashes to the user input
                    if (hashWord.equals(prevInput)) {
                      	pImage = currWord;

                        break;
                    // Otherwise, hash and reduce it
                    } else {
                        currWord = reduce(hashWord);
                    }
                }
                //break;
            // Otherwise, reduce and hash again
            } else {
                userInput = hash(reduce(userInput));
            }
	      if(pImage != "")
	      {
		break;
	      }
	 }
	 if (pImage != "")
		{
		System.out.println("Found preimage of hash: " + pImage);
		break;
	      	}
    }
}

    public static void main(String[] args) {
        System.out.println("--------- Assignment 1 ---------");
        System.out.println("-------- System Security -------");
        try {
            // Read all the passwords first
            readPasswords(args[0]);
            // Generate the rainbow table
            generateRainbowTable();
        } catch (IOException ex) {
            System.out.println("specified word list " + args[0] + " does not exist.");
            return;
        }
        // Ask the user to specify a hash
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter desired hash: ");
        // Use the rainbow table to find the word leading to the hash if possible
        try {
            findWordForHash(sc.nextLine());
        } catch(IOException ex) {
            System.out.println("Rainbow table file Rainbow.txt could not be found");
        }
        sc.close();
    }
}
