class Solution {
    public int maxProfit(int[] prices) {

        int mini_val = prices[0];

        int cur_profit = 0;

        for(int i = 1 ; i < prices.length ; i++)
        {
            int cost = prices[i] - mini_val;

            cur_profit = Math.max(cur_profit,cost);

            mini_val = Math.min(mini_val,prices[i]);
        }
        return cur_profit;
    }
}