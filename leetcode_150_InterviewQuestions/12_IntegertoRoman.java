class Solution {
    public String intToRoman(int num) {

        String[] note = {
            "M", "CM", "D", "CD",
            "C", "XC", "L", "XL",
            "X", "IX", "V", "IV", "I"
        };

        int[] val = {
            1000, 900, 500, 400,
            100, 90, 50, 40,
            10, 9, 5, 4, 1
        };

        StringBuilder ans = new StringBuilder();

        for (int i = 0; i < val.length; i++) {
            while (num >= val[i]) {
                ans.append(note[i]);
                num -= val[i];
            }
        }

        return ans.toString();
    }
}




// recap

//String st = "Hello";
// String Literal
// Stored in String Pool
// Immutable (cannot be changed)

//String st2 = new String("Hello");
// Creates a new String object in Heap memory
// Immutable
// Less memory efficient than literal

//StringBuilder ans = new StringBuilder();
// Mutable string object
// Can be modified using append()
// Faster for repeated string concatenation

//ans.append("Hello");
// Adds text to the same object

//String result = ans.toString();
// Converts StringBuilder to String


//String s = "Java";
// String Literal

//String s1 = "Java";
//String s2 = "Java";
// Both point to same object in String Pool

//String s3 = new String("Java");
// New object created in Heap

//StringBuilder sb = new StringBuilder();
// Used when string changes frequently

//sb.append("A");
//sb.append("B");
// Result = "AB"
