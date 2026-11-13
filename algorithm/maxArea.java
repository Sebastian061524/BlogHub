public class maxArea{
    public static void main(String[] args){
        int[][] inputs = {
                {1, 8, 6, 2, 5, 4, 8, 3, 7},
                {1, 1},
                {0, 0, 0},
                {1, 2, 4, 3}
        };
        int[] expectedAreas = {49, 1, 0, 4};

        for (int i = 0; i < inputs.length; i++) {
            int actualArea = MaxArea(inputs[i]);
            if (actualArea != expectedAreas[i]) {
                throw new AssertionError("Case " + (i + 1) + ": expected "
                        + expectedAreas[i] + ", got " + actualArea);
            }
            System.out.println("Case " + (i + 1) + " passed: " + actualArea);
        }
    }

    public static int MaxArea(int[] nums){
        int n = nums.length;
        int left = 0, right = n - 1;
        int maxArea = 0;
        while (left < right) {
            int area = Math.min(nums[left], nums[right]) * (right - left);
            maxArea = Math.max(area, maxArea);
            if (nums[left] < nums[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxArea;
    }
}
