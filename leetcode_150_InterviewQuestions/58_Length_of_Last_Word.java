class Solution {
    public int lengthOfLastWord(String s) {
        // int count = 0;
        // int idx = s.length() - 1;

        // while (idx >= 0) {
        //     if (s.charAt(idx) != ' ') {
        //         break;
        //     }
        //     idx--;
        // }

        // for (int i = idx; i >= 0; i--) {
        //     if (s.charAt(i) == ' ') {
        //         break;
        //     }
        //     count++;
        // }

        // return count;


        s = s.trim();
        return s.length() - s.lastIndexOf(' ') - 1;



    }
}



//trim()            // removes leading and trailing spaces

//lastIndexOf(' ')  // finds last space position

//length()          // length of string