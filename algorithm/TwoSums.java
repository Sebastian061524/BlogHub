import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TwoSums {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 输入顺序：数组长度、各个元素、目标值。
        int n = scanner.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = scanner.nextInt();
        }
        int target = scanner.nextInt();
        scanner.close();

        System.out.println("暴力法索引：" + Arrays.toString(twoSumBruteForce(nums, target)));
        System.out.println("HashMap 法索引：" + Arrays.toString(twoSumHashMap(nums, target)));
    }

    // 暴力法：时间 O(n²)，额外空间 O(1)，不计返回数组。
    private static int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[0];
    }

    // HashMap 法：平均时间 O(n)，额外空间 O(n)。
    private static int[] twoSumHashMap(int[] nums, int target) {
        Map<Integer, Integer> numMap = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (numMap.containsKey(complement)) {
                return new int[]{numMap.get(complement), i};
            }
            numMap.put(nums[i], i);
        }
        return new int[0];
    }
}
