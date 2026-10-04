class Solution {
    public boolean canJump(int[] nums) {
        int jump = 0;  
        
        if (nums.length == 1) return true;  

        for (int i = 0; i < nums.length; i++) {

            if (i > jump) return false;  

            jump = Math.max(jump, nums[i] + i); 

            if (jump >= nums.length - 1) return true;
        }
        return false; 
    }
}