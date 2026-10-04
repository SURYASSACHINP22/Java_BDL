class Solution {
    public int[] productExceptSelf(int[] nums) {

        int n = nums.length;
        int[] ans = new int[n];

        long pre = 1;
        long suf = 1;

        ans[0] = 1;

        for (int i = 1; i < n; i++) {
            pre *= nums[i - 1];
            ans[i] = (int) pre;
        }

        for (int i = n - 2; i >= 0; i--) {
            suf *= nums[i + 1];
            ans[i] = (int) (ans[i] * suf);
        }

        return ans;
    }
}