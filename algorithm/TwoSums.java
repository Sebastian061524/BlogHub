import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 两数之和练习：读取数组与目标值，用暴力法及HashMap法返回两个不同位置的下标。
 * 无解时返回空数组，多解时各自返回按遍历顺序遇到的一组，两种实现不保证选中同一组。
 * 当前使用int算术，要求和及补数计算不溢出；未额外校验空引用、非法长度或输入格式。
 */
public class TwoSums {
    /**
     * 从标准输入依次读取长度、n个整数、目标值，分别打印两种方法的索引数组；关闭Scanner也会关闭包装的标准输入。
     */
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
    /**
     * 枚举i和比i大的j，避免同一位置配对或重复枚举一对；找到第一对和为target的下标就返回，否则返回空数组。
     */
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
    /**
     * map保存此前遇到的值及下标，先查target减当前值的补数，命中则返回旧下标与当前下标。
     * 先查后放保证不和自己配对；重复值会更新旧下标，但已记录的任一旧位置都可构成答案。
     */
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
