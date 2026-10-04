import java.util.Arrays;
class Solution {
    public String longestCommonPrefix(String[] strs) {
// method 1 


        // if (strs.length == 0) return "";

        // Arrays.sort(strs);

        // StringBuilder res = new StringBuilder();

        // for (int i = 0; i < strs[0].length(); i++) {

        //     char current = strs[0].charAt(i);

        //     for (int j = 1; j < strs.length; j++) {

        //         if (i >= strs[j].length() ||
        //             strs[j].charAt(i) != current) {

        //             return res.toString();
        //         }
        //     }

        //     res.append(current);
        // }

        // return res.toString();



//method 2 

        if (strs == null || strs.length == 0)
            return "";

        Arrays.sort(strs);

        String first = strs[0];
        String last = strs[strs.length - 1];

        int i = 0;

        while (i < first.length() &&
               i < last.length() &&
               first.charAt(i) == last.charAt(i)) {
            i++;
        }

        return first.substring(0, i);
    }
}






// String[] strs = {"flower", "flow", "flight"};

// // Array
// // length  -> number of elements in array

// System.out.println(strs.length);
// // Output: 3


// String s = "flower";

// // String
// // length() -> number of characters in string

// System.out.println(s.length());
// // Output: 6


// // Access element from array
// System.out.println(strs[0]);
// // Output: flower


// // Access character from string
// System.out.println(s.charAt(0));
// // Output: f

// int[] arr = {1, 2, 3};
// arr.length;      // ✅

// String s = "Java";
// s.length();      // ✅

// arr.length();    // ❌
// s.length;        // ❌