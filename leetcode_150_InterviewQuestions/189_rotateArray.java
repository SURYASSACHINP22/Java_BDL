class Solution {
    public void rotate(int[] nums, int k) {

        int n = nums.length;
        k = k % n;

        int[] temp = new int[n];

        for (int i = 0; i < n; i++) {
            temp[(i + k) % n] = nums[i];
        }

        for (int i = 0; i < n; i++) {
            nums[i] = temp[i];
        }

        // int n = nums.length;
        // k %= n;

        // while (k > 0) 
        // {
        //     int temp = nums[n - 1];

        //     for (int i = n - 1; i > 0; i--) {
        //         nums[i] = nums[i - 1];
        //     }

        //     nums[0] = temp;
        //     k--;
        // }
    }
}

