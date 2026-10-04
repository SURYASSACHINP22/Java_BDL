import java.util.Stack;

class Solution {
    public int trap(int[] height) {

// method1 prefix and suffix

        //int len = height.length;

        // if (len <= 2) {
        //     return 0;
        // }

        // int[] prefix = new int[len];
        // prefix[0]=height[0];
        
        // int suffix[] = new int[len];
        // suffix[len-1]=height[len-1];

        // for(int i = 1 ; i < len-1; i++)
        // {
        //     prefix[i] = Math.max(prefix[i-1],height[i]);
        // }

        // for(int i = len-2 ; i >= 0; i--)
        // {
        //     suffix[i] = Math.max(suffix[i+1],height[i]);
        // }

        //int total = 0;

        // for(int i = 0; i < len ; i++)
        // {
        //     int lefttop = prefix[i];
        //     int rightop = suffix[i];

        //     if(height[i] < lefttop && height[i] < rightop){
        //         total += Math.min(lefttop,rightop) - height[i];
        //     }
        // }
        // return total;


//method 2 using two pointer

        int len = height.length;

        int total = 0;

        int l = 0;
        int r = len - 1;

        int lmax = 0;
        int rmax = 0;

        while (l <= r) {

            if (height[l] <= height[r]) {

                if (height[l] >= lmax) {
                    lmax = height[l];
                } else {
                    total += lmax - height[l];
                }

                l++;

            } else {

                if (height[r] >= rmax) {
                    rmax = height[r];
                } else {
                    total += rmax - height[r];
                }

                r--;
            }
        }

        return total;





// method 3 stack method 

        // int total = 0;
        // int len = height.length;

        // Stack<Integer> st = new Stack<>();

        // for(int i = 0 ; i < len ; i++)
        // {
        //     while(!st.empty() && height[i] > height[st.peek()])
        //     {
        //         int depth = st.pop();

        //         if(st.empty()) break;

        //         int left = st.peek();

        //         int width = i - left - 1;

        //         int height_of_currnt_space = Math.min(height[left], height[i]) - height[depth];

        //         total += width * height_of_currnt_space;
        //     }
        //     st.push(i);
        // }

        // return total;

    }
}