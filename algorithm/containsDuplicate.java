import java.util.HashSet;

/**
 * 重复元素检测练习：输入整数数组，输出是否存在至少两个相同元素。
 * HashSet保存已见值，平均时间O(n)、额外空间O(n)，依赖哈希查询的平均常数时间。
 * 当前要求数组非null，空数组返回false，不修改原数组。
 */
public class containsDuplicate {
    /**
     * 打印含重复整数的样例结果；这是演示入口，不是带断言的自动化测试。
     */
    public static void main(String[] args) {
        System.out.println(containsDuplicate(new int[]{1, 2, 3, 1}));
    }

    /**
     * 逐个检查当前值是否已经见过，命中即返回true，否则加入集合。
     * 集合始终保存已遍历的不同值；遍历完成仍未命中，便说明所有元素互异，返回false。
     */
    public static boolean containsDuplicate(int[] nums) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if(set.contains(nums[i])){
                return true;
            }
            set.add(nums[i]);
        }
        return false;
    }
}
